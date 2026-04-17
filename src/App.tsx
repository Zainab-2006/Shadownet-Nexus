import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
// import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Routes, Route, Navigate, useParams } from "react-router-dom";
import Index from "./pages/Index";
import Operators from "./pages/Operators";
import OperatorStory from "./pages/OperatorStory";
import CTF from "./pages/CTF";
import Story from "./pages/Story";
import Missions from "./pages/Missions";
import MissionRuntime from "./pages/MissionRuntime";
import Leaderboard from "./pages/Leaderboard";
import Login from "./pages/Login";
import Register from "./pages/Register";
import NotFound from "./pages/NotFound";
import StoryScene from "./pages/StoryScene";
  // Canonical story routes now require operator prefix
import { GameProvider } from '@/context/GameContext';
import { AuthProvider } from '@/context/AuthContext';
import { AudioProvider } from '@/context/AudioProvider';
import { Hud } from '@/components/Hud';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { NarratorProvider } from '@/context/NarratorContext';

const LegacyOperatorRedirect = () => {
  const { id } = useParams();

  if (!id) {
    return <Navigate to="/operators" replace />;
  }

  return <Navigate to={`/story/operator/${id}`} replace />;
};

const App = () => (
  <AudioProvider>
    <AuthProvider>
      <GameProvider>
        <NarratorProvider>
          <TooltipProvider>
            <Toaster />
            <Sonner />
            <ErrorBoundary fallback={<p className="text-destructive text-sm mt-4">Check console for details.</p>}>
              <Routes>

                <Route path="/" element={<Index />} />
                <Route path="/operators" element={<Operators />} />
                <Route path="/operator/:id" element={<LegacyOperatorRedirect />} />
                <Route path="/dashboard" element={<Index />} />
                <Route path="/solo" element={<CTF />} />
                <Route path="/story/operator/:id" element={<OperatorStory />} />
                <Route path="/story/operator/:id/scene/:sceneId" element={<StoryScene />} />
                <Route path="/story" element={<Story />} />
                <Route path="/story/*" element={<Navigate to="/operators" replace />} />
                <Route path="/missions" element={<Missions />} />
                <Route path="/missions/:missionId" element={<MissionRuntime />} />
                <Route path="/missions/:missionId/runtime" element={<MissionRuntime />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="*" element={<NotFound />} />
              </Routes>
              <Hud />
            </ErrorBoundary>
          </TooltipProvider>
        </NarratorProvider>
      </GameProvider>
    </AuthProvider>
  </AudioProvider>
);

export default App;
