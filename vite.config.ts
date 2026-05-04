import type { UserConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import type { ServerResponse } from "node:http";
import { componentTagger } from "lovable-tagger";

const devProxyTarget = process.env.VITE_DEV_PROXY_TARGET || "http://localhost:3001";

function isServerResponse(value: unknown): value is ServerResponse {
  return typeof value === "object" && value !== null && "writeHead" in value && "end" in value;
}

// https://vitejs.dev/config/
export default ({ mode }: { mode: string }): UserConfig => ({
  define: {
    global: 'globalThis',
    'global.process': {},
  },
  server: {
    host: "::",
    port: 5173,
    proxy: {
      '/api': {
        target: devProxyTarget,
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Proxying', req.method, req.url);
          });
          proxy.on('error', (err, req, res) => {
            console.log('Proxy ERROR', req.url, err.message);
            if (isServerResponse(res)) {
              res.writeHead(500, {
                'Content-Type': 'text/plain',
              });
              res.end('Proxy Error');
            }
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Proxy Response', req.method, req.url, proxyRes.statusCode);
          });
        }
      },
      '/ws': {
        target: devProxyTarget,
        changeOrigin: true,
        ws: true,
      }
    },
    hmr: {
      overlay: false,
    },
  },
  plugins: [react(), mode === "development" && componentTagger()].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),

    },
  },
  optimizeDeps: {
    exclude: ['@tanstack/react-query'],
    include: ['react', 'react-dom', 'react-router-dom'],
  },
  esbuild: {
    sourcemap: false,
  },
});
