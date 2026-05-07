import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RenderPortProxy {
    private static final int CONNECT_TIMEOUT_MS = 750;
    private static final int BOOT_GRACE_SECONDS = intEnv("BOOT_HEALTH_GRACE_SECONDS", 240);
    private static final long STARTED_AT = Instant.now().getEpochSecond();
    private static final String DEFAULT_ALLOWED_ORIGINS =
            "https://shadownet-nexus.vercel.app,https://*.vercel.app,https://shadownet-frontend.onrender.com";

    private RenderPortProxy() {
    }

    public static void main(String[] args) throws IOException {
        int publicPort = intEnv("PORT", 10000);
        int backendPort = intEnv("APP_PORT", 3001);
        ExecutorService executor = Executors.newCachedThreadPool();

        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("0.0.0.0", publicPort));
            System.out.printf("RenderPortProxy listening on 0.0.0.0:%d -> 127.0.0.1:%d%n", publicPort, backendPort);
            while (true) {
                Socket client = server.accept();
                executor.submit(() -> handle(client, backendPort));
            }
        }
    }

    private static void handle(Socket client, int backendPort) {
        try {
            Socket backend = new Socket();
            backend.connect(new InetSocketAddress("127.0.0.1", backendPort), CONNECT_TIMEOUT_MS);
            pipeBothWays(client, backend);
        } catch (IOException unavailable) {
            respondWhileBooting(client);
        }
    }

    private static void pipeBothWays(Socket client, Socket backend) throws IOException {
        Thread upstream = new Thread(() -> pipe(client, backend), "proxy-client-to-backend");
        Thread downstream = new Thread(() -> pipe(backend, client), "proxy-backend-to-client");
        upstream.start();
        downstream.start();
        try {
            upstream.join();
            downstream.join();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static void pipe(Socket source, Socket target) {
        try {
            source.getInputStream().transferTo(target.getOutputStream());
        } catch (IOException ignored) {
            // The peer closing either side is normal for HTTP keep-alive and WebSocket upgrades.
        } finally {
            close(source);
            close(target);
        }
    }

    private static void respondWhileBooting(Socket client) {
        try {
            client.setSoTimeout(250);
            RequestInfo request = readRequest(client.getInputStream());
            String requestLine = request.requestLine().toLowerCase(Locale.ROOT);
            boolean optionsRequest = requestLine.startsWith("options ");
            boolean healthRequest = requestLine.startsWith("get /health ") || requestLine.startsWith("get /actuator/health ");
            boolean withinGrace = Instant.now().getEpochSecond() - STARTED_AT <= BOOT_GRACE_SECONDS;

            if (optionsRequest) {
                writeResponse(client.getOutputStream(), 204, "", request.origin(), request.requestHeaders());
            } else if (healthRequest && withinGrace) {
                writeResponse(client.getOutputStream(), 200, "{\"status\":\"starting\"}", request.origin(),
                        request.requestHeaders());
            } else {
                writeResponse(client.getOutputStream(), 503,
                        "{\"status\":\"starting\",\"message\":\"Backend is still booting\"}", request.origin(),
                        request.requestHeaders());
            }
        } catch (IOException ignored) {
            close(client);
        }
    }

    private static RequestInfo readRequest(InputStream input) throws IOException {
        String requestLine = readLine(input);
        String origin = "";
        String requestHeaders = "";
        String line;
        while (!(line = readLine(input)).isBlank()) {
            int separator = line.indexOf(':');
            if (separator > 0 && "origin".equalsIgnoreCase(line.substring(0, separator).trim())) {
                origin = line.substring(separator + 1).trim();
            } else if (separator > 0
                    && "access-control-request-headers".equalsIgnoreCase(line.substring(0, separator).trim())) {
                requestHeaders = line.substring(separator + 1).trim();
            }
        }
        return new RequestInfo(requestLine, origin, requestHeaders);
    }

    private static String readLine(InputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        while (line.length() < 4096) {
            int next;
            try {
                next = input.read();
            } catch (SocketTimeoutException timeout) {
                break;
            }
            if (next == -1 || next == '\n') {
                break;
            }
            if (next != '\r') {
                line.append((char) next);
            }
        }
        return line.toString();
    }

    private static void writeResponse(OutputStream output, int status, String body, String origin,
            String requestHeaders) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String reason = switch (status) {
            case 200 -> "OK";
            case 204 -> "No Content";
            default -> "Service Unavailable";
        };
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + corsHeaders(origin, requestHeaders)
                + "Connection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        output.write(bodyBytes);
        output.flush();
    }

    private static String corsHeaders(String origin, String requestHeaders) {
        if (!isAllowedOrigin(origin)) {
            return "";
        }
        return "Access-Control-Allow-Origin: " + origin + "\r\n"
                + "Access-Control-Allow-Credentials: true\r\n"
                + "Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS\r\n"
                + "Access-Control-Allow-Headers: " + allowedHeaders(requestHeaders) + "\r\n"
                + "Vary: Origin\r\n";
    }

    private static String allowedHeaders(String requestHeaders) {
        if (requestHeaders == null || requestHeaders.isBlank()) {
            return "Authorization,Content-Type,Accept,Origin,X-Requested-With,Cache-Control,Pragma";
        }
        return requestHeaders;
    }

    private static boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        String allowedOrigins = System.getenv().getOrDefault("CORS_ORIGINS", DEFAULT_ALLOWED_ORIGINS);
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .anyMatch(allowed -> origin.equals(allowed) || matchesWildcardOrigin(origin, allowed));
    }

    private static boolean matchesWildcardOrigin(String origin, String allowed) {
        int wildcard = allowed.indexOf('*');
        if (wildcard < 0) {
            return false;
        }
        String prefix = allowed.substring(0, wildcard);
        String suffix = allowed.substring(wildcard + 1);
        return origin.startsWith(prefix) && origin.endsWith(suffix);
    }

    private static int intEnv(String name, int fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }

    private static void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    private record RequestInfo(String requestLine, String origin, String requestHeaders) {
    }
}
