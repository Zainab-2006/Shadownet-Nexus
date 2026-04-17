import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Shield, Target, Trophy, Users, ChevronRight, Zap, Lock } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import HeroScene from '@/components/three/HeroScene';
import Navbar from '@/components/layout/Navbar';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';

const features = [
  {
    icon: Users,
    title: '24 Operators',
    description: 'Choose one operator as your point of view while the full cast stays active in the campaign.',
  },
  {
    icon: Shield,
    title: '1 Narrator',
    description: 'Your login identity becomes the narrator voice that frames training, missions, and consequences.',
  },
  {
    icon: Trophy,
    title: 'Solo',
    description: 'Solve ranked CTF challenges alone. Training opens after repeated misses and does not count toward the leaderboard.',
  },
  {
    icon: Target,
    title: 'Missions',
    description: 'Run chapter operations with operator roles, trust pressure, evidence, and consequences.',
  },
  {
    icon: Lock,
    title: 'Story',
    description: 'Enter an operator route for background, secrets, relationships, choices, and mission impact.',
  },
];

const stats = [
  { value: '24', label: 'Operators' },
  { value: '1', label: 'Narrator' },
  { value: 'Solo', label: 'Ranked CTF' },
  { value: 'Story', label: 'Operator Routes' },
];

const Index = () => {
  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      
      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
        <HeroScene />
        
        <div className="container relative z-10 px-4 pt-20">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="text-center max-w-4xl mx-auto"
          >
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.6, delay: 0.4 }}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/30 mb-8"
            >
              <Zap className="w-4 h-4 text-primary" />
              <span className="text-sm font-heading text-primary tracking-wider">
                SEASON 1 IS NOW LIVE
              </span>
            </motion.div>
            
            <h1 className="font-heading text-5xl md:text-7xl lg:text-8xl font-black mb-6 leading-tight">
              <span className="gradient-text">SHADOWNET</span>
              <br />
              <span className="text-foreground">NEXUS</span>
            </h1>
            
            <p className="text-xl md:text-2xl text-muted-foreground mb-4 font-body">
              CTF cyber operations
            </p>
            
            <p className="text-lg text-muted-foreground/80 mb-10 max-w-2xl mx-auto">
              Shadownet-Nexus is a cyber intelligence world with 24 operators, 1 narrator, ranked Solo challenges, trust-driven Missions, and operator Story routes.
            </p>
            
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.8 }}
              className="flex flex-col sm:flex-row items-center justify-center gap-4"
            >
              <CyberButton variant="hero" size="xl" asChild>
                <Link to="/operators">
                  <Shield className="w-5 h-5 mr-2" />
                  Choose Operator
                </Link>
              </CyberButton>
              <CyberButton variant="outline" size="xl" asChild>
                <Link to="/solo">
                  Enter Solo
                  <ChevronRight className="w-5 h-5 ml-2" />
                </Link>
              </CyberButton>
            </motion.div>
          </motion.div>
          
          {/* Stats */}
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 1 }}
            className="grid grid-cols-2 md:grid-cols-4 gap-6 mt-20 max-w-4xl mx-auto"
          >
            {stats.map((stat, i) => (
              <div
                key={stat.label}
                className="text-center p-4"
              >
                <div className="font-heading text-3xl md:text-4xl font-bold text-primary text-glow-primary mb-1">
                  {stat.value}
                </div>
                <div className="text-sm text-muted-foreground uppercase tracking-wider">
                  {stat.label}
                </div>
              </div>
            ))}
          </motion.div>
        </div>
        
        {/* Scroll indicator */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1.5 }}
          className="absolute bottom-8 left-1/2 -translate-x-1/2"
        >
          <motion.div
            animate={{ y: [0, 10, 0] }}
            transition={{ duration: 2, repeat: Infinity }}
            className="w-6 h-10 rounded-full border-2 border-primary/50 flex items-start justify-center p-2"
          >
            <motion.div
              animate={{ opacity: [1, 0, 1] }}
              transition={{ duration: 2, repeat: Infinity }}
              className="w-1.5 h-2 bg-primary rounded-full"
            />
          </motion.div>
        </motion.div>
      </section>
      
      {/* Features Section */}
      <section className="relative py-32 overflow-hidden">
        <div className="absolute inset-0 hex-pattern opacity-30" />
        
        <div className="container relative px-4">
          <div
            className="text-center mb-16"
          >

            <h2 className="font-heading text-4xl md:text-5xl font-bold mb-4">
              <span className="text-foreground">How Nexus</span>
              <span className="text-primary ml-3">Works</span>
            </h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Three player routes carry the game: Solo for ranked skill, Missions for trust-driven operations, and Story for operator narrative.
            </p>
          </div>

          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feature, i) => {
              const Icon = feature.icon;
              return (
                <div
                  key={feature.title}
                  className={`opacity-0 animate-in slide-in-from-bottom-4 duration-600 delay-${i * 100}`}
                >

                  <CyberCard variant="glow" className="h-full p-6 group">
                    <CyberCardContent className="p-0">
                      <div className="w-14 h-14 rounded-lg bg-primary/10 flex items-center justify-center mb-4 group-hover:bg-primary/20 transition-colors">
                        <Icon className="w-7 h-7 text-primary" />
                      </div>
                      <h3 className="font-heading text-lg font-bold mb-2">
                        {feature.title}
                      </h3>
                      <p className="text-muted-foreground text-sm">
                        {feature.description}
                      </p>
                    </CyberCardContent>
                  </CyberCard>
                </div>

              );
            })}
          </div>
        </div>
      </section>
      
      {/* CTA Section */}
      <section className="relative py-32">
        <div className="absolute inset-0 bg-gradient-to-t from-primary/5 via-transparent to-transparent" />
        
        <div className="container relative px-4">
          <div
            className="animate-in zoom-in-95 duration-800"
          >

            <CyberCard variant="hero" className="p-8 md:p-16 text-center">
              <h2 className="font-heading text-3xl md:text-5xl font-bold mb-6">
                Narrator <span className="text-primary text-glow-primary">Brief</span>
              </h2>
              <p className="text-lg text-muted-foreground mb-8 max-w-2xl mx-auto">
                Welcome to Shadownet-Nexus. Choose your operator for Story, enter Solo for ranked CTF, or start Missions for trust-driven operations.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <CyberButton variant="hero" size="lg" asChild>
                  <Link to="/operators">Choose Operator</Link>
                </CyberButton>
                <CyberButton variant="outline" size="lg" asChild>
                  <Link to="/solo">Enter Solo</Link>
                </CyberButton>
              </div>
            </CyberCard>
          </div>

        </div>
      </section>
      
      {/* Footer */}
      <footer className="border-t border-border/50 py-12">
        <div className="container px-4">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-3">
              <Shield className="w-8 h-8 text-primary" />
              <span className="font-heading text-xl font-bold">
                SHADOWNET-NEXUS
              </span>
            </div>
            <p className="text-sm text-muted-foreground">
              Nexus Division. Solo, Missions, Story.
            </p>
          </div>
        </div>
      </footer>
    </PageTransition>
  );
};

export default Index;



