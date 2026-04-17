import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';

import { ChevronLeft, BookOpen, User, Shield, Target, Users, AlertTriangle, Zap } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard } from '@/components/ui/cyber-card';
import Navbar from '@/components/layout/Navbar';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useGame } from '@/context/GameContext';
import { useCharacters } from '@/api/operatorApi';
import HeroScene from '@/components/three/HeroScene';
import { Operator } from '@/types/operator';

import { useState } from 'react';



const OperatorStory = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'backstory' | 'dossier' | 'missions'>('overview');

  const { id } = useParams<{ id: string }>();
  const { selectedOperator } = useGame();
  const { data: charactersData } = useCharacters();

  const allCharacters = charactersData || [];
  const character = allCharacters.find(c => c.id === id) || selectedOperator || allCharacters[0] as Operator;

  if (!character) {
    return null;
  }

  const trustLevel = character.trust || 50;

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      
      <div className="min-h-screen relative overflow-hidden">
        <HeroScene />
        
        <main className="relative z-10 container px-4 py-24">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="mb-8"
          >
            <CyberButton variant="ghost" size="sm" asChild>
              <Link to="/operators">
                <ChevronLeft className="w-4 h-4 mr-2" />
                Back to Operators
              </Link>
            </CyberButton>
          </motion.div>

          <div className="grid lg:grid-cols-2 gap-12 items-start">
            {/* Part 1: Overview */}
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.2 }}
              className="relative space-y-8"
            >
                <div className="aspect-[3/4] bg-gradient-to-b from-card/50 to-card rounded-2xl p-8 flex items-center justify-center shadow-2xl shadow-primary/10 relative">
                <div className="w-64 h-80 rounded-xl shadow-glow-primary relative overflow-hidden">
                  {character.portraitUrl ? (
                    <img 
                      loading="eager"
                      src={character.portraitUrl} 
                      alt={`${character.codename} portrait`}
                      className="object-cover w-full h-full rounded-xl shadow-2xl ring-4 ring-background/50" 
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-black/60 border-4 border-primary/20 relative overflow-hidden">
                      {/* Faction silhouette */}
                      <div className={`absolute inset-0 opacity-30 ${character.faction === 'Heroes' ? 'bg-gradient-to-r from-emerald-500/20 via-cyan-500/20 to-blue-500/20' : 'bg-gradient-to-r from-rose-500/20 via-fuchsia-500/20 to-purple-500/20'}`} />
                      <div className="relative z-10 text-6xl font-heading font-bold text-primary/70">
                        {character.codename?.slice(0, 2)?.toUpperCase() || 'OP'}
                      </div>
                      <div className="absolute bottom-4 left-4 text-xs font-mono uppercase tracking-wider text-primary/50">
                        {character.faction === 'Heroes' ? 'Hero Silhouette' : 'Villain Shadow'}
                      </div>
                    </div>
                  )}

                </div>
              </div>
              
              {/* Trust badge de-emphasized - move if needed */}

              {/* Continue Story Panel */}
              <CyberCard className="p-6 bg-gradient-to-r from-primary/5 to-secondary/5 border-primary/20">
                <h3 className="flex items-center gap-2 text-lg font-bold mb-4">
                  <BookOpen className="w-5 h-5" />
                  Continue Story Path
                </h3>
                <p className="text-sm text-muted-foreground mb-4">
                  Current scene progress and recent choices shown here. Emotional decisions shape trust and mission impact.
                </p>
                <div className="space-y-2">
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Current Scene</span>
                    <span className="font-mono">Scene 5/12</span>
                  </div>
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Last Choice</span>
                    <span className="font-mono">Confronted Ally → Trust -5</span>
                  </div>
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Next Branch</span>
                    <span className="font-mono">Mission Recon Unlocked</span>
                  </div>
                </div>
                <CyberButton variant="hero" className="w-full mt-4" asChild>
                  <Link to={`/story/operator/${character.id}/scene/5`}>
                    Continue POV · Scene 5/12
                  </Link>
                </CyberButton>
              </CyberCard>

              {/* Mission Impact */}
              <CyberCard className="p-6 bg-secondary/5 border-secondary/20">
                <h3 className="flex items-center gap-2 text-lg font-heading mb-4 text-secondary">
                  <Target className="w-5 h-5" />
                  Mission Impact
                </h3>
                <p className="text-sm text-foreground/80 leading-relaxed mb-4">
                  Trust {trustLevel}/100 unlocks operator bonuses in squad missions.
                </p>
                <div className="w-full bg-secondary rounded-full h-2">
                  <div className="bg-gradient-to-r from-primary to-secondary h-2 rounded-full" style={{width: `${Math.min(trustLevel, 100)}%`}}></div>
                </div>
                <div className="text-xs font-mono uppercase tracking-wider text-muted-foreground mt-2">
                  {trustLevel >= 75 ? 'High Trust · Full Bonuses' : trustLevel >= 50 ? 'Operational' : 'At Risk · Limited'}
                </div>
              </CyberCard>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
              className="space-y-10"
            >
              {/* Header */}
              <div>
                <h1 className="font-heading text-4xl lg:text-5xl font-bold mb-4 bg-gradient-to-r from-primary via-secondary to-destructive bg-clip-text text-transparent">
                  {character.codename}
                </h1>
                <p className="text-xl text-muted-foreground">{character.name}</p>
                
                <div className="mt-4 flex flex-wrap items-center gap-3 text-sm">
                  <span className="px-3 py-1 bg-primary/20 text-primary rounded-full text-xs font-mono uppercase border border-primary/20">
                    {character.role}
                  </span>
                  <span className="px-3 py-1 bg-secondary/20 text-secondary rounded-full text-xs font-mono uppercase border border-secondary/20">
                    {character.faction}
                  </span>
                  <span className="px-3 py-1 bg-muted text-muted-foreground rounded-full text-xs font-mono uppercase border border-border">
                    {character.tier} Tier
                  </span>
                </div>
              </div>

              {/* Dossier Tabs */}
              <div className="grid gap-1 bg-border/20 rounded-xl p-1">
                {[
                  { id: 'overview', label: 'Overview', icon: User },
                  { id: 'backstory', label: 'Backstory', icon: BookOpen },
                  { id: 'dossier', label: 'Dossier', icon: Shield },
                  { id: 'missions', label: 'Mission Impact', icon: Target }
                ].map(({ id, label, icon: Icon }) => (
                  <CyberButton
                    key={id}
                    variant={activeTab === id ? 'secondary' : 'ghost'}
                    size="sm"
                    className="justify-start h-auto py-2.5 capitalize font-mono tracking-wider text-xs"
onClick={() => setActiveTab(id as 'overview' | 'backstory' | 'dossier' | 'missions')}
                  >
                    <Icon className="w-4 h-4 mr-2 flex-shrink-0 opacity-75" />
                    {label}
                  </CyberButton>
                ))}
              </div>

              {/* Tab Content */}
              <div className="space-y-6 mt-6">
                {activeTab === 'overview' && (
                  <div>
                    <h3 className="flex items-center gap-2 text-lg font-heading mb-4">
                      <User className="w-5 h-5" />
                      Operator Summary
                    </h3>
                    <div className="prose prose-invert max-w-none text-sm leading-relaxed">
                      <p>{character.bio?.substring(0, 300) || "Profile summary."}...</p>
                    </div>
                  </div>
                )}

                {activeTab === 'backstory' && (
                  <div>
                    <h3 className="flex items-center gap-2 text-lg font-heading mb-4">
                      <BookOpen className="w-5 h-5" />
                      Origin, Trauma & Psyche
                    </h3>
                    <div className="prose prose-invert max-w-none text-sm leading-relaxed text-foreground/80 space-y-4">
                      <p><strong>Entry to Nexus:</strong> {character.bio?.split('.')[0] || "Restricted operational history."}</p>
                      <p><strong>Core Trauma:</strong> {character.personality || 'Loyalty fracture under accusation pressure.'}</p>
                      <blockquote className="border-l-4 border-primary/30 pl-4 italic bg-primary/5 py-2">
                        "{character.bio || 'The shadow of betrayal lingers.'}"
                      </blockquote>
                    </div>
                  </div>
                )}


                {activeTab === 'dossier' && (
                  <div>
                    <h3 className="flex items-center gap-2 text-lg font-heading mb-4">
                      <Shield className="w-5 h-5" />
                      Psychological Dossier
                    </h3>
                <div className="grid gap-4 md:grid-cols-2">
                      {/* Psychological Profile */}
                      <CyberCard className="border-primary/20">
                        <div className="flex items-start gap-3 p-4">
                          <div className="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center">
                            <Shield className="w-5 h-5 text-primary" />
                          </div>
                          <div>
                            <div className="font-mono text-xs uppercase tracking-wider text-primary mb-1">Psychological Profile</div>
                            <p className="text-sm">{character.personality || 'Calculated risk-taker. Loyalty stable but tested by moral ambiguity.'}</p>
                          </div>
                        </div>
                      </CyberCard>
                      {/* Operational Style */}
                      <CyberCard className="border-secondary/20">
                        <div className="flex items-start gap-3 p-4">
                          <div className="w-10 h-10 bg-secondary/10 rounded-lg flex items-center justify-center">
                            <Zap className="w-5 h-5 text-secondary" />
                          </div>
                          <div>
                            <div className="font-mono text-xs uppercase tracking-wider text-secondary mb-1">Operational Style</div>
                            <p className="text-sm">{character.role} specialist. {character.specialization || 'Precision execution under pressure.'}</p>
                          </div>
                        </div>
                      </CyberCard>
                      {/* Known Allies */}
                      <CyberCard className="border-emerald/20">
                        <div className="flex items-start gap-3 p-4">
                          <div className="w-10 h-10 bg-emerald/10 rounded-lg flex items-center justify-center">
                            <Users className="w-5 h-5 text-emerald" />
                          </div>
                          <div>
                            <div className="font-mono text-xs uppercase tracking-wider text-emerald mb-1">Known Allies</div>
                            <p className="text-sm">Core team: {character.faction === 'Heroes' ? 'Darren, Yuna' : 'Iris, Rowan'}. Signature partnership drives mission bonuses.</p>
                          </div>
                        </div>
                      </CyberCard>
                      {/* Signature Incidents */}
                      <CyberCard className="border-amber/20">
                        <div className="flex items-start gap-3 p-4">
                          <div className="w-10 h-10 bg-amber/10 rounded-lg flex items-center justify-center">
                            <AlertTriangle className="w-5 h-5 text-amber" />
                          </div>
                          <div>
                            <div className="font-mono text-xs uppercase tracking-wider text-amber mb-1">Signature Incidents</div>
                            <p className="text-sm">Past ops: {character.faction === 'Heroes' ? 'NullByte breach' : 'Oracle prediction failure'}. Defines operational trust posture.</p>
                          </div>
                        </div>
                      </CyberCard>
                      {/* Trust Posture */}
                      <CyberCard className="md:col-span-2 border-accent/20">
                        <div className="flex items-start gap-3 p-4">
                          <div className="w-10 h-10 bg-accent/10 rounded-lg flex items-center justify-center">
                            <Shield className="w-5 h-5 text-accent" />
                          </div>
                          <div>
                            <div className="font-mono text-xs uppercase tracking-wider text-accent mb-1">Trust Posture & {character.tier?.toLowerCase()} Tier</div>
                            <p className="text-sm font-medium">Faction: <span className="font-mono uppercase">{character.faction}</span>. {character.tier} operations unlock specialist bonuses, higher accusation thresholds.</p>
                          </div>
                        </div>
                      </CyberCard>
                    </div>

                  </div>
                )}

                {activeTab === 'missions' && (
                  <div>
                    <h3 className="flex items-center gap-2 text-lg font-heading mb-4">
                      <Target className="w-5 h-5" />
                      Mission Impact & Trust Posture
                    </h3>
                    <CyberCard className="p-6 bg-secondary/5 border-secondary/20">
                      <p className="text-sm text-foreground/80 leading-relaxed mb-4">
                        Trust modifiers from this operator influence squad dynamics. Choices here unlock/alter mission behaviors, faction tension, and accusation outcomes.
                      </p>
                      <div className="text-xs font-mono uppercase tracking-wider text-muted-foreground grid grid-cols-2 gap-2">
                        <div>Readiness: <span className="font-bold text-primary">{trustLevel >= 50 ? 'Operational' : 'At Risk'}</span></div>
                        <div>Faction Ties: <span className="font-bold">{character.faction}</span></div>
                      </div>
                    </CyberCard>


                  </div>
                )}
              </div>
                <p className="text-sm text-foreground/80 mb-6">
                  Make choices in memories and relationships. Each decision changes trust, unlocks mission variants.
                </p>
                <div className="grid md:grid-cols-2 gap-4">
                <CyberButton variant="hero" size="lg" className="w-full" asChild>
                    <Link to={`/story/operator/${character.id}/scene/1`}>
                      <span>Continue <span className="font-mono uppercase tracking-wider">Story</span> Scene 5/12</span>
                    </Link>
                  </CyberButton>

                  <CyberButton variant="outline" size="lg" className="w-full">
                    Timeline (Coming)
                  </CyberButton>
                </div>
            </motion.div>
          </div>
        </main>
      </div>
    </PageTransition>
  );
};

export default OperatorStory;

