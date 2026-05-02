import React, { createContext, useEffect } from 'react';
import { useAudio, initGlobalAudio } from '@/hooks/useAudio';
import type { AudioContextType } from './AudioProvider.hooks';

export const AudioContext = createContext<AudioContextType | null>(null);

export const AudioProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const audio = useAudio();

  useEffect(() => {
    // Initialize global audio instance
    initGlobalAudio(audio);
    
    // Start ambient loop on component mount
    const timer = setTimeout(() => {
      audio.playAmbientLoop();
    }, 500);

    return () => clearTimeout(timer);
  }, [audio]);

  const value: AudioContextType = {
    playSound: audio.playSound,
    playAmbientLoop: audio.playAmbientLoop,
    setVolume: audio.setVolume,
    toggle: audio.toggle,
    isEnabled: audio.isEnabled,
    masterVolume: audio.masterVolume,
  };

  return (
    <AudioContext.Provider value={value}>
      {children}
    </AudioContext.Provider>
  );
};
