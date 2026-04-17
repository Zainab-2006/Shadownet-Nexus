import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Shield, Mail, Lock, ChevronRight, AlertCircle } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard } from '@/components/ui/cyber-card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useLogin, useUser } from '@/api/shadownetApi';
import { useAuthentication } from '@/context/AuthContext';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const loginMutation = useLogin();
  const { login } = useAuthentication();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail || !password) {
      setError('Please fill in all fields');
      return;
    }

    try {
      const result = await loginMutation.mutateAsync({ email: normalizedEmail, password });
      // Unified token/user contract
      login(result.token, result.user);
      navigate('/operators');
  } catch (err: unknown) {
      const message = err?.response?.data?.message || err?.response?.data?.error || err?.message || 'Login failed';
      setError(message);
    }
  };

  return (
    <PageTransition>
      <ParticleBackground />

      <div className="min-h-screen flex items-center justify-center p-4 py-12">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md"
        >
          {/* Logo */}
          <Link to="/" className="flex items-center justify-center gap-3 mb-8">
            <div className="relative w-12 h-12 flex items-center justify-center">
              <div className="absolute inset-0 bg-gradient-primary rounded-lg opacity-80" />
              <Shield className="relative w-7 h-7 text-primary-foreground" />
            </div>
            <span className="font-heading text-2xl font-bold">
              <span className="text-primary text-glow-primary">NEXUS</span>
              <span className="text-foreground ml-1">CTF</span>
            </span>
          </Link>

          <CyberCard variant="hero" className="p-8">
            <div className="text-center mb-8">
              <h1 className="font-heading text-2xl font-bold mb-2">NEXUS Operator Login</h1>
              <p className="text-muted-foreground">Access the network</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex items-center gap-2 p-3 rounded-lg bg-destructive/20 text-destructive"
                >
                  <AlertCircle className="w-4 h-4" />
                  <span className="text-sm">{error}</span>
                </motion.div>
              )}

              <div className="space-y-2">
                <Label htmlFor="email" className="text-muted-foreground">Email</Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <Input
                    id="email"
                    type="email"
                    placeholder="operator@nexus.io"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="pl-10 bg-card border-border"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-muted-foreground">Password</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-10 bg-card border-border"
                  />
                </div>
              </div>

              <div className="text-xs text-muted-foreground">
                Don't have an account?{' '}
                <Link to="/register" className="text-primary hover:underline">
                  Create one
                </Link>
              </div>

              <CyberButton type="submit" variant="hero" className="w-full" disabled={loginMutation.isPending}>
                {loginMutation.isPending ? 'Authenticating...' : 'Access Network'}
                <ChevronRight className="w-4 h-4 ml-2" />
              </CyberButton>
            </form>
          </CyberCard>
        </motion.div>
      </div>
    </PageTransition>
  );
};

export default Login;



