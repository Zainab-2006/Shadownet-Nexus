import React, { useState, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Users, CheckCircle2 } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import { useUser, useTeamSession, useAddTeamEvidence, useToggleTeamReady, useAccuseTeam, useStartTeam } from '@/api/shadownetApi';
import { useGame } from '@/context/GameContext';
import { useWebSocket } from '@/hooks/useWebSocket';

interface MissionCellPanelProps {
  teamId?: string;
  missionId?: string;
  onTeamCreated?: (teamId: string) => void;
}

type TeamState = {
  sessionId: string;
  players: Array<{
    id: string;
    name?: string;
    displayName?: string;
    username?: string;
    operatorCodename?: string;
    contributionSummary?: string;
    ready: boolean;
    role: string;
    connected: boolean;
  }>;
  phase: 'lobby' | 'active' | 'evidence' | 'accusation';
  evidenceCount: number;
  evidence?: {
    items?: Record<string, number>;
    threshold?: number;
    whyItMatters?: string;
  };
  trust?: {
    currentTrust?: number;
    state?: string;
    reason?: string;
    gameplayEffect?: string;
  };
  data: unknown;
  accusationUnlocked: boolean;
  accusationTargetPool?: Array<string | { id: string; label?: string; name?: string; codename?: string }>;
};

type CellMessage = {
  text?: string;
  timestamp?: string;
  userId?: string | number;
  username?: string;
};

const MissionCellPanel = ({ teamId = '', missionId }: MissionCellPanelProps) => {
  const [messages, setMessages] = useState<CellMessage[]>([]);
  const [accusedId, setAccusedId] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();
  const game = useGame();
  const { selectedOperator, refreshProgression } = game;
  const { data: user } = useUser();
  const { data: teamData, refetch: refetchTeam } = useTeamSession(teamId);
  const { connected: wsConnected } = useWebSocket({
    teamId,
    onMessage: (topic, msg) => {
      if ((topic === `/topic/team/${teamId}` || topic === '/topic/team') && (!msg.teamId || msg.teamId === teamId)) {
        if (msg.type === 'team:message') {
          setMessages(prev => [...prev, msg.data]);
        }
        queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] });
      }
    },
  });

  const addEvidenceMutation = useAddTeamEvidence(teamId);
  const accuseMutation = useAccuseTeam(teamId);
  const startTeamMutation = useStartTeam(teamId);
  const toggleReadyMutation = useToggleTeamReady(teamId);

  const sendMessage = () => {
    const text = inputRef.current?.value.trim();
    if (!text || !user) return;
    // Message sending logic (WS)
    inputRef.current!.value = '';
    setMessages(prev => [...prev, { text, timestamp: new Date().toISOString(), userId: user.id, username: user.username || 'Anon' }]);
  };

  const addEvidence = async () => {
    addEvidenceMutation.mutate(undefined, {
      onSuccess: () => refreshProgression()
    });
  };

  const accuseTeam = async () => {
    if (!accusedId) return;
    accuseMutation.mutate(accusedId, {
      onSuccess: () => refreshProgression()
    });
  };

  const toggleReady = () => toggleReadyMutation.mutate(!isReady);
  const startTeam = () => startTeamMutation.mutate(undefined, {
    onSuccess: () => refreshProgression()
  });

const teamState = teamData as TeamState | null;
  const isReady = teamState?.players.find(p => p.id === user?.id)?.ready || false;
  const isLeader = teamState?.players.find(p => p.id === user?.id)?.role === 'leader';

  if (!teamId) {
    return (
      <CyberCard className="max-w-md">
        <CyberCardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Mission cell is initializing. Return to Missions and start this operation again if the runtime does not receive a cell id.
          </p>
        </CyberCardContent>
      </CyberCard>
    );
  }

  if (!teamState) {
    return <div>Loading mission cell...</div>;
  }

  const suspectOptions = (teamState.accusationTargetPool || [{ id: 'hidden-hand', label: 'Unknown hidden hand' }]).map((suspect) => ({
    id: typeof suspect === 'string' ? suspect : suspect.id,
    label: typeof suspect === 'string' ? suspect : suspect.label || suspect.codename || suspect.name || suspect.id,
  }));

  return (
    <CyberCard className="lg:col-span-1">
      <CyberCardContent className="p-6 space-y-4">
        <h3 className="text-lg font-bold mb-4 flex items-center gap-2">
          <Users className="w-4 h-4" />
          Mission Cell | Phase: {teamState.phase}
        </h3>
        {/* Squad List */}
        <div className="space-y-3 max-h-48 overflow-y-auto">
          {teamState.players.map((player, index) => (
            <div key={player.id || `${player.username || 'player'}-${index}`} className="flex items-center gap-3 p-3 rounded-lg bg-gradient-to-r from-accent/30 to-accent/10 border border-accent/50">
              <div className={`w-3 h-3 rounded-full ${player.connected ? 'bg-green-400' : 'bg-red-400'}`} />
              <span className="font-semibold truncate">{player.displayName || player.name || player.username || player.id}</span>
              {player.operatorCodename && <span className="text-xs text-primary">{player.operatorCodename}</span>}
              <span className="text-xs font-mono">{player.role?.toUpperCase()}</span>
              {player.ready && <CheckCircle2 className="w-3 h-3 text-green-400" />}
              {player.contributionSummary && <span className="text-xs text-muted-foreground truncate">{player.contributionSummary}</span>}
            </div>
          ))}
        </div>
        {/* Evidence */}
        <div className="space-y-2">
          <div className="rounded-lg border border-border/50 bg-muted/20 p-3">
            <div className="text-xs font-mono uppercase text-muted-foreground">Trust State</div>
            <div className="mt-1 text-sm">{teamState.trust?.state || 'NEUTRAL'} ({teamState.trust?.currentTrust ?? 50}/100)</div>
            <p className="mt-1 text-xs text-muted-foreground">{teamState.trust?.gameplayEffect || 'Trust changes mission stability and accusation risk.'}</p>
          </div>
          {Object.entries(teamState.evidence?.items || {}).length > 0 ? (
            Object.entries(teamState.evidence?.items || {}).map(([type, count], index) => (
              <div key={`${type || 'evidence'}-${index}`} className="rounded-lg border border-emerald-400/40 bg-emerald-500/5 p-3">
                <div className="text-xs font-mono uppercase text-emerald-300">{type}</div>
                <p className="text-sm">{count} submitted</p>
                <p className="text-xs text-muted-foreground">{teamState.evidence?.whyItMatters || 'Evidence changes accusation and mission consequences.'}</p>
              </div>
            ))
          ) : (
            <div className="rounded-lg border border-warning/30 bg-warning/5 p-3">
              <div className="text-xs font-mono uppercase text-warning">Evidence Pending</div>
              <p className="text-xs text-muted-foreground">Submit evidence to unlock accusation and change mission consequences.</p>
            </div>
          )}
        </div>
        {/* Controls */}
        <div className="space-y-2 pt-2">
          <CyberButton className="w-full text-xs" variant={isReady ? "secondary" : "outline"} onClick={toggleReady} size="sm">
            {isReady ? 'Ready' : 'Mark Ready'}
          </CyberButton>
          {isLeader && teamState.phase === 'lobby' && (
            <CyberButton className="w-full text-xs" onClick={startTeam} size="sm">
              Start Operation
            </CyberButton>
          )}
          {teamState.evidenceCount >= 3 && (
            <select className="w-full p-2 text-xs border rounded" value={accusedId} onChange={(e) => setAccusedId(e.target.value)}>
              <option value="">Select Suspect</option>
              {suspectOptions.map((s, index) => <option key={`${s.id}-${index}`} value={s.id}>{s.label}</option>)}
            </select>
          )}
        </div>
        {/* Chat */}
        <div className="border-t pt-2">
          <div className="h-20 overflow-y-auto text-xs space-y-1 mb-1">
            {messages.slice(-5).map((msg, i) => (
              <div key={`${msg.timestamp || 'message'}-${msg.userId || 'anon'}-${i}`}><span className="font-mono text-muted-foreground">[recent]</span> {msg.text}</div>
            ))}
          </div>
          <div className="flex gap-1">
            <input className="flex-1 p-1 text-xs border rounded" ref={inputRef} placeholder="Cell comms..." onKeyPress={(e) => e.key === 'Enter' && sendMessage()} />
            <CyberButton size="sm" className="px-2 py-1 text-xs" onClick={sendMessage}>Send</CyberButton>
          </div>
        </div>
      </CyberCardContent>
    </CyberCard>
  );
};

export default MissionCellPanel;
