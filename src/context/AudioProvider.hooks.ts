import { useContext } from 'react';
import { AudioContext } from './AudioProvider';

export interface AudioContextType {
  playSound: (type: string, volume?: number) => void;
  playAmbientLoop: () => void;
  setVolume: (vol: number) => void;
  toggle: () => boolean;
  isEnabled: boolean;
  masterVolume: number;
}

export const useAudioContext = () => {
  const context = useContext(AudioContext);
  if (!context) {
    throw new Error('useAudioContext must be used within AudioProvider');
  }
  return context;
};
