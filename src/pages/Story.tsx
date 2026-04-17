import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGame } from '@/context/GameContext';

import { useAuthentication } from '@/context/AuthContext';
import Navbar from '@/components/layout/Navbar';
import PageTransition from '@/components/layout/PageTransition';
import ParticleBackground from '@/components/layout/ParticleBackground';

const Story = () => {
  const navigate = useNavigate();
  const { selectedOperator } = useGame();
  const { token } = useAuthentication();

  useEffect(() => {
    if (!token) {
      navigate('/register');
      return;
    }

    if (!selectedOperator) {
      navigate('/operators');
      return;
    }

    // Redirect to operator-specific Story route
    navigate(`/story/operator/${selectedOperator.id}`);
  }, [navigate, token, selectedOperator]);

  return (
    <PageTransition>
      <Navbar />
      <ParticleBackground />
      <div className="min-h-screen pt-24 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p>Loading your operator dossier...</p>
        </div>
      </div>
    </PageTransition>
  );
};

export default Story;

