import { forwardRef, useState, useMemo, useEffect, useCallback } from 'react';
import RecommendedSection from '@/components/RecommendedSection';

import { Search, Flag, CheckCircle2, Clock, Users, Trophy, X, Send, AlertCircle, ChevronDown } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { useNarrator } from '@/context/NarratorContext';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import { Input } from '@/components/ui/input';
import Navbar from '@/components/layout/Navbar';
import { PlayerDirective } from '@/components/PlayerDirective';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useGame } from '@/context/GameContext';
import { useAuthentication } from '@/context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/apiClient';
import { usePuzzleSession, useSubmitStage, useGetHint } from '@/api/puzzleApi';
import { Challenge } from '@/api/challengeApi';
import { motion, AnimatePresence } from 'framer-motion';

const categoryColors: Record<string, string> = {
  web: 'bg-primary/20 text-primary border-primary/30',
  crypto: 'bg-tertiary/20 text-tertiary border-tertiary/30',
  pwn: 'bg-destructive/20 text-destructive border-destructive/30',
  forensics: 'bg-warning/20 text-warning border-warning/30',
  rev: 'bg-secondary/20 text-secondary border-secondary/30',
  osint: 'bg-success/20 text-success border-success/30',
  misc: 'bg-muted text-muted-foreground border-border',
};

const difficultyColors: Record<string, string> = {
  easy: 'text-success',
  medium: 'text-warning',
  hard: 'text-destructive',
  insane: 'text-secondary',
};

type ChallengeStageView = {
  briefing?: string;
  objective?: string;
  evidence?: string;
  submitFormat?: string;
  learningContent?: string;
  answer?: string;
  solution?: string;
  finalAnswer?: string;
};

type SoloAttemptMode = 'ranked' | 'teaching' | 'coaching';

const parseChallengeStages = (stages: unknown): ChallengeStageView[] => {
  if (Array.isArray(stages)) return stages as ChallengeStageView[];
  if (typeof stages !== 'string' || !stages.trim()) return [];

  try {
    const parsed = JSON.parse(stages);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const ChallengeCard = forwardRef<HTMLDivElement, { challenge: Challenge; onClick: () => void }>(({ challenge, onClick }, ref) => {
  return (
    <motion.div
      ref={ref}
      layout
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      whileHover={{ scale: 1.02 }}
      transition={{ duration: 0.2 }}
    >
      <CyberCard
        variant={challenge.isSolved ? 'default' : 'interactive'}
        className={`cursor-pointer ${challenge.isSolved ? 'opacity-60' : ''}`}
        onClick={onClick}
      >
        <CyberCardContent className="p-5">
          <div className="flex items-start justify-between mb-3">
            <div className="flex items-center gap-2">
              <span className={`text-xs font-mono uppercase px-2 py-1 rounded border ${categoryColors[challenge.category]}`}>
                {challenge.category}
              </span>
              <span className={`text-xs font-heading uppercase ${difficultyColors[challenge.difficulty]}`}>
                {challenge.difficulty}
              </span>
            </div>
            <div className="flex items-center gap-1">
              <Trophy className="w-4 h-4 text-primary" />
              <span className="font-mono text-sm font-bold text-primary">{challenge.points}</span>
            </div>
          </div>
          
          <h3 className="font-heading text-lg font-bold mb-2 flex items-center gap-2">
            {challenge.name}
            {challenge.isSolved && <CheckCircle2 className="w-4 h-4 text-success" />}
          </h3>
          
          <p className="text-sm text-muted-foreground mb-4 line-clamp-2">
            {challenge.description}
          </p>
          
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <Users className="w-3 h-3" />
              {challenge.solves} solves
            </span>
            <span>by {challenge.author}</span>
          </div>
        </CyberCardContent>
      </CyberCard>
    </motion.div>
  );
});
ChallengeCard.displayName = 'ChallengeCard';

interface ChallengeModalProps {
  challenge: Challenge | null;
  onClose: () => void;
  onSolve: (challengeId: string) => void;
  showLearning: boolean;
  onToggleLearning: () => void;
  openNarrator: (payload: { event: string; title: string; message: string; dismissible: boolean }) => void;
}

type PuzzleChallengePayload = {
  stages?: unknown;
};

const getPuzzleChallengeStages = (challenge: unknown): unknown => {
  if (typeof challenge === 'object' && challenge !== null && 'stages' in challenge) {
    return (challenge as PuzzleChallengePayload).stages;
  }

  return undefined;
};

const ChallengeModal = ({
  challenge,
  onClose,
  onSolve,
  showLearning,
  onToggleLearning,
  openNarrator,
}: ChallengeModalProps) => {
  const [flag, setFlag] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'correct' | 'wrong' | null>(null);
  const [timeElapsed, setTimeElapsed] = useState(0);
  const [timerId, setTimerId] = useState<NodeJS.Timeout | null>(null);
  const [currentStage, setCurrentStage] = useState(1);
  const [hintsUsed, setHintsUsed] = useState(0);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [wrongAttempts, setWrongAttempts] = useState(0);
  const [trainingMode, setTrainingMode] = useState(false);
  const [rankedEligible, setRankedEligible] = useState(true);
  const [solutionRevealed, setSolutionRevealed] = useState(false);
  const [narratorOpen, setNarratorOpen] = useState(false);
  const [soloAttemptMode, setSoloAttemptMode] = useState<SoloAttemptMode>('ranked');

  const { data: session, isLoading: sessionLoading } = usePuzzleSession(challenge?.id || '');
  const submitStageMut = useSubmitStage();
  const getHintMut = useGetHint();
  const activeStages = parseChallengeStages(getPuzzleChallengeStages(session?.challenge) ?? challenge?.stages);
  const stageCount = Math.max(activeStages.length, 1);
  const activeStage = activeStages[currentStage - 1];
  const isCaesarCipher = challenge?.id === 'crypto-001' || challenge?.name?.toLowerCase() === 'caesar cipher';
  const objectiveText = activeStage?.objective || (isCaesarCipher ? 'Decode the Caesar-rotated flag.' : 'Recover the signal requested by the briefing.');
  const evidenceText = activeStage?.evidence || (isCaesarCipher ? 'synt{pnrfne}' : '');
  const submitFormatText = activeStage?.submitFormat || (isCaesarCipher ? 'flag{decoded_plaintext}' : 'flag{...}');
  const teachingText = activeStage?.learningContent || (isCaesarCipher
    ? 'Try every Caesar rotation until the ciphertext becomes a readable flag. The answer goes in the submit box exactly as flag{...}.'
    : 'Real-world relevance and prevention guidance will appear here post-solve.');
  const revealedAnswer = activeStage?.answer || activeStage?.solution || activeStage?.finalAnswer || (isCaesarCipher ? 'flag{caesar}' : '');

  const enterTeachingMode = () => {
    setSoloAttemptMode('teaching');
    setTrainingMode(true);
    setRankedEligible(false);
    setNarratorOpen(true);
    openNarrator({
      event: 'SOLO_3_FAILS',
      title: 'Teaching Mode',
      message: 'Three failed attempts logged. Ranked scoring is now disabled for this challenge. I will walk you through the concept and answer path before you continue.',
      dismissible: true
    });
  };

  const enterCoachingMode = () => {
    setSoloAttemptMode('coaching');
    setTrainingMode(true);
    setRankedEligible(false);
    setNarratorOpen(true);
    setSolutionRevealed(true);
    openNarrator({
      event: 'SOLO_3_FAILS',
      title: 'Coaching Mode',
      message: 'Additional misses detected. I am switching from brief teaching to detailed coaching. This path reveals the solve process and remains excluded from leaderboard scoring.',
      dismissible: true
    });
  };

  const startTimer = useCallback(() => {
    if (timerId) clearInterval(timerId);
    const id = setInterval(() => {
      setTimeElapsed((prev) => prev + 1);
    }, 1000);
    setTimerId(id);
  }, [timerId]);

  const stopTimer = () => {
    if (timerId) {
      clearInterval(timerId);
      setTimerId(null);
    }
  };

  useEffect(() => {
    if (session && !sessionLoading) {
      setSessionId(session.id);
      setCurrentStage(session.currentStage);
      setHintsUsed(session.hintsUsed);
      if (!timerId) startTimer();
    }
  }, [session, sessionLoading]);

  const handleRevealAnswer = () => {
    setTrainingMode(true);
    setRankedEligible(false);
    setSolutionRevealed(true);
    if (soloAttemptMode === 'ranked') {
      setSoloAttemptMode('teaching');
    }
    openNarrator({
      event: 'SOLO_3_FAILS',
      title: 'Answer Path Revealed',
      message: 'The answer path is now visible. This challenge remains a training solve and will not award leaderboard points.',
      dismissible: true
    });
  };

  const handleSubmit = () => {
    if (!flag.trim() || !sessionId) return;

    setSubmitting(true);
    setResult(null);

    submitStageMut.mutate(
      { 
        sessionId: sessionId!,
        stageNumber: currentStage, 
        flag: flag.trim(),
        trainingMode,
        rankedEligible,
        solutionRevealed,
        narratorTriggered: narratorOpen || trainingMode
      },
      {
        onSuccess: (result) => {
          if (result.correct) {
            setResult('correct');
            setWrongAttempts(0);
            setFlag('');
            if (result.showExplanation || typeof result.awardedPoints !== 'undefined' || currentStage >= stageCount) {
              onSolve(challenge!.id);
              stopTimer();
            } else {
              setCurrentStage(result.nextStage || currentStage + 1);
            }
          } else {
            const newAttempts = wrongAttempts + 1;
            setWrongAttempts(newAttempts);
            setResult('wrong');
            if (newAttempts >= 5) {
              enterCoachingMode();
            } else if (!trainingMode && newAttempts >= 3) {
              enterTeachingMode();
            }
          }
        },
        onError: () => {
          setResult('wrong');
          setWrongAttempts((prev) => {
            const next = prev + 1;
            if (next >= 5) {
              enterCoachingMode();
            } else if (!trainingMode && next >= 3) {
              enterTeachingMode();
            }
            return next;
          });
        },
        onSettled: () => {
          setSubmitting(false);
        },
      }
    );
  };

  const handleGetHint = () => {
    if (!sessionId) return;
    getHintMut.mutate(sessionId, {
      onSuccess: () => setHintsUsed((prev) => prev + 1),
    });
  };

  if (!challenge) return null;

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-xl"
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.9, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.9, y: 20 }}
        className="w-full max-w-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <CyberCard variant="hero" className="overflow-hidden">
          <div className="p-6 border-b border-border">
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <span className={`text-xs font-mono uppercase px-2 py-1 rounded border ${categoryColors[challenge.category]}`}>
                    {challenge.category}
                  </span>
                  <span className={`text-xs font-heading uppercase ${difficultyColors[challenge.difficulty]}`}>
                    {challenge.difficulty}
                  </span>
                </div>
                <h2 className="font-heading text-2xl font-bold">{challenge.name}</h2>
              </div>
              <button onClick={onClose} className="p-2 rounded-full hover:bg-accent transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Trophy className="w-4 h-4 text-primary" />
                  <span className="font-mono font-bold text-primary">{challenge.points} pts</span>
                </span>
                <span className="flex items-center gap-1">
                  <Users className="w-4 h-4" />
                  {challenge.solves} solves
                </span>
              </div>
              <span className="text-xs text-muted-foreground">by {challenge.author}</span>
            </div>

            <div className="space-y-4 mb-6">
              {sessionLoading ? (
                <div>Loading puzzle...</div>
              ) : session && activeStage ? (
                <>
                  <div className="space-y-4 rounded-2xl border border-cyan-400/10 bg-black/30 p-4">
                    <div>
                  <h4 className="font-heading text-primary mb-2">Challenge Briefing</h4>
                      <p className="text-sm">{activeStage.briefing || challenge.description}</p>
                    </div>
                    <div className="grid gap-3 text-sm md:grid-cols-3">
                      <div className="rounded-xl border border-cyan-400/10 bg-cyan-400/[0.05] p-3">
                        <div className="mb-1 text-[10px] font-mono uppercase tracking-[0.18em] text-cyan-300">Objective</div>
                        <p className="text-muted-foreground">{objectiveText}</p>
                      </div>
                      <div className="rounded-xl border border-amber-400/15 bg-amber-400/[0.05] p-3">
                        <div className="mb-1 text-[10px] font-mono uppercase tracking-[0.18em] text-amber-300">Evidence</div>
                        <code className="font-mono text-amber-100">{evidenceText || 'Check the briefing or attachment.'}</code>
                      </div>
                      <div className="rounded-xl border border-emerald-400/15 bg-emerald-400/[0.05] p-3">
                        <div className="mb-1 text-[10px] font-mono uppercase tracking-[0.18em] text-emerald-300">Submit Format</div>
                        <code className="font-mono text-emerald-100">{submitFormatText}</code>
                      </div>
                    </div>
                  </div>
                  <div className="space-y-2">
                    <h4 className="font-heading text-lg flex items-center gap-2 cursor-pointer" onClick={onToggleLearning}>
                      Teaching Notes {showLearning ? <ChevronDown className="rotate-180" /> : <ChevronDown />}
                    </h4>
                    {(showLearning || trainingMode) && (
                      <div className="space-y-3 p-4 bg-secondary/20 rounded-lg text-sm">
                        <p>{teachingText}</p>
                        {soloAttemptMode !== 'ranked' && (
                          <div className="rounded-lg border border-warning/30 bg-warning/10 p-3">
                            <div className="mb-1 font-heading text-warning">
                              {soloAttemptMode === 'coaching' ? 'Coaching breakdown' : 'Teaching path'}
                            </div>
                            <p className="text-muted-foreground">
                              Start from the evidence, identify the category pattern, then transform the signal until it matches the required submit format.
                            </p>
                            {solutionRevealed && (
                              <p className="mt-2 font-mono text-primary">
                                Answer: {revealedAnswer || 'No plaintext answer is exposed by this challenge payload. Follow the concept path above and request hints.'}
                              </p>
                            )}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="prose prose-invert prose-sm max-w-none">
                  <p className="text-foreground">{challenge.description}</p>
                </div>
              )}
            </div>

            <div className="flex items-center gap-4 mb-4 p-3 bg-secondary/10 rounded-lg">
              <Clock className="w-4 h-4" />
              <span className="font-mono">{Math.floor(timeElapsed / 60)}:{(timeElapsed % 60).toString().padStart(2, '0')}</span>
              <span>Stage {currentStage} / {stageCount}</span>
              <span className="text-warning">H{hintsUsed}</span>
            </div>

            <div className="space-y-4">
              <h4 className="text-sm font-heading uppercase tracking-wider text-muted-foreground">
                Submit Signal
              </h4>
              <div className="space-y-2">
                <div className="flex gap-2">
                  <Input
                    placeholder="flag{...}"
                    value={flag}
                    onChange={(e) => setFlag(e.target.value)}
                    className="font-mono bg-card border-border"
                    disabled={submitting}
                  />
                  <CyberButton
                    variant="primary"
                    onClick={handleSubmit}
                    disabled={submitting || !flag.trim()}
                  >
                    {submitting ? (
                      <Clock className="w-4 h-4 animate-spin" />
                    ) : (
                      <>
                        <Send className="w-4 h-4 mr-2" />
                        Submit Stage {currentStage}
                      </>
                    )}
                  </CyberButton>
                </div>
                <CyberButton
                  variant="outline"
                  onClick={handleGetHint}
                  disabled={!sessionId || submitting || hintsUsed >= 3}
                  size="sm"
                >
                  Request Hint ({hintsUsed}/3)
                </CyberButton>
                <div className={`inline-flex rounded border px-2 py-1 text-xs font-mono ${
                  rankedEligible ? 'border-success/30 text-success' : 'border-warning/30 text-warning'
                }`}>
                  {soloAttemptMode === 'ranked' && 'Ranked Attempt'}
                  {soloAttemptMode === 'teaching' && 'Teaching Mode - not counted'}
                  {soloAttemptMode === 'coaching' && 'Coaching Mode - answer path revealed, not counted'}
                </div>
                {trainingMode && (
                  <CyberButton
                    variant="ghost"
                    onClick={handleRevealAnswer}
                    size="sm"
                  >
                    Reveal Answer Path
                  </CyberButton>
                )}
              </div>

              {result && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`flex items-center gap-2 p-3 rounded-lg ${
                    result === 'correct'
                      ? 'bg-success/20 text-success border-success/30'
                      : 'bg-destructive/20 text-destructive border-destructive/30'
                  }`}
                >
                  {result === 'correct' ? (
                    <>
                      <CheckCircle2 className="w-5 h-5" />
                      <span className="font-heading">Pattern Confirmed</span>
                    </>
                  ) : (
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <AlertCircle className="w-5 h-5" />
                        <span className="font-heading">Signal Rejected</span>
                      </div>
                      <p className="text-sm">
                        Attempts: {wrongAttempts}/3. {wrongAttempts >= 5 ? 'Coaching mode active.' : wrongAttempts >= 3 ? 'Teaching mode active.' : 'Review evidence and format.'}
                      </p>
                    </div>
                  )}
                </motion.div>
              )}
            </div>
          </div>
        </CyberCard>
      </motion.div>
    </motion.div>
  );
};

const CTF = () => {
  const game = useGame();
  const { gameState: state, progression, refreshProgression, refreshUserData } = game;
  const { token } = useAuthentication();
  const { openNarrator } = useNarrator();
  const [category, setCategory] = useState<string>('all');
  const [difficulty, setDifficulty] = useState<string>('all');
  const [search, setSearch] = useState('');
  const [selectedChallenge, setSelectedChallenge] = useState<Challenge | null>(null);
  const [puzzleError, setPuzzleError] = useState('');
  const [showLearning, setShowLearning] = useState(false);
  const [wrongAttempts, setWrongAttempts] = useState(0);
  const [trainingMode, setTrainingMode] = useState(false);
  const [rankedEligible, setRankedEligible] = useState(true);
  const [narratorOpen, setNarratorOpen] = useState(false);

  const handleTrainingOverride = () => {
    setTrainingMode(true);
    setRankedEligible(false);
    setNarratorOpen(true);
    openNarrator({
      event: 'SOLO_3_FAILS',
      title: 'Training Override',
      message: 'You have reached the coaching threshold. This solve will no longer count toward ranked progression. Teaching mode active.',
      dismissible: true
    });
  };

  const { data: serverChallenges = [], isLoading: challengesLoading } = useQuery({
    queryKey: ['challenges'],
    queryFn: () => apiFetch<Challenge[]>('/challenges'),
  });

  const normalizeChallenge = (serverCh: Challenge): Challenge => ({
    id: serverCh.id || serverCh._id || `fallback-${Math.random()}`,
    name: serverCh.name || serverCh.title || 'Unknown Challenge',
    title: serverCh.title || serverCh.name || 'Unknown Challenge',
    category: serverCh.category || 'misc',
    difficulty: serverCh.difficulty || 'easy',
    points: serverCh.points || serverCh.value || 100,
    description: serverCh.description || 'No description available.',
    author: serverCh.author || 'NEXUS Team',
    solves: serverCh.solves || serverCh.solveCount || 0,
    isSolved: Boolean(serverCh.isSolved || serverCh.solved),
    solved: Boolean(serverCh.isSolved || serverCh.solved),
    attachments: serverCh.attachments || serverCh.files || [],
    stages: parseChallengeStages(serverCh.stages),
  } as Challenge);

  const challengeList = Array.isArray(serverChallenges) ? serverChallenges.map(normalizeChallenge) : [];

  const categories = ['all', 'web', 'crypto', 'pwn', 'forensics', 'rev', 'osint', 'misc'];
  const difficulties = ['all', 'easy', 'medium', 'hard', 'insane'];

  const solvedChallenges = useMemo(() => state.ctfSolves, [state.ctfSolves]);
  const getChallengeSolved = (challengeId: string) => solvedChallenges.includes(challengeId);

  const filteredChallenges = challengeList.map(ch => ({
    ...ch,
    isSolved: getChallengeSolved(ch.id)
  })).filter((ch) => {
    if (category !== 'all' && ch.category !== category) return false;
    if (difficulty !== 'all' && ch.difficulty !== difficulty) return false;
    if (search && !ch.name.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });
  
  const earnedPoints = challengeList
    .filter(ch => solvedChallenges.includes(ch.id))
    .reduce((sum, ch) => sum + ch.points, 0);
  const totalPoints = challengeList.reduce((sum, ch) => sum + ch.points, 0);

  const handleSolve = async (_challengeId: string) => {
    await Promise.allSettled([refreshProgression(), refreshUserData()]);
  };

  const toggleLearning = () => setShowLearning(!showLearning);
  const requireAuthenticatedPuzzleAccess = (challenge?: Challenge | null) => {
    setPuzzleError('');
    if (token) {
      if (challenge) {
        setSelectedChallenge(challenge);
      }
      return true;
    }

    setSelectedChallenge(null);
    setPuzzleError('Login required to enter the simulation chamber. Sign in from the navbar to save progress.');
    return false;
  };

  const handleStartPuzzle = () => {
    setPuzzleError('');
    const firstUnsolved = challengeList.find(ch => !getChallengeSolved(ch.id));
    if (firstUnsolved) {
      requireAuthenticatedPuzzleAccess(firstUnsolved);
    } else {
      setPuzzleError('All challenges completed!');
    }
  };

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      
      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4">
          <RecommendedSection onChallengeSelect={(id) => requireAuthenticatedPuzzleAccess(challengeList.find(ch => ch.id === id) || null)} />
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-12"
          >
            <div className="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-6 mb-8">
              <div>
                <h1 className="font-heading text-4xl md:text-5xl font-bold mb-4">
                  <span className="text-foreground">Solo</span>
                  <span className="text-primary ml-3">Operations</span>
                </h1>
                <p className="text-lg text-muted-foreground max-w-2xl">
                  Ranked CTF challenges measure clean solves. After three misses, narrator training opens and any completion from that path is excluded from leaderboard scoring.
                </p>
              </div>
              
              <CyberCard variant="glow" className="p-4">
                <div className="flex items-center gap-6">
                  <div>
                    <span className="text-xs uppercase tracking-wider text-muted-foreground">Level</span>
                    <div className="font-heading text-2xl font-bold text-primary">
                      {progression?.currentLevel || 1}
                    </div>
                  </div>
                  <div>
                    <span className="text-xs uppercase tracking-wider text-muted-foreground">Points</span>
                    <div className="font-heading text-2xl font-bold text-primary">
                      {progression?.totalPoints || 0}
                    </div>
                  </div>
                  <div>
                    <span className="text-xs uppercase tracking-wider text-muted-foreground">Solved</span>
                    <div className="font-heading text-2xl font-bold">
                      {progression?.challengesSolved || 0}
                    </div>
                  </div>
                </div>
              </CyberCard>
            </div>
            
            <PlayerDirective
              mode="Solo Operations"
              directive="Pick one challenge, inspect the evidence, decode the signal, and submit the exact flag format."
              why="Solo rank depends on clean competitive solves. Training mode teaches the pattern without awarding leaderboard points."
              next="Pick a challenge. Three misses trigger narrator training for that solve."
            />

            <div className="mb-8 grid gap-4 md:grid-cols-3">
              <CyberCard><CyberCardContent className="p-4 pt-4"><h3 className="font-heading text-sm text-primary mb-2">Ranked Solve</h3><p className="text-sm text-muted-foreground">Solve cleanly before training triggers. Correct flags award leaderboard points; wrong flags do not.</p></CyberCardContent></CyberCard>
              <CyberCard><CyberCardContent className="p-4 pt-4"><h3 className="font-heading text-sm text-primary mb-2">Teaching Mode</h3><p className="text-sm text-muted-foreground">After three wrong attempts, narrator guidance opens and the solve becomes non-ranked.</p></CyberCardContent></CyberCard>
              <CyberCard><CyberCardContent className="p-4 pt-4"><h3 className="font-heading text-sm text-primary mb-2">Coaching Mode</h3><p className="text-sm text-muted-foreground">After continued misses, narrator reveals the answer path and teaches the concept in detail.</p></CyberCardContent></CyberCard>
            </div>

            <div className="flex items-center gap-3 mb-8">
              <CyberButton variant="secondary" onClick={handleStartPuzzle}>
                Begin Challenge
              </CyberButton>
              {puzzleError && <span className="text-sm text-destructive">{puzzleError}</span>}
            </div>
            
            <div className="flex flex-col md:flex-row gap-4 mb-8">
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                <Input
                  placeholder="Search challenges..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="pl-10 bg-card border-border"
                />
              </div>
              
              <div className="flex gap-2 flex-wrap">
                {categories.map((cat) => (
                  <CyberButton
                    key={cat}
                    variant={category === cat ? 'primary' : 'ghost'}
                    size="sm"
                    onClick={() => setCategory(cat)}
                  >
                    {cat}
                  </CyberButton>
                ))}
              </div>
              
              <div className="flex gap-2 flex-wrap">
                {difficulties.map((diff) => (
                  <CyberButton
                    key={diff}
                    variant={difficulty === diff ? 'outline' : 'ghost'}
                    size="sm"
                    onClick={() => setDifficulty(diff)}
                    className={difficulty === diff ? difficultyColors[diff] || '' : ''}
                  >
                    {diff}
                  </CyberButton>
                ))}
              </div>
            </div>
          </motion.div>
          
          <motion.div layout className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            <AnimatePresence mode="popLayout">
              {filteredChallenges.map((challenge) => (
                <ChallengeCard
                  key={challenge.id}
                  challenge={challenge}
                  onClick={() => requireAuthenticatedPuzzleAccess(challenge)}
                />
              ))}
            </AnimatePresence>
          </motion.div>
          
          {filteredChallenges.length === 0 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center py-20">
              <Flag className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">No challenges found matching your criteria.</p>
            </motion.div>
          )}
        </div>
      </main>
      
      <AnimatePresence>
        {selectedChallenge && (
          <ChallengeModal
            challenge={selectedChallenge}
            onClose={() => setSelectedChallenge(null)}
            onSolve={handleSolve}
            showLearning={showLearning}
            onToggleLearning={toggleLearning}
            openNarrator={openNarrator}
          />
        )}
      </AnimatePresence>
    </PageTransition>
  );
};

export default CTF;
