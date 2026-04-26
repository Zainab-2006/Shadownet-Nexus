import { forwardRef, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Target, Clock, Star, CheckCircle2, Play } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import Navbar from '@/components/layout/Navbar';
import { PlayerDirective } from '@/components/PlayerDirective';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { Mission } from '@/types/mission';
import { useCreateTeam, useMissions } from '@/api/shadownetApi';
import { useNavigate } from 'react-router-dom';
import { useNarrator } from '@/context/NarratorContext';

const typeColors: Record<string, string> = {
  corporate_espionage: 'text-primary bg-primary/10 border-primary/30',
  data_heist: 'text-tertiary bg-tertiary/10 border-tertiary/30',
  infrastructure_attack: 'text-warning bg-warning/10 border-warning/30',
  cyber_warfare: 'text-secondary bg-secondary/10 border-secondary/30',
  'Data Heist': 'text-tertiary bg-tertiary/10 border-tertiary/30',
  'Cyber Defense': 'text-secondary bg-secondary/10 border-secondary/30',
};

const difficultyConfig: Record<string, { color: string; stars: number }> = {
  easy: { color: 'text-success', stars: 1 },
  medium: { color: 'text-warning', stars: 2 },
  hard: { color: 'text-destructive', stars: 3 },
  extreme: { color: 'text-secondary', stars: 4 },
};

const formatTime = (seconds: number) => `${Math.floor(seconds / 60)} min`;

const MissionCard = forwardRef<HTMLDivElement, { mission: Mission; index: number; onClick: () => void }>(({ mission, index, onClick }, ref) => {
  const diff = difficultyConfig[mission.difficulty] || difficultyConfig.medium;
  const typeClass = typeColors[mission.type] || 'text-cyan-200 bg-cyan-500/10 border-cyan-400/20';

  return (
    <motion.div ref={ref} layout initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} whileHover={{ scale: 1.02 }}>
      <CyberCard variant="interactive" className={`cursor-pointer ${mission.completed ? 'opacity-60' : ''}`} onClick={onClick}>
        <CyberCardContent className="p-5">
          <div className="flex items-start justify-between mb-3">
            <span className={`text-xs font-mono uppercase px-2 py-1 rounded border ${typeClass}`}>{String(mission.type).replace('_', ' ')}</span>
            <div className={`flex items-center gap-1 ${diff.color}`}>{Array.from({ length: diff.stars }).map((_, i) => <Star key={i} className="w-3 h-3 fill-current" />)}</div>
          </div>
          <p className="text-xs font-mono text-primary uppercase mb-1">Episode {index + 1}</p>
          <h3 className="font-heading text-lg font-bold mb-2 flex items-center gap-2">{mission.name}{mission.completed && <CheckCircle2 className="w-4 h-4 text-success" />}</h3>
          <p className="text-sm text-muted-foreground mb-4 line-clamp-2">{mission.description}</p>
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span className="flex items-center gap-1"><Clock className="w-3 h-3" />{formatTime(mission.timeLimitSeconds)}</span>
            <span className="text-primary font-mono">+{mission.xpReward} XP</span>
          </div>
        </CyberCardContent>
      </CyberCard>
    </motion.div>
  );
});
MissionCard.displayName = 'MissionCard';

const MissionBriefing = ({ mission, onClose, onStart }: { mission: Mission | null; onClose: () => void; onStart: () => void }) => {
  if (!mission) return null;
  const diff = difficultyConfig[mission.difficulty] || difficultyConfig.medium;
  const typeClass = typeColors[mission.type] || 'text-cyan-200 bg-cyan-500/10 border-cyan-400/20';

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/90 backdrop-blur-xl" onClick={onClose}>
      <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.9 }} className="w-full max-w-2xl" onClick={(e) => e.stopPropagation()}>
        <CyberCard variant="hero" className="overflow-hidden">
          <div className="relative p-6 border-b border-border bg-gradient-to-r from-card-elevated to-card">
            <div className="absolute inset-0 bg-gradient-to-r from-primary/5 to-secondary/5" />
            <div className="relative">
              <div className="flex items-center gap-3 mb-3">
                <span className={`text-xs font-mono uppercase px-2 py-1 rounded border ${typeClass}`}>{String(mission.type).replace('_', ' ')}</span>
                <div className={`flex items-center gap-1 ${diff.color}`}>
                  {Array.from({ length: diff.stars }).map((_, i) => <Star key={i} className="w-3 h-3 fill-current" />)}
                  <span className="ml-1 uppercase text-xs">{mission.difficulty}</span>
                </div>
              </div>
              <h2 className="font-heading text-3xl font-bold mb-2">{mission.name}</h2>
              <p className="text-muted-foreground">{mission.description}</p>
            </div>
          </div>
          <div className="p-6 space-y-6">
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center p-3 rounded-lg bg-card"><Clock className="w-5 h-5 mx-auto mb-1 text-muted-foreground" /><span className="text-lg font-heading font-bold">{formatTime(mission.timeLimitSeconds)}</span><p className="text-xs text-muted-foreground">Time Limit</p></div>
              <div className="text-center p-3 rounded-lg bg-card"><Target className="w-5 h-5 mx-auto mb-1 text-muted-foreground" /><span className="text-lg font-heading font-bold">{mission.objectives.length}</span><p className="text-xs text-muted-foreground">Objectives</p></div>
              <div className="text-center p-3 rounded-lg bg-card"><Star className="w-5 h-5 mx-auto mb-1 text-primary" /><span className="text-lg font-heading font-bold text-primary">{mission.xpReward}</span><p className="text-xs text-muted-foreground">XP Reward</p></div>
            </div>
            <div>
<h4 className="font-heading text-sm uppercase tracking-wider text-muted-foreground mb-3">Mission Objectives</h4>
              <ul className="space-y-2">{mission.objectives.map((obj, i) => <li key={i} className="flex items-center gap-3 text-sm"><span className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center text-xs font-mono text-primary">{i + 1}</span>{obj}</li>)}</ul>
            </div>
<p className="text-sm text-success">Evidence → accusation → consequences. Wrong accusations fracture trust.</p>
          </div>
          <div className="p-6 border-t border-border flex gap-4">
            <CyberButton variant="ghost" onClick={onClose} className="flex-1">Cancel</CyberButton>
            <CyberButton variant="hero" onClick={onStart} className="flex-1"><Play className="w-4 h-4 mr-2" />Enter Mission Runtime</CyberButton>
          </div>
        </CyberCard>
      </motion.div>
    </motion.div>
  );
};

const Missions = () => {
  const missionsQuery = useMissions();
  const createTeamMutation = useCreateTeam();
  const [selectedMission, setSelectedMission] = useState<Mission | null>(null);
  const [filter, setFilter] = useState<string>('all');
  const [teamId, setTeamId] = useState('');
  const navigate = useNavigate();
  const missionsData = missionsQuery.data || [];
  const isLoading = missionsQuery.isLoading;
  const types = ['all', 'corporate_espionage', 'data_heist', 'infrastructure_attack', 'cyber_warfare', 'Data Heist', 'Cyber Defense'];

  const filteredMissions = missionsData.filter(m => filter === 'all' || m.type === filter);

  const { openNarrator } = useNarrator();

  const enterMissionRuntime = (mission: Mission, resolvedTeamId: string) => {
    setTeamId(resolvedTeamId);
    navigate(`/missions/${mission.id}/runtime?teamId=${resolvedTeamId}`);
    setSelectedMission(null);
  };

  const handleStartMission = () => {
    if (!selectedMission || createTeamMutation.isPending) return;
    openNarrator({
      event: 'MISSION_BRIEF',
      title: selectedMission.name,
      message: `Operation ${selectedMission.name} commencing. Runtime loaded.`,
      dismissible: true
    });

    if (teamId) {
      enterMissionRuntime(selectedMission, teamId);
      return;
    }

    createTeamMutation.mutate(selectedMission.id, {
      onSuccess: (session: { teamId?: string; id: string }) => {
        const createdTeamId = session.teamId || session.id;
        if (createdTeamId) enterMissionRuntime(selectedMission, createdTeamId);
      },
    });
  };


  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-12">
            <div className="mb-12">
              <h1 className="font-heading text-4xl md:text-5xl font-bold mb-4"><span className="text-foreground">Mission</span><span className="text-primary ml-3">Chapters</span></h1>
              <p className="text-lg text-muted-foreground max-w-2xl">
                Choose the operation, review the objective, then enter the runtime where evidence, trust, and consequences resolve.
              </p>
            </div>
              <PlayerDirective
              mode="Mission Chapters"
              directive="Select a mission, confirm the briefing, and enter the active runtime."
              why="Each mission tests specific trust dynamics with consequences."
              next="Mission cell mechanics load inside the runtime."
              tone="cyan"
            />


            <div className="flex gap-2 flex-wrap mb-8">{types.map((type) => <CyberButton key={type} variant={filter === type ? 'primary' : 'ghost'} size="sm" onClick={() => setFilter(type)}>{type === 'all' ? 'All Types' : String(type).replace('_', ' ')}</CyberButton>)}</div>
          </motion.div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6"><AnimatePresence mode="popLayout">{filteredMissions.map((mission, index) => <MissionCard key={mission.id} mission={mission} index={index} onClick={() => setSelectedMission(mission)} />)}</AnimatePresence></div>
          {filteredMissions.length === 0 && !isLoading && <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-center py-20"><Target className="w-12 h-12 text-muted-foreground mx-auto mb-4" /><p className="text-muted-foreground">No missions available for this category.</p></motion.div>}
          {isLoading && <div className="text-center py-20"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div><p>Loading missions...</p></div>}
        </div>
      </main>
      <AnimatePresence>{selectedMission && <MissionBriefing mission={selectedMission} onClose={() => setSelectedMission(null)} onStart={handleStartMission} />}</AnimatePresence>
    </PageTransition>
  );
};

export default Missions;
