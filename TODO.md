# TODO (Networking 503 + WebSocket failures)

- [x] Inspect frontend + backend networking paths
- [x] Confirm backend SockJS/STOMP endpoint mapping is `/ws` and health endpoint is `/health`
- [x] Update frontend `src/lib/config.ts` to read `VITE_API_BASE_URL` / `VITE_WS_BASE_URL` (with defaults)
- [x] Reduce websocket reconnect hammering in `src/hooks/useWebSocket.ts` (lower delay + cap attempts)
- [x] Add limited retry/backoff behavior for 503 on GET requests (API calls) to reduce log spam

- [ ] Build frontend and run basic smoke test
- [ ] Validate in browser console: `/api/*` and `/ws/info` no longer spam 503

