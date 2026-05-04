import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGame } from '@/context/GameContext';

import { useAuthentication } from '@/context/AuthContext.hooks';
import Navbar from '@/components/layout/Navbar';
import PageTransition from '@/components/layout/PageTransition';
import ParticleBackground from '@/components/layout/ParticleBackground';

const Story = () => {
  const navigate = useNavigate();
  const { selectedOperator, isInitializing, isLoading, gameState } = useGame();
  const { token, isValidating } = useAuthentication();
  const hasStoredToken = Boolean(token || localStorage.getItem('token'));

  useEffect(() => {
    if (isValidating || isInitializing || isLoading) {
      return;
    }

    if (!hasStoredToken) {
      navigate('/login', { replace: true });
      return;
    }

    const operatorId = selectedOperator?.id || gameState.selectedOperator;

    if (!operatorId) {
      navigate('/operators');
      return;
    }

    navigate(`/story/operator/${operatorId}`, { replace: true });
  }, [
    gameState.selectedOperator,
    hasStoredToken,
    isInitializing,
    isLoading,
    isValidating,
    navigate,
    selectedOperator?.id,
  ]);

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
