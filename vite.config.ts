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
        changeOrigin: true
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
