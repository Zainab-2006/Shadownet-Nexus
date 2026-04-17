import { useCallback, useEffect, useRef } from 'react';

interface AudioContextWithOld extends AudioContext {
  [key: string]: unknown;
}

type SoundType = 
  | 'click' | 'hover' | 'success' | 'failure' | 'alert' 
  | 'unlock' | 'mission_start' | 'level_up' | 'error' | 'select';

type AudioContextType = typeof window.AudioContext;

/**
 * Procedural Audio System - Generates sound effects on-the-fly using Web Audio API
 * No external audio files needed!
 */
export const useAudio = () => {
  const audioContextRef = useRef<AudioContextWithOld | null>(null);
  const masterVolumeRef = useRef(0.3);
  const isEnabledRef = useRef(true);

  // Initialize Audio Context
  useEffect(() => {
    const initAudio = () => {
      if (!audioContextRef.current) {
        const AudioContext = (window.AudioContext || ((window as unknown) as Record<string, unknown>).webkitAudioContext) as AudioContextType;
        audioContextRef.current = new AudioContext();
      }
    };

    // Auto-init on first user interaction
    const initOnInteraction = () => {
      initAudio();
      document.removeEventListener('click', initOnInteraction);
    };
    document.addEventListener('click', initOnInteraction);

    return () => {
      document.removeEventListener('click', initOnInteraction);
    };
  }, []);

  /**
   * Play procedural sound effect
   */
  const playSound = useCallback((type: SoundType, volume: number = 1) => {
    if (!isEnabledRef.current || !audioContextRef.current) return;

    const ctx = audioContextRef.current;
    const now = ctx.currentTime;
    const masterVol = masterVolumeRef.current * volume;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    const filter = ctx.createBiquadFilter();

    // Route: osc → filter → gain → destination
    osc.connect(filter);
    filter.connect(gain);
    gain.connect(ctx.destination);

    // Default values
    gain.gain.setValueAtTime(masterVol, now);
    filter.type = 'highpass';
    filter.frequency.setValueAtTime(1000, now);

    switch (type) {
      case 'click':
        // Short, punchy beep
        osc.frequency.setValueAtTime(800, now);
        osc.frequency.exponentialRampToValueAtTime(600, now + 0.05);
        osc.type = 'sine';
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.05);
        osc.start(now);
        osc.stop(now + 0.05);
        break;

      case 'hover':
        // Ascending chirp
        osc.frequency.setValueAtTime(400, now);
        osc.frequency.exponentialRampToValueAtTime(800, now + 0.15);
        osc.type = 'sine';
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
        osc.start(now);
        osc.stop(now + 0.15);
        break;

      case 'success': {
        // Ascending double beep (like a level-up)
        const successOsc = ctx.createOscillator();
        const successGain = ctx.createGain();
        successOsc.connect(successGain);
        successGain.connect(ctx.destination);
        successGain.gain.setValueAtTime(masterVol, now);

        // First note
        osc.frequency.setValueAtTime(600, now);
        osc.frequency.exponentialRampToValueAtTime(800, now + 0.2);
        osc.type = 'sine';
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.2);

        // Second note (offset)
        successOsc.frequency.setValueAtTime(800, now + 0.15);
        successOsc.frequency.exponentialRampToValueAtTime(1200, now + 0.35);
        successOsc.type = 'sine';
        successGain.gain.setValueAtTime(masterVol, now + 0.15);
        successGain.gain.exponentialRampToValueAtTime(0.01, now + 0.35);

        osc.start(now);
        osc.stop(now + 0.2);
        successOsc.start(now + 0.15);
        successOsc.stop(now + 0.35);
        break;
      }

      case 'failure':
        // Descending buzz
        osc.frequency.setValueAtTime(800, now);
        osc.frequency.exponentialRampToValueAtTime(200, now + 0.3);
        osc.type = 'square';
        filter.frequency.setValueAtTime(500, now);
        filter.frequency.exponentialRampToValueAtTime(200, now + 0.3);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
        osc.start(now);
        osc.stop(now + 0.3);
        break;

      case 'alert': {
        // Pulsing alarm tone
        const alertOsc = ctx.createOscillator();
        const alertGain = ctx.createGain();
        alertOsc.connect(alertGain);
        alertGain.connect(ctx.destination);

        osc.frequency.setValueAtTime(1000, now);
        osc.type = 'square';
        gain.gain.setValueAtTime(masterVol * 0.6, now);

        // Pulse effect
        gain.gain.setValueAtTime(masterVol * 0.6, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.4);

        osc.start(now);
        osc.stop(now + 0.1);
        osc.start(now + 0.15);
        osc.stop(now + 0.25);
        osc.start(now + 0.35);
        osc.stop(now + 0.4);
        break;
      }

      case 'unlock':
        // Rising magical sound
        osc.frequency.setValueAtTime(300, now);
        osc.frequency.exponentialRampToValueAtTime(1500, now + 0.4);
        osc.type = 'triangle';
        filter.frequency.setValueAtTime(500, now);
        filter.frequency.exponentialRampToValueAtTime(3000, now + 0.4);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.4);
        osc.start(now);
        osc.stop(now + 0.4);
        break;

      case 'mission_start':
        // Deep notification sound
        osc.frequency.setValueAtTime(200, now);
        osc.frequency.exponentialRampToValueAtTime(400, now + 0.3);
        osc.type = 'sine';
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(500, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
        osc.start(now);
        osc.stop(now + 0.3);
        break;

      case 'level_up': {
        // Victorious fanfare (3-note ascent)
        const notes = [440, 550, 660]; // A, C#, E
        notes.forEach((freq, idx) => {
          const noteOsc = ctx.createOscillator();
          const noteGain = ctx.createGain();
          noteOsc.connect(noteGain);
          noteGain.connect(ctx.destination);
          noteOsc.frequency.setValueAtTime(freq, now + idx * 0.15);
          noteOsc.type = 'sine';
          noteGain.gain.setValueAtTime(masterVol, now + idx * 0.15);
          noteGain.gain.exponentialRampToValueAtTime(0.01, now + idx * 0.15 + 0.2);
          noteOsc.start(now + idx * 0.15);
          noteOsc.stop(now + idx * 0.15 + 0.2);
        });
        break;
      }

      case 'error':
        // System error buzz
        osc.frequency.setValueAtTime(1200, now);
        osc.frequency.exponentialRampToValueAtTime(400, now + 0.2);
        osc.type = 'sawtooth';
        filter.frequency.setValueAtTime(800, now);
        filter.type = 'highpass';
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.2);
        osc.start(now);
        osc.stop(now + 0.2);
        break;

      case 'select':
        // Soft confirmation
        osc.frequency.setValueAtTime(700, now);
        osc.frequency.exponentialRampToValueAtTime(900, now + 0.1);
        osc.type = 'sine';
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
        osc.start(now);
        osc.stop(now + 0.1);
        break;
    }
  }, []);

  /**
   * Play background ambient loop music
   */
  const playAmbientLoop = useCallback(() => {
    if (!isEnabledRef.current || !audioContextRef.current) return;

    const ctx = audioContextRef.current;

    // Create a simple ambient pad sound using oscillators
    const ambientFreq = 55; // Very low bass
    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gain = ctx.createGain();
    const filter = ctx.createBiquadFilter();

    osc1.type = 'sine';
    osc1.frequency.value = ambientFreq;
    osc2.type = 'sine';
    osc2.frequency.value = ambientFreq * 1.5;

    filter.type = 'lowpass';
    filter.frequency.value = 200;
    filter.Q.value = 1;

    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(filter);
    filter.connect(ctx.destination);

    gain.gain.setValueAtTime(masterVolumeRef.current * 0.1, ctx.currentTime);

    osc1.start();
    osc2.start();

    // Create a long decay envelope
    const decayDuration = 30; // 30 seconds
    gain.gain.exponentialRampToValueAtTime(
      0.001,
      ctx.currentTime + decayDuration
    );

    // Stop and restart for loop effect
    setTimeout(() => {
      osc1.stop();
      osc2.stop();
      playAmbientLoop(); // Loop
    }, decayDuration * 1000);
  }, []);

  /**
   * Set master volume (0-1)
   */
  const setVolume = useCallback((vol: number) => {
    masterVolumeRef.current = Math.max(0, Math.min(1, vol));
  }, []);

  /**
   * Toggle audio on/off
   */
  const toggle = useCallback(() => {
    isEnabledRef.current = !isEnabledRef.current;
    return isEnabledRef.current;
  }, []);

  return {
    playSound,
    playAmbientLoop,
    setVolume,
    toggle,
    isEnabled: isEnabledRef.current,
    masterVolume: masterVolumeRef.current,
  };
};

// Export a singleton instance for global use
let audioInstance: ReturnType<typeof useAudio> | null = null;

export const getAudioInstance = () => {
  return audioInstance;
};

export const initGlobalAudio = (instance: ReturnType<typeof useAudio>) => {
  audioInstance = instance;
};
