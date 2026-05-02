import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Menu, X, Shield, Target, Trophy, BookOpen, User, LogOut } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { cn } from '@/lib/utils';
import { useAuthentication } from '@/context/AuthContext.hooks';

const navLinks = [
  { href: '/operators', label: 'Operators', icon: User },
  { href: '/solo', label: 'Solo', icon: Trophy },
  { href: '/missions', label: 'Missions', icon: Target },
  { href: '/story', label: 'Story', icon: BookOpen },
  { href: '/leaderboard', label: 'Leaderboard', icon: Trophy },
];

const Navbar = () => {

  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const location = useLocation();
  const { user, token, logout } = useAuthentication();
  const isAuthenticated = Boolean(token);
  const displayName = user?.displayName || user?.username || user?.email || 'Operator';

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    setIsMobileOpen(false);
  }, [location.pathname]);

  return (
    <>
      <motion.nav
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
        className={cn(
          "fixed top-0 left-0 right-0 z-50 transition-all duration-300",
          isScrolled
            ? "bg-background/80 backdrop-blur-xl border-b border-border/50 shadow-card"
            : "bg-transparent"
        )}
      >
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between h-16 md:h-20">
            {/* Logo */}
            <Link to="/" className="flex items-center gap-3 group">
              <div className="relative w-10 h-10 flex items-center justify-center">
                <div className="absolute inset-0 bg-gradient-primary rounded-lg opacity-80 group-hover:opacity-100 transition-opacity" />
                <Shield className="relative w-6 h-6 text-primary-foreground" />
              </div>
              <span className="font-heading text-xl font-bold tracking-wider">
                <span className="text-primary text-glow-primary">SHADOWNET</span>
                <span className="text-foreground ml-1">NEXUS</span>
              </span>
            </Link>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center gap-1">
              {navLinks.map((link) => {
                const isActive = location.pathname === link.href;
                const Icon = link.icon;
                
                return (
                  <Link
                    key={link.href}
                    to={link.href}
                    className={cn(
                      "relative px-4 py-2 font-heading text-sm font-medium tracking-wide transition-all duration-300",
                      "hover:text-primary",
                      isActive ? "text-primary" : "text-muted-foreground"
                    )}
                  >
                    <span className="flex items-center gap-2">
                      <Icon className="w-4 h-4" />
                      {link.label}
                    </span>
                    {isActive && (
                      <motion.div
                        layoutId="navbar-indicator"
                        className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-primary"
                        transition={{ type: "spring", stiffness: 380, damping: 30 }}
                      />
                    )}
                  </Link>
                );
              })}
            </div>

            {/* Desktop Actions */}
            <div className="hidden md:flex items-center gap-3">
              {isAuthenticated ? (
                <>
                  <div className="flex items-center gap-2 px-3 py-2 rounded-md border border-primary/30 bg-primary/10 text-primary font-heading text-xs uppercase tracking-wider">
                    <User className="w-4 h-4" />
                    {displayName}
                  </div>
                  <CyberButton variant="ghost" size="sm" onClick={logout}>
                    <LogOut className="w-4 h-4 mr-2" />
                    Logout
                  </CyberButton>
                </>
              ) : (
                <>
                  <CyberButton variant="ghost" size="sm" asChild>
                    <Link to="/login">
                      <User className="w-4 h-4 mr-2" />
                      Login
                    </Link>
                  </CyberButton>
                  <CyberButton variant="primary" size="sm" asChild>
                    <Link to="/register">Join the Network</Link>
                  </CyberButton>
                </>
              )}
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setIsMobileOpen(!isMobileOpen)}
              className="md:hidden p-2 text-foreground hover:text-primary transition-colors"
            >
              {isMobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </motion.nav>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isMobileOpen && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.3 }}
            className="fixed inset-x-0 top-16 z-40 md:hidden bg-background/95 backdrop-blur-xl border-b border-border"
          >
            <div className="container mx-auto px-4 py-6 space-y-2">
              {navLinks.map((link) => {
                const Icon = link.icon;
                const isActive = location.pathname === link.href;
                
                return (
                  <Link
                    key={link.href}
                    to={link.href}
                    className={cn(
                      "flex items-center gap-3 px-4 py-3 rounded-lg font-heading text-sm font-medium tracking-wide transition-all",
                      isActive
                        ? "bg-primary/10 text-primary"
                        : "text-muted-foreground hover:bg-accent hover:text-foreground"
                    )}
                  >
                    <Icon className="w-5 h-5" />
                    {link.label}
                  </Link>
                );
              })}
              <div className="pt-4 space-y-2">
                {isAuthenticated ? (
                  <>
                    <div className="px-4 py-3 rounded-lg border border-primary/30 bg-primary/10 text-primary font-heading text-sm">
                      Signed in as {displayName}
                    </div>
                    <CyberButton variant="outline" className="w-full" onClick={logout}>Logout</CyberButton>
                  </>
                ) : (
                  <>
                    <CyberButton variant="outline" className="w-full" asChild>
                      <Link to="/login">Login</Link>
                    </CyberButton>
                    <CyberButton variant="primary" className="w-full" asChild>
                      <Link to="/register">Join the Network</Link>
                    </CyberButton>
                  </>
                )}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default Navbar;
