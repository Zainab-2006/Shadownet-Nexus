import { useEffect } from 'react';
import { FadeSlideUp } from '@/systems/animation/MotionWrappers';
import { useAudioContext } from '@/context/AudioProvider.hooks';

type NarratorPanelProps = {
  text: string;
  label?: string;
};

export default function NarratorPanel({ text, label = 'Narrator' }: NarratorPanelProps) {
  const { playSound } = useAudioContext();

  useEffect(() => {
    playSound('select', 0.35);
  }, [playSound, text]);

  return (
    <FadeSlideUp>
      <div className="snx-panel border-cyan-400/20 bg-black/40 p-4 text-cyan-100">
        <div className="mb-2 snx-kicker">{label}</div>
        <p className="snx-body text-cyan-100">{text}</p>
      </div>
    </FadeSlideUp>
  );
}
