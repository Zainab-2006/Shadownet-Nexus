import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronLeft, CheckCircle2, Shield } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import Navbar from '@/components/layout/Navbar';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useGame } from '@/context/GameContext';
import { useGetSceneQuery, useMakeDecisionMutation } from '@/api/storyApi';
import { toast } from 'sonner';

interface Scene {
  id: number;
  chapterId: number;
  sceneNumber: number;
  content: string;
  sceneType: 'NARRATIVE' | 'DIALOGUE' | 'CHOICE' | 'MISSION';
  characterSpeaking?: string;
  operatorPovVariants?: Record<string, string>;
  choices?: Array<{
    id: string | number;  // Fix TS: API returns string, local expects number
    text: string;
    trustImpact: number;
    evidenceCode?: string;
    nextSceneId?: number;
    missionUnlock?: string;
  }>;
}

interface DecisionResponseSummary {
  nextSceneId: number | null;
  trustDelta: number;
  evidenceGained: Array<{ evidenceCode: string; title: string; }>;
  unlockedMissions: string[];
  consequenceSummary?: string;
}

const StoryScene = () => {
  const { id: operatorId, sceneId } = useParams<{ id: string; sceneId: string }>();
  const sceneNum = parseInt(sceneId || '1', 10);
  const navigate = useNavigate();
  const { selectedOperator } = useGame();
  const { data: scene, isLoading } = useGetSceneQuery(sceneNum);
  const makeDecisionMutation = useMakeDecisionMutation();

  const [localChoices, setLocalChoices] = useState<Scene['choices']>([]);
  const [recentDecision, setRecentDecision] = useState<DecisionResponseSummary | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [povFallbackUsed, setPovFallbackUsed] = useState(false);

  // Fix infinite re-render: Move POV logic to useMemo, log warning once only
  const hasValidPovVariant = React.useMemo(() => {
    if (!scene?.operatorPovVariants || !selectedOperator?.id) return false;
    return !!scene.operatorPovVariants[selectedOperator.id];
  }, [scene?.operatorPovVariants, selectedOperator?.id]);

  React.useEffect(() => {
    if (!hasValidPovVariant && operatorId && !povFallbackUsed) {
      console.warn(`POV fallback for operator ${operatorId} - no variant found (using operatorId=${operatorId}, selectedOperatorId=${selectedOperator?.id})`);
      setPovFallbackUsed(true);
    }
  }, [hasValidPovVariant, operatorId, selectedOperator?.id, povFallbackUsed]);

  useEffect(() => {
    if (scene?.choices) {
      setLocalChoices(scene.choices);
    }
  }, [scene?.choices]);

  const handleChoice = React.useCallback(async (choiceId: number | string) => {
    const choiceIdNum = typeof choiceId === 'string' ? Number(choiceId) : choiceId;
    if (!scene?.id || isSubmitting || makeDecisionMutation.isPending) return;
    setIsSubmitting(true);

    makeDecisionMutation.mutate(
      { 
        sceneId: scene.id, 
        choiceId: choiceIdNum,
        operatorId: operatorId || selectedOperator?.id // Explicit operatorId POV
      },
      {
        onSuccess: (response) => {
          const summary: DecisionResponseSummary = {
            nextSceneId: response.nextSceneId,
            trustDelta: response.trustDelta || 0,
            evidenceGained: response.evidenceGained || [],
            unlockedMissions: response.unlockedMissionIds || [],
            consequenceSummary: response.consequenceSummary?.summary,
          };
          setRecentDecision(summary);

          toast.success(
            `Decision logged. Trust ${response.trustDelta > 0 ? '+' : ''}${response.trustDelta}. Evidence: ${summary.evidenceGained.length}.`
          );

          if (response.nextSceneId) {
            navigate(`/story/operator/${operatorId}/scene/${response.nextSceneId}`);
          } else if (response.unlockedMissionIds?.length) {
            toast.info(`Mission unlocked: ${response.unlockedMissionIds[0]}`);
            navigate('/missions');
          } else {
            navigate(`/story/operator/${operatorId}`);
          }
        },
        onError: (error) => {
          toast.error(`Decision failed: ${error}`);
          setIsSubmitting(false);
        },
        onSettled: () => {
          setIsSubmitting(false);
        },
      }
    );
  }, [scene?.id, isSubmitting, makeDecisionMutation, operatorId, selectedOperator?.id, navigate]);

  // Safe POV variant (no render side-effects)
  const povVariant = React.useMemo(() => {
    if (!selectedOperator || !scene?.operatorPovVariants) return '';
    return scene.operatorPovVariants[selectedOperator.id] || '';
  }, [scene?.operatorPovVariants, selectedOperator?.id]);

  if (isLoading || !selectedOperator || selectedOperator.id !== operatorId) {
    return (
      <PageTransition>
        <Navbar />
        <ParticleBackground />
        <div className="min-h-screen pt-24 flex items-center justify-center">
          Loading scene...
        </div>
      </PageTransition>
    );
  }

  if (!scene) {
    return (
      <PageTransition>
        <Navbar />
        <ParticleBackground />
        <div className="min-h-screen pt-24 flex items-center justify-center text-destructive">
          Scene not found.
        </div>
      </PageTransition>
    );
  }

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4 max-w-4xl mx-auto">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="mb-8 flex items-center gap-4"
          >
            <CyberButton variant="ghost" onClick={() => navigate(`/story/operator/${operatorId}`)}>
              <ChevronLeft className="w-5 h-5 mr-2" />
              Back to Operator Dossier
            </CyberButton>
            <CyberButton variant="ghost" onClick={() => navigate(`/story/operator/${operatorId}`)}>
              Timeline
            </CyberButton>
            {povFallbackUsed && (
              <div className="px-3 py-1 bg-warning/20 text-warning rounded-full text-xs font-mono">
                POV Fallback Active
              </div>
            )}
          </motion.div>

          <CyberCard variant="hero" className="mb-8">
            <CyberCardContent className="p-8">
              <div className="flex items-center gap-4 mb-6">
                <div className="text-2xl font-heading">
                  Chapter {scene.chapterId} • Scene {scene.sceneNumber}
                </div>
                {scene.characterSpeaking && (
                  <div className="px-4 py-2 bg-secondary/20 rounded-full text-sm font-mono">
                    {scene.characterSpeaking}
                  </div>
                )}
              </div>
              {povVariant && (
                <div className="italic text-primary mb-4 text-sm">
                  *{selectedOperator.codename}: {povVariant}*
                </div>
              )}
            </CyberCardContent>
          </CyberCard>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="prose prose-invert prose-lg max-w-none mb-12"
          >
            <div className="p-8 bg-gradient-to-b from-card/80 to-card rounded-2xl border backdrop-blur-xl">
              <p>{scene.content}</p>
            </div>
          </motion.div>

          <AnimatePresence>
            {recentDecision && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                className="mb-8 p-6 bg-gradient-to-r from-success/20 to-primary/20 border border-success/30 rounded-2xl"
              >
                <h4 className="font-heading text-lg mb-3 flex items-center gap-2">
                  <CheckCircle2 className="w-5 h-5 text-success" />
                  Decision Applied
                </h4>
                <div className="space-y-2 text-sm">
                  <p>Trust {recentDecision.trustDelta > 0 ? '+' : ''}{recentDecision.trustDelta}</p>
                  {recentDecision.evidenceGained.length > 0 && (
                    <p>Evidence: {recentDecision.evidenceGained.map(e => e.title).join(', ')}</p>
                  )}
                  {recentDecision.unlockedMissions.length > 0 && (
                    <p>Missions: {recentDecision.unlockedMissions.join(', ')}</p>
                  )}
                  {recentDecision.consequenceSummary && (
                    <p className="italic">"{recentDecision.consequenceSummary}"</p>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {scene.sceneType === 'CHOICE' && localChoices.length > 0 ? (
            <div className="space-y-4">
              <h3 className="font-heading text-xl mb-6 text-center">Your Decision</h3>
              <div className="grid md:grid-cols-2 gap-4">
                {localChoices.map((choice, index) => (
                  <motion.div
                    key={choice.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                  >
                    <CyberCard variant={isSubmitting || makeDecisionMutation.isPending ? 'default' : 'interactive'} className={isSubmitting || makeDecisionMutation.isPending ? 'opacity-50 pointer-events-none' : ''}>
                      <CyberCardContent className="p-6 cursor-pointer" onClick={() => handleChoice(Number(choice.id))}>
                        <div className="flex items-start justify-between mb-3">
                          <span className={`px-3 py-1 rounded-full text-xs font-mono ${
                            choice.trustImpact >= 5 ? 'bg-success/20 text-success border-success/30' :
                            choice.trustImpact <= -5 ? 'bg-destructive/20 text-destructive border-destructive/30' :
                            'bg-secondary/20 text-secondary border-secondary/30'
                          }`}>
                            {choice.trustImpact >= 0 ? '+' : ''}{choice.trustImpact} Trust
                          </span>
                          {choice.evidenceCode && (
                            <Shield className="w-4 h-4 text-primary" />
                          )}
                        </div>
                        <p className="text-lg font-medium mb-4">{choice.text}</p>
                        {choice.missionUnlock && (
                          <div className="text-xs text-primary font-mono bg-primary/10 px-2 py-1 rounded">
                            Unlocks: {choice.missionUnlock}
                          </div>
                        )}
                      </CyberCardContent>
                    </CyberCard>
                  </motion.div>
                ))}
              </div>
            </div>
          ) : scene.sceneType === 'MISSION' ? (
            <div className="text-center">
              <CyberCard variant="hero" className="max-w-2xl mx-auto">
                <CyberCardContent className="p-12">
                  <CheckCircle2 className="w-16 h-16 text-success mx-auto mb-6" />
                  <h2 className="font-heading text-2xl mb-4">Mission Debrief Complete</h2>
                  <p className="text-muted-foreground mb-8">
                    Evidence logged. Trust updated. Progress synced to your profile.
                  </p>
                  <div className="flex gap-4 justify-center">
                    <CyberButton onClick={() => navigate('/story')}>
                      Continue Story
                    </CyberButton>
                    <CyberButton variant="outline" onClick={() => navigate('/missions')}>
                      Active Missions
                    </CyberButton>
                  </div>
                </CyberCardContent>
              </CyberCard>
            </div>
          ) : (
            <div className="text-center py-12">
              <CyberButton size="lg" onClick={() => navigate('/story')}>
                Continue to Next Chapter
              </CyberButton>
            </div>
          )}

          {scene.sceneType !== 'CHOICE' && !isLoading && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center mt-12 p-6 bg-secondary/20 rounded-xl"
            >
              <p className="text-muted-foreground mb-4">Scene complete. Ready for next.</p>
              <CyberButton variant="outline" onClick={() => navigate('/story')}>
                Story Timeline
              </CyberButton>
            </motion.div>
          )}
        </div>
      </main>
    </PageTransition>
  );
};

export default StoryScene;
