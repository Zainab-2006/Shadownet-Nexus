import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { componentTagger } from "lovable-tagger";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  define: {
    global: 'globalThis',
    'global.process': {},
  },
  server: {
    host: "::",
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:3001',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Proxying', req.method, req.url);
          });
          proxy.on('error', (err, req, res) => {
            console.log('Proxy ERROR', req.url, err.message);
            res.writeHead(500, {
              'Content-Type': 'text/plain',
            });
            res.end('Proxy Error');
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Proxy Response', req.method, req.url, proxyRes.statusCode);
          });
        }
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
}));
