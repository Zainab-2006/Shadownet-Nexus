import React, { useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Shield, Mail, Lock, User, ChevronRight, AlertCircle, Check } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard } from '@/components/ui/cyber-card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import ParticleBackground from '@/components/layout/ParticleBackground';
import PageTransition from '@/components/layout/PageTransition';
import { useRegister } from '@/api/shadownetApi';
import { useAuthentication } from '@/context/AuthContext';
import { useNarrator } from '@/context/NarratorContext';

const passwordRequirements = [
  { label: 'At least 8 characters', test: (p: string) => p.length >= 8 },
  { label: 'Contains uppercase letter', test: (p: string) => /[A-Z]/.test(p) },
  { label: 'Contains lowercase letter', test: (p: string) => /[a-z]/.test(p) },
  { label: 'Contains number', test: (p: string) => /\d/.test(p) },
  { label: 'Contains special character', test: (p: string) => /[^A-Za-z0-9]/.test(p) },
];

const Register = () => {
  const navigate = useNavigate();
  const { openNarrator } = useNarrator();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const registerMutation = useRegister();
  const { login } = useAuthentication();

  const allRequirementsMet = passwordRequirements.every((req) => req.test(password));
  const passwordsMatch = password === confirmPassword && password.length > 0;

const handleOpenOnboarding = useCallback(() => {
    openNarrator({
      event: 'ONBOARDING',
      title: 'Nexus Access Granted',
      message: 'Welcome, Operator. Three paths are now open: Solo simulations, Missions with squad trust pressure, Story archives. Choose your operator to begin.',
      dismissible: true
    });
  }, [openNarrator]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const username = displayName.trim();
    const normalizedEmail = email.trim().toLowerCase();

    if (!username || !normalizedEmail || !password || !confirmPassword) {
      setError('Please fill in all fields');
      return;
    }

    if (!allRequirementsMet) {
      setError('Password must include upper, lower, number, and special character.');
      return;
    }

    if (!passwordsMatch) {
      setError('Passwords do not match');
      return;
    }

    try {
      const result = await registerMutation.mutateAsync({
        username,
        email: normalizedEmail,
        password
      });
      login(result.token, result.user);
      handleOpenOnboarding();
      navigate('/operators');
    } catch (err: unknown) {
      const message = err?.response?.data?.message || err?.response?.data?.error || err?.message || 'Registration failed';
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
              <h1 className="font-heading text-2xl font-bold mb-2">Join NEXUS Division</h1>
              <p className="text-muted-foreground">Create your operator account</p>
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
                <Label htmlFor="displayName" className="text-muted-foreground">Codename</Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <Input
                    id="displayName"
                    type="text"
                    placeholder="SPECTER"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    className="pl-10 bg-card border-border uppercase"
                  />
                </div>
              </div>

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
                    placeholder="********"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-10 bg-card border-border"
                  />
                </div>

                <div className="grid grid-cols-1 gap-2 mt-3 sm:grid-cols-2">
                  {passwordRequirements.map((req) => {
                    const passed = req.test(password);
                    return (
                      <div
                        key={req.label}
                        className={`flex items-center gap-1 text-xs ${passed ? 'text-success' : 'text-muted-foreground'}`}
                      >
                        <Check className={`w-3 h-3 ${passed ? 'opacity-100' : 'opacity-30'}`} />
                        {req.label}
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-muted-foreground">Confirm Password</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <Input
                    id="confirmPassword"
                    type="password"
                    placeholder="********"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className={`pl-10 bg-card border-border ${confirmPassword && !passwordsMatch ? 'border-destructive' : ''}`}
                  />
                </div>
                {confirmPassword && !passwordsMatch && (
                  <p className="text-xs text-destructive">Passwords do not match</p>
                )}
              </div>

              <div className="text-xs text-muted-foreground">
                By creating an account, you agree to our{' '}
                <a href="#" className="text-primary hover:underline">Terms of Service</a>
                {' '}and{' '}
                <a href="#" className="text-primary hover:underline">Privacy Policy</a>.
              </div>

              <CyberButton
                type="submit"
                variant="hero"
                className="w-full"
                disabled={registerMutation.isPending}
              >
...
                <ChevronRight className="w-4 h-4 ml-2" />
              </CyberButton>
            </form>

            <div className="mt-6 text-center">
              <p className="text-muted-foreground text-sm">
                Already have an account?{' '}
                <Link to="/login" className="text-primary hover:underline">
                  Sign in
                </Link>
              </p>
            </div>
          </CyberCard>
        </motion.div>
      </div>
    </PageTransition>
  );
};

export default Register;


