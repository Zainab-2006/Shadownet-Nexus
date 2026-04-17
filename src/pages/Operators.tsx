import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import {
  CheckCircle2,
  Crosshair,
  Shield,
  Sparkles,
  Target,
  User,
  Users,
  Zap,
} from 'lucide-react';
import Navbar from '@/components/layout/Navbar';
import { PlayerDirective } from '@/components/PlayerDirective';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import { useGame } from '@/context/GameContext';
import { useOperators } from '@/api/operatorApi';
import type { Operator } from '@/types/operator';
import { toast } from 'sonner';
import type { Character } from '@/data/roster';
import { roster } from '@/data/roster';


const factionConfig = {
  hero: {
    label: 'Heroes',
    description: 'Nexus-aligned operatives leading the front line against systemic collapse.',
    gradient: 'from-cyan-500/20 via-blue-500/10 to-transparent',
    border: 'border-cyan-400/30',
    chip: 'border-cyan-400/25 bg-cyan-500/10 text-cyan-200',
  },
  villain: {
    label: 'Villains',
    description: 'Hostile entities, ghosts, and engineered threats shaping the war from the shadows.',
    gradient: 'from-fuchsia-500/20 via-rose-500/10 to-transparent',
    border: 'border-fuchsia-400/30',
    chip: 'border-fuchsia-400/25 bg-fuchsia-500/10 text-fuchsia-200',
  },
} as const;


const roleIconFor = (role: string) => {
  const normalized = role.toLowerCase();
  if (normalized.includes('analyst')) return Target;
  if (normalized.includes('hack') || normalized.includes('engineer')) return Zap;
  if (normalized.includes('leader') || normalized.includes('commander')) return Users;
  if (normalized.includes('field') || normalized.includes('breach') || normalized.includes('assault')) return Shield;
  return User;
};

const rosterPresentationByBackendId = new Map(
  roster.map((character) => [character.backendOperatorId || 'op_' + character.id, character])
);

const rosterPresentationByName = new Map(
  roster.map((character) => [character.name.toLowerCase(), character])
);

const operatorToCharacter = (operator: Operator): Character => {
  const presentation = rosterPresentationByBackendId.get(operator.id) || rosterPresentationByName.get(operator.name.toLowerCase()) || null;
  const faction = operator.alignment === 'villain' ? 'villain' : 'hero';
  const skills = operator.skills.length > 0 ? operator.skills : [operator.role];

  return {
    id: operator.id,
    faction,
    name: operator.name,
    codename: operator.codename,
    title: presentation?.title || operator.specialty || operator.role,
    role: operator.role,
    background: operator.bio,
    status: operator.unlocked ? 'available' : 'locked',
    image: presentation?.image || operator.portraitUrl || '',
    skills: skills.map((skill) => ({
      key: skill.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      name: skill,
      description: skill,
    })),
    tags: presentation?.tags || [operator.role],
    backendOperatorId: operator.id,
  };
};

const RosterCard = ({
  character,
  isSelected,
  isActive,
  onClick,
}: {
  character: Character;
  isSelected: boolean;
  isActive: boolean;
  onClick: () => void;
}) => {
  const cfg = factionConfig[character.faction];

  return (
    <button
      type="button"
      onClick={onClick}
      className="w-full text-left transition-transform duration-300 hover:-translate-y-1"
    >
      <CyberCard
        variant="interactive"
        className={[
          'group overflow-hidden border bg-card/80 backdrop-blur-xl',
          cfg.border,
          isSelected ? 'ring-1 ring-primary/50 shadow-[0_0_30px_rgba(34,211,238,0.12)]' : '',
        ].join(' ')}
      >
        <div className="relative aspect-[4/5] overflow-hidden bg-black">
          <img
            src={character.image}
            alt={character.codename}
            className="h-full w-full object-contain object-center transition-transform duration-500 group-hover:scale-[1.02]"
          />
          <div className={`absolute inset-0 bg-gradient-to-t ${cfg.gradient}`} />
          <div className="absolute left-0 right-0 top-0 flex items-center justify-between p-4">
            <span className={`rounded-full border px-2.5 py-1 text-[10px] font-mono uppercase tracking-[0.22em] backdrop-blur ${cfg.chip}`}>
              {cfg.label.slice(0, -1)}
            </span>
            {isActive ? (
              <span className="rounded-full border border-emerald-400/40 bg-emerald-500/15 px-2.5 py-1 text-[10px] font-mono uppercase tracking-[0.18em] text-emerald-300">
                Active
              </span>
            ) : null}
          </div>
          <div className="absolute inset-x-0 bottom-0 p-4">
            <h3 className="font-heading text-xl font-bold tracking-[0.1em] text-white drop-shadow-[0_2px_10px_rgba(0,0,0,0.55)]">
              {character.name}
            </h3>
            <p className="mt-1 text-sm text-white/78 drop-shadow-[0_2px_10px_rgba(0,0,0,0.55)]">{character.codename}</p>
          </div>
        </div>
      </CyberCard>
    </button>
  );
};

const DossierPanel = ({ character, isActive, onActivate, isPending }: { character: Character; isActive: boolean; onActivate: () => void; isPending: boolean; }) => {
  const cfg = factionConfig[character.faction];
  const RoleIcon = roleIconFor(character.role);

  return (
    <AnimatePresence mode="wait">
      <motion.div key={character.id} initial={{ opacity: 0, x: 14 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -14 }}>
        <CyberCard variant="hero" className={`overflow-hidden border ${cfg.border}`}>
          <div className="grid gap-0 lg:grid-cols-[0.88fr_1.12fr]">
            <div className="relative min-h-[520px] overflow-hidden border-b border-border/40 bg-black lg:border-b-0 lg:border-r">
              <div className={`absolute inset-0 bg-gradient-to-t ${cfg.gradient}`} />
              <img src={character.image} alt={character.codename} className="relative z-10 h-full w-full object-contain object-center" />
              <div className="absolute left-0 right-0 top-0 z-20 flex items-start justify-between p-5">
                <div>
                  <div className="text-[11px] font-mono uppercase tracking-[0.24em] text-white/55">{cfg.label} Dossier</div>
                  <h2 className="mt-2 font-heading text-3xl font-bold tracking-[0.12em] text-white">{character.name}</h2>
                  <p className="mt-1 text-sm text-white/75">{character.codename}</p>
                </div>
                {isActive ? <div className="rounded-full border border-emerald-400/40 bg-emerald-500/15 px-3 py-1 text-[10px] font-mono uppercase tracking-[0.2em] text-emerald-300">Selected</div> : null}
              </div>
            </div>

            <div className="space-y-6 p-6">
              <div className="grid gap-3 sm:grid-cols-3">
                <div className="rounded-lg border border-border/50 bg-muted/20 p-4"><div className="mb-2 text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Faction</div><div className="text-sm text-foreground/85">{cfg.label.slice(0, -1)}</div></div>
                <div className="rounded-lg border border-border/50 bg-muted/20 p-4"><div className="mb-2 text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Role</div><div className="flex items-center gap-2 text-sm text-foreground/85"><RoleIcon className="h-4 w-4 text-primary" />{character.role}</div></div>
                <div className="rounded-lg border border-border/50 bg-muted/20 p-4"><div className="mb-2 text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Status</div><div className="text-sm text-emerald-300">Available</div></div>
              </div>

              <div>
                <div className="mb-2 text-[11px] font-mono uppercase tracking-[0.22em] text-muted-foreground">Profile</div>
                <p className="text-sm leading-7 text-foreground/80">{character.title}</p>
                <p className="mt-3 text-sm leading-7 text-foreground/72">{character.background}</p>
              </div>

              <div>
                <div className="mb-3 text-[11px] font-mono uppercase tracking-[0.22em] text-muted-foreground">Core Skills</div>
                <div className="grid gap-3 sm:grid-cols-2">
                  {character.skills.map((skill) => (
                    <div key={skill.key} className="rounded-lg border border-border/50 bg-muted/20 p-4">
                      <div className="mb-2 text-sm font-semibold text-white">{skill.name}</div>
                      <div className="text-sm leading-6 text-muted-foreground">{skill.description}</div>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <div className="mb-3 text-[11px] font-mono uppercase tracking-[0.22em] text-muted-foreground">Mission Tags</div>
                <div className="flex flex-wrap gap-2">
                  {character.tags.map((tag) => (
                    <span key={tag} className={`rounded-full border px-3 py-1.5 text-[11px] font-mono uppercase tracking-[0.12em] ${cfg.chip}`}>{tag}</span>
                  ))}
                </div>
              </div>

                <CyberButton variant={isActive ? 'secondary' : 'hero'} size="lg" className="w-full" onClick={onActivate} disabled={isPending || !character.backendOperatorId}>
                  {isActive ? <CheckCircle2 className="h-4 w-4" /> : <Crosshair className="h-4 w-4" />}
                  {!character.backendOperatorId ? 'Backend Sync Pending' : isActive ? 'Current Operator' : 'Enter Dossier'}
                </CyberButton>
            </div>
          </div>
        </CyberCard>
      </motion.div>
    </AnimatePresence>
  );
};

const OperatorsPage = () => {
  const { selectedOperator, selectOperator } = useGame();
  const { data: backendOperators = [], isLoading: operatorsLoading, isError: operatorsError } = useOperators();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'heroes' | 'villains' | 'all'>('heroes');
  const [selectedCharacterId, setSelectedCharacterId] = useState('');
  const [isPending, setIsPending] = useState(false);

  const backendRoster = useMemo(() => backendOperators.map(operatorToCharacter), [backendOperators]);
  const backendHeroes = useMemo(() => backendRoster.filter((character) => character.faction === 'hero'), [backendRoster]);
  const backendVillains = useMemo(() => backendRoster.filter((character) => character.faction === 'villain'), [backendRoster]);
  const visibleRoster = activeTab === 'heroes' ? backendHeroes : activeTab === 'villains' ? backendVillains : backendRoster;
  const selectedCharacter = useMemo(
    () => backendRoster.find((character) => character.id === selectedCharacterId) ?? visibleRoster[0] ?? backendRoster[0],
    [backendRoster, selectedCharacterId, visibleRoster]
  );
  const activeCharacterId = useMemo(
    () => backendRoster.find((character) => character.backendOperatorId === selectedOperator?.id)?.id ?? '',
    [backendRoster, selectedOperator?.id]
  );

  useEffect(() => {
    if (selectedCharacterId && backendRoster.some((character) => character.id === selectedCharacterId)) return;
    const backendSelected = backendRoster.find((character) => character.backendOperatorId === selectedOperator?.id);
    setSelectedCharacterId((backendSelected ?? visibleRoster[0] ?? backendRoster[0])?.id ?? '');
  }, [backendRoster, selectedCharacterId, selectedOperator?.id, visibleRoster]);

  const handleActivate = async (character: Character) => {
    try {
      setIsPending(true);
      setSelectedCharacterId(character.id);
      if (!character.backendOperatorId) throw new Error('Missing backend operator mapping');
      await selectOperator(character.backendOperatorId);

      navigate(`/story/operator/${character.backendOperatorId}`);
    } catch {
      toast.error('Failed to update the active character.');
    } finally {
      setIsPending(false);
    }
  };

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4">
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="mb-10">
            <div className="mb-8 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
              <div>
                <div className="mb-3 text-[11px] font-mono uppercase tracking-[0.28em] text-muted-foreground">Nexus Program</div>
                <h1 className="font-heading text-4xl font-bold md:text-5xl"><span className="text-foreground">Choose Your</span><span className="ml-3 text-primary">Operator</span></h1>
                <p className="mt-4 max-w-3xl text-lg text-muted-foreground">Choose who you become inside Shadownet-Nexus. Your operator changes how the narrator frames missions, how trust reacts, and your narrative history.</p>
              </div>

              <div className="grid gap-3 sm:grid-cols-3">
                <CyberCard><CyberCardContent className="flex items-center gap-3 p-4 pt-4"><Users className="h-5 w-5 text-cyan-300" /><div><div className="text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Heroes</div><div className="font-heading text-xl font-bold">{backendHeroes.length}</div></div></CyberCardContent></CyberCard>
                <CyberCard><CyberCardContent className="flex items-center gap-3 p-4 pt-4"><Sparkles className="h-5 w-5 text-fuchsia-300" /><div><div className="text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Villains</div><div className="font-heading text-xl font-bold">{backendVillains.length}</div></div></CyberCardContent></CyberCard>
                <CyberCard><CyberCardContent className="flex items-center gap-3 p-4 pt-4"><CheckCircle2 className="h-5 w-5 text-emerald-300" /><div><div className="text-[11px] font-mono uppercase tracking-[0.2em] text-muted-foreground">Status</div><div className="font-heading text-xl font-bold">{operatorsLoading ? 'Syncing' : operatorsError ? 'Offline' : 'Backend'}</div></div></CyberCardContent></CyberCard>
              </div>
            </div>

            <PlayerDirective
              mode="Operators"
              directive="Choose the identity you want the network to recognize. This is your point of view, not just a skin."
              why="Your operator changes narrator framing, trust reactions, and relationship tension."
              next="Select an operator. You will immediately enter their personal dossier and story."
            />

            <div className="flex flex-wrap gap-2">
              <CyberButton variant={activeTab === 'heroes' ? 'primary' : 'ghost'} size="sm" onClick={() => setActiveTab('heroes')}>Heroes</CyberButton>
              <CyberButton variant={activeTab === 'villains' ? 'secondary' : 'ghost'} size="sm" onClick={() => setActiveTab('villains')}>Villains</CyberButton>
              <CyberButton variant={activeTab === 'all' ? 'outline' : 'ghost'} size="sm" onClick={() => setActiveTab('all')}>All Characters</CyberButton>
            </div>
          </motion.div>

          <div className="grid gap-8 xl:grid-cols-[1.15fr_1.05fr]">
            <div>
              <div className="mb-4">
                <h2 className="font-heading text-2xl font-bold">{activeTab === 'all' ? 'Complete Roster' : activeTab === 'heroes' ? 'Heroes' : 'Villains'}</h2>
                <p className="text-sm text-muted-foreground">{activeTab === 'all' ? 'The full 24-character lineup from backend operator truth.' : factionConfig[activeTab === 'heroes' ? 'hero' : 'villain'].description}</p>
              </div>

              <div className="grid gap-6 sm:grid-cols-2 2xl:grid-cols-3">
                {visibleRoster.map((character) => (
                  <RosterCard
                    key={character.id}
                    character={character}
                    isSelected={selectedCharacter?.id === character.id}
                    isActive={activeCharacterId === character.id}
                    onClick={() => setSelectedCharacterId(character.id)}
                  />
                ))}
              </div>
            </div>

            {selectedCharacter ? (
              <div className="xl:sticky xl:top-24 xl:self-start">
                <div className="mb-4">
                  <h2 className="font-heading text-2xl font-bold">Operator Dossier</h2>
                  <p className="text-sm text-muted-foreground">This locks your operator identity across Nexus. Enter their dossier and story path directly. Missions and trust shaped by this choice.</p>
                </div>
                <DossierPanel character={selectedCharacter} isActive={activeCharacterId === selectedCharacter.id} onActivate={() => void handleActivate(selectedCharacter)} isPending={isPending} />
              </div>
            ) : null}
          </div>
        </div>
      </main>

    </PageTransition>
  );
};

export default OperatorsPage;









