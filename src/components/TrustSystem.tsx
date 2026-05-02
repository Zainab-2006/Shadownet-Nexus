import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Heart, Skull, Shield } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { Character, TrustChoice } from '@/types';
import { useAudioContext } from '@/context/AudioProvider.hooks';

interface TrustMeterProps {
  operator: Character;
  trust: number;
}

/**
 * Visual trust meter for an operator
 */
export const TrustMeter: React.FC<TrustMeterProps> = ({ operator, trust }) => {
  const percentage = Math.max(0, Math.min(100, (trust + 100) / 2));
  const color = 
    trust < -50 ? '#ff3355' :
    trust < 0 ? '#ffaa00' :
    trust < 50 ? '#00ff88' :
    '#00f5ff';

  const icon = 
    trust < -30 ? <Skull className="w-4 h-4" /> :
    trust < 0 ? <Shield className="w-4 h-4" /> :
    <Heart className="w-4 h-4" />;

  return (
    <div className="trust-meter">
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-semibold">{operator.codename}</span>
        <div className="flex items-center gap-2" style={{ color }}>
          {icon}
          <span className="text-sm font-mono">{trust > 0 ? '+' : ''}{trust}</span>
        </div>
      </div>
      <div className="relative h-2 bg-background rounded-full overflow-hidden border border-border">
        <motion.div
          className="absolute inset-y-0 left-0 rounded-full"
          initial={{ width: 0 }}
          animate={{ width: `${percentage}%` }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
          style={{
            background: `linear-gradient(90deg, ${color}, ${color}80)`,
            boxShadow: `0 0 8px ${color}`,
          }}
        />
      </div>
      <div className="text-xs text-muted-foreground mt-1">
        {percentage.toFixed(0)}% Trust
      </div>
    </div>
  );
};

interface ChoiceButtonProps {
  choice: TrustChoice;
  onSelect: (choice: TrustChoice) => void;
  isLoading: boolean;
}

/**
 * Single interactive choice button with outcome preview
 */
export const ChoiceButton: React.FC<ChoiceButtonProps> = ({ choice, onSelect, isLoading }) => {
  const audio = useAudioContext();
  const [isHovered, setIsHovered] = useState(false);

  const outcomeIcon = 
    choice.outcome === 'success' ? '✓' :
    choice.outcome === 'fail' ? '✗' : '~';

  const outcomeColor =
    choice.outcome === 'success' ? 'text-green-500' :
    choice.outcome === 'fail' ? 'text-red-500' : 'text-amber-500';

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      onHoverStart={() => {
        setIsHovered(true);
        audio.playSound('hover', 0.6);
      }}
      onHoverEnd={() => setIsHovered(false)}
    >
      <CyberButton
        disabled={isLoading}
        onClick={() => {
          audio.playSound('select', 0.8);
          onSelect(choice);
        }}
        className="w-full justify-between ring-offset-1 group"
        variant={isHovered ? 'outline' : 'ghost'}
      >
        <span className="text-left flex-1">{choice.text}</span>
        <div className="flex items-center gap-2 ml-4">
          {choice.trustDelta !== 0 && (
            <span className={`text-xs font-mono ${
              choice.trustDelta > 0 ? 'text-green-400' : 'text-red-400'
            }`}>
              {choice.trustDelta > 0 ? '+' : ''}{choice.trustDelta}
            </span>
          )}
          <span className={`text-sm font-bold ${outcomeColor}`}>
            {outcomeIcon}
          </span>
        </div>
      </CyberButton>
    </motion.div>
  );
};

interface DecisionPanelProps {
  characterId: string;
  characterName: string;
  decision: string;
  choices: TrustChoice[];
  onChoiceSelected: (choice: TrustChoice) => void;
  isLoading: boolean;
}

/**
 * Full decision panel for mission choices
 */
export const DecisionPanel: React.FC<DecisionPanelProps> = ({
  characterId,
  characterName,
  decision,
  choices,
  onChoiceSelected,
  isLoading,
}) => {
  const audio = useAudioContext();

  return (
    <motion.div
      initial={{ opacity: 0, y: 30, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.4 }}
      className="bg-card border border-primary/30 rounded-lg p-6 backdrop-blur-sm"
    >
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-2 h-2 rounded-full bg-primary animate-pulse" />
          <h3 className="text-xs font-heading uppercase tracking-wider text-primary">
            {characterName} — Decision Point
          </h3>
        </div>
        <p className="text-sm text-foreground leading-relaxed">{decision}</p>
      </div>

      {/* Choices */}
      <div className="space-y-3">
        {choices.map((choice, idx) => (
          <ChoiceButton
            key={idx}
            choice={choice}
            onSelect={() => onChoiceSelected(choice)}
            isLoading={isLoading}
          />
        ))}
      </div>

      {/* Footer hint */}
      <div className="mt-4 pt-4 border-t border-border/50">
        <p className="text-xs text-muted-foreground">
          💡 Your choice will affect {characterName}'s trust level and mission outcomes.
        </p>
      </div>
    </motion.div>
  );
};

interface TrustNetworkProps {
  operatorTrusts: Record<string, number>;
  operatorNames: Record<string, string>;
}

/**
 * Visual network showing trust relationships between team members
 */
export const TrustNetwork: React.FC<TrustNetworkProps> = ({
  operatorTrusts,
  operatorNames,
}) => {
  const entries = Object.entries(operatorTrusts);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="space-y-3"
    >
      {entries.map(([opId, trustValue]) => (
        <TrustMeter
          key={opId}
          operator={{
            id: opId,
            codename: operatorNames[opId] || opId,
            name: '',
            alignment: 'hero',
            faction: '',
            tier: 'elite',
            role: '',
            specialty: '',
            personality: '',
            visualTheme: '',
            portraitUrl: '',
            fullImageUrl: '',
            bio: '',
            stats: { attack: 50, defense: 50, speed: 50, tech: 50 },
          }}
          trust={trustValue}
        />
      ))}
    </motion.div>
  );
};

interface OutcomeDisplayProps {
  outcome: 'success' | 'fail' | 'neutral';
  message: string;
  trustDelta: number;
  xpGained: number;
}

/**
 * Shows the result of a choice
 */
export const OutcomeDisplay: React.FC<OutcomeDisplayProps> = ({
  outcome,
  message,
  trustDelta,
  xpGained,
}) => {
  const audio = useAudioContext();
  const [hasPlayed, setHasPlayed] = React.useState(false);

  React.useEffect(() => {
    if (!hasPlayed) {
      if (outcome === 'success') {
        audio.playSound('success');
      } else if (outcome === 'fail') {
        audio.playSound('failure');
      } else {
        audio.playSound('hover');
      }
      setHasPlayed(true);
    }
  }, [outcome, audio, hasPlayed]);

  const bgColor =
    outcome === 'success' ? 'bg-green-950/20 border-green-500/30' :
    outcome === 'fail' ? 'bg-red-950/20 border-red-500/30' :
    'bg-amber-950/20 border-amber-500/30';

  const textColor =
    outcome === 'success' ? 'text-green-300' :
    outcome === 'fail' ? 'text-red-300' : 'text-amber-300';

  const icon =
    outcome === 'success' ? '✓' :
    outcome === 'fail' ? '✗' : '~';

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className={`border rounded-lg p-4 ${bgColor}`}
    >
      <div className="flex items-start gap-3 mb-3">
        <div className={`text-2xl font-bold ${textColor}`}>{icon}</div>
        <div className="flex-1">
          <h4 className={`text-sm font-semibold ${textColor}`}>
            {outcome === 'success' ? 'Mission Success!' :
             outcome === 'fail' ? 'Mission Failed!' : 'Neutral Outcome'}
          </h4>
          <p className="text-sm text-foreground mt-1">{message}</p>
        </div>
      </div>

      {/* Rewards */}
      <div className="flex gap-4 text-sm font-mono pt-3 border-t border-current/20">
        {trustDelta !== 0 && (
          <div className={trustDelta > 0 ? 'text-green-400' : 'text-red-400'}>
            Trust {trustDelta > 0 ? '+' : ''}{trustDelta}
          </div>
        )}
        {xpGained > 0 && (
          <div className="text-blue-400">+{xpGained.toLocaleString()} XP</div>
        )}
      </div>
    </motion.div>
  );
};
