import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE } from '@/lib/config';

interface UseWebSocketProps {
  onMessage?: (topic: string, data: any) => void;
  teamId?: string;
}

interface WebSocketStatus {
  connected: boolean;
  socket: Client | null;
}

export const useWebSocket = (props: UseWebSocketProps): WebSocketStatus => {
  const { onMessage, teamId } = props || {};
  const clientRef = useRef<Client | null>(null);
  const onMessageRef = useRef(onMessage);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
      },
      onConnect: () => {
        setConnected(true);
        if (client) {
          client.subscribe('/topic/leaderboard', (message) => {
            onMessageRef.current?.('/topic/leaderboard', JSON.parse(message.body));
          });
          client.subscribe('/topic/team', (message) => {
            onMessageRef.current?.('/topic/team', JSON.parse(message.body));
          });
          if (teamId) {
            client.subscribe(`/topic/team/${teamId}`, (message) => {
              onMessageRef.current?.(`/topic/team/${teamId}`, JSON.parse(message.body));
            });
          }
          client.subscribe('/user/queue/private', (message) => {
            onMessageRef.current?.('/user/queue/private', JSON.parse(message.body));
          });
        }
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
        setConnected(false);
      },
      onWebSocketError: () => {
        console.error('WebSocket error');
        setConnected(false);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [teamId]);

  return { connected, socket: clientRef.current };
};


