import React, { createContext, useContext, useState, ReactNode, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import { CyberButton } from '@/components/ui/cyber-button';
import { CyberCard } from '@/components/ui/cyber-card';
import { toast } from 'sonner';

export type NarratorEvent =
  | 'ONBOARDING'
  | 'SOLO_3_FAILS'
  | 'MISSION_BRIEF'
  | 'MISSION_DEBRIEF'
  | 'STORY_TRANSITION'
  | 'TRUST_GAIN'
  | 'TRUST_LOSS'
  | 'MISSION_SUCCESS'
  | 'MISSION_FAILURE';

interface NarratorPayload {
  event: NarratorEvent;
  title: string;
  message: string;
  context?: Record<string, unknown>;
  dismissible?: boolean;
}

interface NarratorContextType {
  openNarrator: (payload: NarratorPayload) => void;
  closeNarrator: () => void;
  isOpen: boolean;
}

const NarratorContext = createContext<NarratorContextType | null>(null);

export const NarratorProvider = ({ children }: { children: ReactNode }) => {
  const [payload, setPayload] = useState<NarratorPayload | null>(null);

  const openNarrator = useCallback((newPayload: NarratorPayload) => {
    setPayload(newPayload);
    toast(newPayload.message, { duration: 5000 });
  }, []);

  const closeNarrator = useCallback(() => {
    setPayload(null);
  }, []);

  return (
    <NarratorContext.Provider value={{ openNarrator, closeNarrator, isOpen: !!payload }}>
      {children}
      <AnimatePresence>
        {payload && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-end justify-center p-6 pointer-events-none"
          >
            <motion.div
              initial={{ y: 100, scale: 0.95 }}
              animate={{ y: 0, scale: 1 }}
              exit={{ y: 100, scale: 0.95 }}
              className="w-full max-w-2xl pointer-events-auto"
            >
              <CyberCard variant="hero" className="backdrop-blur-xl shadow-2xl">
                <div className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <h3 className="font-heading text-lg font-bold">{payload.title}</h3>
                    {payload.dismissible !== false && (
                      <CyberButton variant="ghost" size="sm" onClick={closeNarrator}>
                        <X className="w-4 h-4" />
                      </CyberButton>
                    )}
                  </div>
                  <p className="text-sm leading-relaxed mb-4">{payload.message}</p>
                  {payload.context && (
                    <pre className="text-xs bg-muted p-3 rounded font-mono overflow-auto max-h-32">
                      {JSON.stringify(payload.context, null, 2)}
                    </pre>
                  )}
                </div>
              </CyberCard>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </NarratorContext.Provider>
  );
};

export const useNarrator = () => {
  const context = useContext(NarratorContext);
  if (!context) throw new Error('useNarrator must be used within NarratorProvider');
  return context;
};
