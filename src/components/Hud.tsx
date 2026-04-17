import { useGame } from '@/context/GameContext';
import { useLocation } from 'react-router-dom';
import { Trophy, User, Zap, Users, Radio } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { CyberCard } from '@/components/ui/cyber-card';

export const Hud = () => {
  const { gameState: state, selectedOperator } = useGame();
  const { pathname } = useLocation();

  if (!state) return null;

  const level = Math.floor(state.score / 1000) + 1;
  const mode =
    pathname.startsWith('/solo')
      ? {
          label: 'Solo',
          cue: 'Ranked CTF challenges with training after 3 fails. Proves pattern reading under pressure.',
        }
      : pathname.startsWith('/missions/')
        ? {
            label: 'Missions',
            cue: 'Trust gameplay: briefing, evidence, accusation, consequences.',
          }
        : pathname.startsWith('/missions')
          ? {
              label: 'Missions',
              cue: 'Trust gameplay: briefing, evidence, accusation, consequences.',
            }
          : pathname.startsWith('/story/operator')
            ? {
                label: 'Story',
                cue: 'Operator dossier: background, memories, choices that shape missions and trust.',
              }
            : pathname.startsWith('/story')
              ? {
                  label: 'Story',
                  cue: 'Operator dossier: background, memories, choices that shape missions and trust.',
                }
              : pathname.startsWith('/operators')
                ? {
                    label: 'Operators',
                    cue: 'Choose operator → direct to their Story route.',
                  }
                : {
                    label: 'Nexus',
                    cue: selectedOperator ? 'Solo | Missions | Story' : 'Select operator to unlock.',
                  };

  return (
    <div className="fixed top-20 right-4 z-40 md:right-6 space-y-2 pointer-events-none">
      <CyberCard variant="glass" className="p-3 backdrop-blur-sm bg-background/90 border-border/50 w-72 max-w-sm">
        <div className="space-y-3">
          <div className="rounded border border-primary/20 bg-primary/10 p-2">
            <div className="mb-1 flex items-center gap-2 text-[11px] font-mono uppercase tracking-wider text-primary">
              <Radio className="h-3.5 w-3.5" />
              {mode.label}
            </div>
            <p className="text-xs leading-5 text-muted-foreground">{mode.cue}</p>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Trophy className="w-4 h-4 text-primary" />
              <span className="text-sm font-mono text-muted-foreground">Level {level}</span>
            </div>
            <Badge variant="secondary" className="text-xs">
              {state.score.toLocaleString()} XP
            </Badge>
          </div>

          {selectedOperator && (
            <div className="flex items-center gap-3 p-2 bg-secondary/20 rounded">
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
                <User className="w-4 h-4 text-primary" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold truncate">{selectedOperator?.codename || 'N/A'}</p>
                <p className="text-xs text-muted-foreground">
                  {selectedOperator?.role ? selectedOperator.role.toLowerCase() : 'agent'}
                </p>
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-2 text-xs">
            <div className="flex items-center gap-1 p-2 bg-muted/50 rounded">
              <Zap className="w-3 h-3" />
              <span>{state.ctfSolves.length} solves</span>
            </div>
            <div className="flex items-center gap-1 p-2 bg-muted/50 rounded">
              <Users className="w-3 h-3" />
              <span>{state.onlineUsers || 0} online</span>
            </div>
          </div>
        </div>
      </CyberCard>
    </div>
  );
};
