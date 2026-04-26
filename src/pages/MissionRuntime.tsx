import React from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ChevronLeft, Clock, CheckCircle2, Target, Activity, Users } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import Navbar from '@/components/layout/Navbar';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useGame } from '@/context/GameContext';
import { useCompleteMissionRuntime, useMission, useMissionRuntime, useUpdateMissionObjective } from '@/api/shadownetApi';
import MissionCellPanel from '@/components/MissionCellPanel';
import { useNarrator } from '@/context/NarratorContext';

const MissionRuntime = () => {
  const { missionId } = useParams<{ missionId: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { refreshProgression } = useGame();
  const { openNarrator } = useNarrator();
  const missionQuery = useMission(missionId);
  const runtimeQuery = useMissionRuntime(missionId);
  const updateObjective = useUpdateMissionObjective(missionId);
  const completeMission = useCompleteMissionRuntime(missionId);
  const mission = missionQuery.data;
  const runtime = runtimeQuery.data;
  const teamId = searchParams.get('teamId') || '';

  const objectives = runtime?.objectives ?? [];
  const completedCount = runtime?.completedObjectives ?? objectives.filter((objective) => objective.complete).length;
  const allComplete = objectives.length > 0 && completedCount === objectives.length;
  const runtimeStatus = runtime?.status ?? 'active';
  const timeLeft = runtime?.timeRemaining ?? mission?.timeLimitSeconds ?? 0;
  const evidenceCount = runtime?.evidenceCount ?? 0;

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const toggleObjective = (objId: string, complete: boolean) => {
    if (runtimeStatus !== 'active' || updateObjective.isPending) return;
    updateObjective.mutate({ objectiveId: objId, complete: !complete });
  };

  const handleCompleteMission = async () => {
    const result = await completeMission.mutateAsync();
    await refreshProgression();
    openNarrator({
      event: result?.status === 'failed' ? 'MISSION_FAILURE' : 'MISSION_SUCCESS',
      title: result?.status === 'failed' ? 'Mission Failed' : 'Mission Complete',
      message: result?.status === 'failed'
        ? 'The operation has closed with unresolved pressure. Trust consequences have been recorded.'
        : 'The operation is resolved. Evidence, trust, and progress have been recorded.',
      dismissible: true,
    });
  };

  if (!missionId) {
    return (
      <PageTransition>
        <div className="min-h-screen flex items-center justify-center text-destructive">
          No mission selected.
        </div>
      </PageTransition>
    );
  }

  if (missionQuery.isLoading || runtimeQuery.isLoading) {
    return (
      <PageTransition>
        <Navbar />
        <ParticleBackground />
        <main className="min-h-screen pt-24 pb-16 flex items-center justify-center">
          Loading mission runtime...
        </main>
      </PageTransition>
    );
  }

  if (missionQuery.isError || runtimeQuery.isError || !mission || !runtime) {
    return (
      <PageTransition>
        <Navbar />
        <ParticleBackground />
        <main className="min-h-screen pt-24 pb-16 flex items-center justify-center text-destructive">
          Mission runtime unavailable.
        </main>
      </PageTransition>
    );
  }

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4 max-w-6xl mx-auto">
          {/* Header */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
            <CyberButton variant="ghost" onClick={() => navigate('/missions')} className="mb-6">
              <ChevronLeft className="w-5 h-5 mr-2" />
              Back to Missions
            </CyberButton>
            <div className="flex items-center gap-6 mb-4">
              <h1 className="font-heading text-4xl font-bold">Mission Episode Runtime: {(mission?.name || missionId).replace('_', ' ').toUpperCase()}</h1>
              <div className={`px-4 py-2 rounded-full text-sm font-mono ${
                runtimeStatus === 'active' ? 'bg-success/20 text-success border-success/30' :
                'bg-destructive/20 text-destructive'
              }`}>
                {runtimeStatus.toUpperCase()}
              </div>
            </div>
            <div className="flex items-center gap-8 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <Clock className="w-4 h-4" />
                {formatTime(timeLeft)}
              </div>
              <div className="flex items-center gap-2">
                <Target className="w-4 h-4" />
                {completedCount}/{objectives.length} objectives
              </div>
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4" />
                Evidence: {evidenceCount}
              </div>
            </div>
          </motion.div>

          <div className="grid lg:grid-cols-3 gap-8">
            {/* Objectives */}
            <CyberCard className="lg:col-span-2">
              <CyberCardContent className="p-0">
                <div className="p-6 border-b border-border">
                  <h2 className="font-heading text-xl mb-2 flex items-center gap-2">
                    <Target className="w-5 h-5" />
                    Episode Objectives
                  </h2>
                </div>
                <div className="p-6 space-y-3">
                  {objectives.map((objective) => (
                    <motion.div
                      key={objective.id}
                      layout
                      data-testid="mission-objective-toggle"
                      className={`flex items-center gap-4 p-4 border rounded-lg ${runtimeStatus === 'active' ? 'cursor-pointer hover:bg-accent' : 'cursor-not-allowed opacity-70'}`}
                      onClick={() => toggleObjective(objective.id, objective.complete)}
                    >
                      <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono ${
                        objective.complete 
                          ? 'bg-success text-success border-success' 
                          : 'bg-secondary/30 border-secondary/50 hover:border-primary'
                      }`}>
                        {objective.complete ? <CheckCircle2 className="w-4 h-4" /> : '*'}
                      </div>
                      <div className="flex-1">
                        <p className="font-medium">{objective.title}</p>
                      </div>
                    </motion.div>
                  ))}
                </div>
              </CyberCardContent>
            </CyberCard>

            {/* Sidebar */}
            <CyberCard>
              <CyberCardContent className="p-6 space-y-4">
                <h3 className="font-heading text-lg mb-4 flex items-center gap-2">
                  <Users className="w-4 h-4" />
                  Episode Runtime
                </h3>
                

                <MissionCellPanel teamId={teamId} missionId={missionId} />

                {/* Complete Check */}
                {allComplete && runtimeStatus === 'active' && (
                  <CyberButton 
                    data-testid="mission-complete-button"
                    className="w-full bg-gradient-to-r from-success to-emerald-600" 
                    size="lg"
                    onClick={handleCompleteMission}
                    disabled={completeMission.isPending}
                  >
                    <CheckCircle2 className="w-4 h-4 mr-2" />
                    Resolve Episode
                  </CyberButton>
                )}
              </CyberCardContent>
            </CyberCard>
          </div>

          {/* Complete Screen */}
          {runtimeStatus === 'completed' && (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="mt-12 text-center"
            >
              <CyberCard variant="hero" className="max-w-2xl mx-auto">
                <CyberCardContent className="p-12">
                  <CheckCircle2 className="w-24 h-24 text-success mx-auto mb-8" />
                  <h2 className="font-heading text-3xl mb-6">Operation Resolved</h2>
                  <p className="text-xl text-muted-foreground mb-8">
                    Phase status, evidence thresholds, team pressure, and narrator directives updated through backend truth.
                  </p>
                  <div className="flex flex-col sm:flex-row gap-4 justify-center">
                    <CyberButton size="lg" onClick={() => navigate('/missions')}>
                      Next Mission
                    </CyberButton>
                    <CyberButton variant="outline" size="lg" onClick={() => navigate('/story')}>
                      Continue Story
                    </CyberButton>
                  </div>
                </CyberCardContent>
              </CyberCard>
            </motion.div>
          )}
        </div>
      </main>
    </PageTransition>
  );
};

export default MissionRuntime;
