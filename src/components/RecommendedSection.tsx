import React from 'react';
import { Star, Target, Zap } from 'lucide-react';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import { CyberButton } from '@/components/ui/cyber-button';
import { useGame } from '@/context/GameContext';
import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/apiClient';
import { Challenge } from '@/types';
import { motion } from 'framer-motion';

interface RecommendedSectionProps {
  onChallengeSelect: (challengeId: string) => void;
}

const RecommendedSection: React.FC<RecommendedSectionProps> = ({ onChallengeSelect }) => {
  const { state, isAuthenticated } = useGame();

  const { data: recommended, isLoading } = useQuery({
    queryKey: ['recommendedChallenges'],
    queryFn: () => apiFetch<Challenge[]>('/challenges/recommended'),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  if (!isAuthenticated || !recommended || (Array.isArray(recommended) && recommended.length === 0)) {
    return null;
  }

  const challenges: Challenge[] = Array.isArray(recommended) ? recommended : [];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="mb-12"
    >
      <CyberCard variant="glow">
        <CyberCardContent className="p-6">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-2 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-xl">
              <Target className="w-6 h-6 text-primary" />
            </div>
            <div>
              <h2 className="font-heading text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                🎯 Recommended for You
              </h2>
              <p className="text-sm text-muted-foreground">
                Challenges matched to your skill level ({state.score ? Math.max(1, Math.floor(state.score / 500) + 1) : 1})
              </p>
            </div>
          </div>

          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <Zap className="w-6 h-6 animate-spin text-primary mr-2" />
              <span>Finding perfect challenges...</span>
            </div>
          ) : challenges.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {challenges.slice(0, 3).map((challenge) => (
                <CyberButton
                  key={challenge.id}
                  variant="ghost"
                  className="h-auto p-4 justify-start hover:shadow-glow-primary"
                  onClick={() => onChallengeSelect(challenge.id)}
                >
                  <div className="flex flex-col items-start w-full">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-xs font-mono uppercase px-2 py-1 rounded-full bg-primary/20 text-primary border border-primary/30">
                        {challenge.category}
                      </span>
                      <span className={`text-xs uppercase px-2 py-1 rounded-full font-bold ${
                        challenge.difficulty === 'easy' ? 'bg-success/20 text-success' :
                        challenge.difficulty === 'medium' ? 'bg-warning/20 text-warning' :
                        challenge.difficulty === 'hard' ? 'bg-destructive/20 text-destructive' : 
                        'bg-secondary/20 text-secondary'
                      }`}>
                        {challenge.difficulty}
                      </span>
                    </div>
                    <h3 className="font-heading text-lg font-bold text-left">{challenge.name}</h3>
                    <p className="text-sm text-muted-foreground text-left mt-1 line-clamp-2">
                      {challenge.description}
                    </p>
                    <div className="flex items-center gap-2 mt-3 w-full justify-between">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 text-primary fill-primary" />
                        <span className="font-mono font-bold text-primary">{challenge.points} pts</span>
                      </div>
                      <span className="text-xs text-muted-foreground">Perfect skill match</span>
                    </div>
                  </div>
                </CyberButton>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-muted-foreground">
              <Star className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>Complete more challenges to unlock personalized recommendations</p>
            </div>
          )}
        </CyberCardContent>
      </CyberCard>
    </motion.div>
  );
};

export default RecommendedSection;
