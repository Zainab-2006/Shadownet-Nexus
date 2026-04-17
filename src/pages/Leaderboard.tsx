import { useState } from 'react';
import { useLeaderboard } from '@/api/leaderboardApi';
import { Trophy, Crown, Zap, Target, Users } from 'lucide-react';
import { CyberCard, CyberCardContent } from '@/components/ui/cyber-card';
import Navbar from '@/components/layout/Navbar';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useWebSocket } from '@/hooks/useWebSocket';
import { motion } from 'framer-motion';

interface LeaderboardEntry {
  id: string;
  displayName: string;
  score: number;
  solves: number;
  rank?: number;
}

const Leaderboard = () => {
  const { connected } = useWebSocket();
  const [filter, setFilter] = useState<'all' | 'week' | 'today'>('all');
  const [userId] = useState(localStorage.getItem('userId') || '');
  const { data: leaderboardData, isLoading: loading } = useLeaderboard();

  const leaderboard = (leaderboardData || []) as LeaderboardEntry[];

  const getRankColor = (rank: number) => {
    if (rank === 1) return 'text-yellow-400';
    if (rank === 2) return 'text-gray-300';
    if (rank === 3) return 'text-orange-400';
    return 'text-foreground';
  };

  const getRankIcon = (rank: number) => {
    if (rank === 1) return <Crown className="w-5 h-5" />;
    if (rank === 2) return <Zap className="w-5 h-5" />;
    if (rank === 3) return <Target className="w-5 h-5" />;
    return <span className="font-bold text-lg">#{rank}</span>;
  };

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />

      <main className="min-h-screen pt-24 pb-16">
        <div className="container px-4">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-12"
          >
            <div className="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-6 mb-8">
              <div>
                <h1 className="font-heading text-4xl md:text-5xl font-bold mb-4">
                  <span className="text-foreground">Leaderboard</span>
                </h1>
                <p className="text-muted-foreground text-lg">
                  Top performers in NEXUS CTF challenges
                </p>
                <p className="text-xs text-muted-foreground mt-1 italic">
                  Training note: Advance tiers (Rookie → Platinum) through mission training, story progression, and consistent solves.
                </p>
              </div>

              <div className="flex items-center gap-4">

                <div className="flex items-center gap-2 px-4 py-2 rounded-lg bg-card border border-border">
                  <Users className="w-5 h-5 text-primary" />
                  <span className="font-mono font-bold">{leaderboard.length} Players</span>
                </div>
                <div
                  className={`w-2 h-2 rounded-full ${
                    connected ? 'bg-success animate-pulse' : 'bg-destructive'
                  }`}
                />
              </div>
            </div>

            {/* Filters */}
            <div className="flex gap-3">
              {(['all', 'week', 'today'] as const).map((f) => (
                <button
                  key={f}
                  onClick={() => setFilter(f)}
                  className={`px-4 py-2 rounded-lg font-heading uppercase text-sm transition-colors ${
                    filter === f
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-card border border-border hover:border-primary'
                  }`}
                >
                  {f === 'all' ? 'All Time' : f === 'week' ? 'This Week' : 'Today'}
                </button>
              ))}
            </div>
          </motion.div>

          {/* Top 3 Podium */}
          {!loading && leaderboard.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-12 grid md:grid-cols-3 gap-6"
            >
              {/* 2nd Place */}
              {leaderboard[1] && (
                <div className="md:order-1">
                  <CyberCard variant="interactive" className="h-full">
                    <CyberCardContent className="p-6 text-center">
                      <div className="mb-4">
                        <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-300/20 border-2 border-gray-300 flex items-center justify-center">
                          <Zap className="w-8 h-8 text-gray-300" />
                        </div>
                        <p className="text-gray-300 font-heading uppercase text-sm mb-2">
                          2nd Place
                        </p>
                      </div>
                      <h3 className="font-heading text-xl font-bold mb-2">
                        {leaderboard[1].displayName}
                      </h3>
                      <p className="text-3xl font-bold text-primary mb-4">
                        {leaderboard[1].score}
                      </p>
                      <div className="text-sm text-muted-foreground">
                        {leaderboard[1].solves} challenges solved
                      </div>
                    </CyberCardContent>
                  </CyberCard>
                </div>
              )}

              {/* 1st Place */}
              {leaderboard[0] && (
                <div className="md:order-2">
                  <CyberCard variant="hero" className="h-full relative overflow-hidden">
                    <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-transparent to-primary/10" />
                    <CyberCardContent className="p-6 text-center relative z-10">
                      <div className="mb-4">
                        <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-yellow-400/20 border-2 border-yellow-400 flex items-center justify-center">
                          <Crown className="w-10 h-10 text-yellow-400" />
                        </div>
                        <p className="text-yellow-400 font-heading uppercase text-sm mb-2">
                          🏆 Champion
                        </p>
                      </div>
                      <h3 className="font-heading text-2xl font-bold mb-2">
                        {leaderboard[0].displayName}
                      </h3>
                      <p className="text-4xl font-bold text-yellow-400 mb-4">
                        {leaderboard[0].score}
                      </p>
                      <div className="text-sm text-muted-foreground">
                        {leaderboard[0].solves} challenges solved
                      </div>
                    </CyberCardContent>
                  </CyberCard>
                </div>
              )}

              {/* 3rd Place */}
              {leaderboard[2] && (
                <div className="md:order-3">
                  <CyberCard variant="interactive" className="h-full">
                    <CyberCardContent className="p-6 text-center">
                      <div className="mb-4">
                        <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-orange-400/20 border-2 border-orange-400 flex items-center justify-center">
                          <Target className="w-8 h-8 text-orange-400" />
                        </div>
                        <p className="text-orange-400 font-heading uppercase text-sm mb-2">
                          3rd Place
                        </p>
                      </div>
                      <h3 className="font-heading text-xl font-bold mb-2">
                        {leaderboard[2].displayName}
                      </h3>
                      <p className="text-3xl font-bold text-primary mb-4">
                        {leaderboard[2].score}
                      </p>
                      <div className="text-sm text-muted-foreground">
                        {leaderboard[2].solves} challenges solved
                      </div>
                    </CyberCardContent>
                  </CyberCard>
                </div>
              )}
            </motion.div>
          )}

          {/* Full Leaderboard */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <CyberCard variant="default" className="overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="px-6 py-4 text-left text-sm font-heading uppercase text-muted-foreground">
                        Rank
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-heading uppercase text-muted-foreground">
                        Player
                      </th>
                      <th className="px-6 py-4 text-left text-sm font-heading uppercase text-muted-foreground">
                        Tier
                      </th>
                      <th className="px-6 py-4 text-right text-sm font-heading uppercase text-muted-foreground">
                        Score
                      </th>
                      <th className="px-6 py-4 text-right text-sm font-heading uppercase text-muted-foreground">
                        Solves
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-12 text-center text-muted-foreground">
                          Loading leaderboard...
                        </td>
                      </tr>
                    ) : leaderboard.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-12 text-center text-muted-foreground">
                          No players yet. Be the first to solve challenges!
                        </td>
                      </tr>
                    ) : (
                      leaderboard.map((entry, index) => (
                        <motion.tr
                          key={entry.id}
                          initial={{ opacity: 0, x: -20 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: index * 0.05 }}
                          className={`border-b border-border/50 hover:bg-accent/50 transition-colors ${
                            entry.id === userId ? 'bg-primary/10' : ''
                          }`}
                        >
                          <td className={`px-6 py-4 font-heading font-bold ${getRankColor(index + 1)}`}>
                            <div className="flex items-center gap-2">
                              {getRankIcon(index + 1)}
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-3">
                              <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary flex items-center justify-center text-xs font-bold">
                                {entry.displayName.charAt(0)}
                              </div>
                              <span className="font-mono uppercase text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground border font-bold">
                                {entry.tier}
                              </span>
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <span className="font-heading font-bold">
                              {entry.displayName}
                              {entry.id === userId && (
                                <span className="ml-2 text-xs bg-primary/20 text-primary px-2 py-1 rounded">
                                  YOU
                                </span>
                              )}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-right font-mono font-bold text-primary">
                            {entry.score}
                          </td>
                          <td className="px-6 py-4 text-right text-muted-foreground">
                            {entry.solves}
                          </td>
                        </motion.tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </CyberCard>
          </motion.div>
        </div>
      </main>
    </PageTransition>
  );
};

export default Leaderboard;
