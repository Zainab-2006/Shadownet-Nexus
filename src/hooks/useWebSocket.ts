import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE } from '@/lib/config';

interface UseWebSocketProps {
  onMessage?: (topic: string, data: unknown) => void;
  teamId?: string;
}

interface WebSocketStatus {
  connected: boolean;
  socket: Client | null;
}

const parseMessageBody = (body: string): unknown => {
  try {
    return JSON.parse(body);
  } catch {
    return body;
  }
};

const reportRealtimeIssue = (message: string, detail?: unknown) => {
  if ((import.meta as any).env?.DEV) {
    console.debug(message, detail);
  }
};


export const useWebSocket = (props: UseWebSocketProps = {}): WebSocketStatus => {
  const { onMessage, teamId } = props || {};
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    let attempts = 0;
    const maxAttempts = 6;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
      // Avoid infinite reconnect hammering when backend is unhealthy.
      reconnectDelay: 3000,
      // Cap reconnect attempts to prevent endless 503 spam.
      beforeConnect: () => {
        attempts += 1;
        if (attempts > maxAttempts) {
          // If we keep failing, deactivate so we stop reconnecting.
          try {
            client.deactivate();
          } catch {
            // ignore
          }
        }
      },

      heartbeatIncoming: 10000,

      heartbeatOutgoing: 10000,
      debug: () => undefined,
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
      },
      onConnect: () => {
        setConnected(true);
        if (client) {
          client.subscribe('/topic/leaderboard', (message) => {
            onMessageRef.current?.('/topic/leaderboard', parseMessageBody(message.body));
          });
          client.subscribe('/topic/team', (message) => {
            onMessageRef.current?.('/topic/team', parseMessageBody(message.body));
          });
          if (teamId) {
            client.subscribe(`/topic/team/${teamId}`, (message) => {
              onMessageRef.current?.(`/topic/team/${teamId}`, parseMessageBody(message.body));
            });
          }
          client.subscribe('/user/queue/private', (message) => {
            onMessageRef.current?.('/user/queue/private', parseMessageBody(message.body));
          });
        }
      },
      onStompError: (frame) => {
        reportRealtimeIssue('Realtime broker rejected the connection.', frame);
        setConnected(false);
      },
      onWebSocketClose: () => {
        setConnected(false);
      },
      onWebSocketError: (event) => {
        reportRealtimeIssue('Realtime socket transport error.', event);
        setConnected(false);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
      clientRef.current = null;
      setConnected(false);
    };
  }, [teamId]);

  return { connected, socket: clientRef.current };
};
