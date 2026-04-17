import { Crosshair, Radio, ShieldAlert } from 'lucide-react';
import { cn } from '@/lib/utils';

type PlayerDirectiveProps = {
  mode: string;
  directive: string;
  why: string;
  next: string;
  tone?: 'cyan' | 'amber' | 'red';
  className?: string;
};

const toneClass = {
  cyan: 'border-cyan-400/15 bg-cyan-400/[0.04]',
  amber: 'border-amber-400/20 bg-amber-500/[0.06]',
  red: 'border-red-500/20 bg-red-500/[0.06]',
} as const;

export const PlayerDirective = ({ mode, directive, why, next, tone = 'cyan', className }: PlayerDirectiveProps) => (
  <section className={cn('snx-panel-strong relative mb-8 overflow-hidden p-5', toneClass[tone], className)}>
    <div className="snx-grid-overlay" />
    <div className="relative grid gap-4 md:grid-cols-3">
      <div>
        <div className="mb-2 flex items-center gap-2 snx-kicker">
          <Radio className="h-4 w-4" />
          {mode}
        </div>
        <p className="snx-body text-slate-100">{directive}</p>
      </div>
      <div>
        <div className="mb-2 flex items-center gap-2 snx-kicker text-amber-300/80">
          <ShieldAlert className="h-4 w-4" />
          Why it matters
        </div>
        <p className="snx-body">{why}</p>
      </div>
      <div>
        <div className="mb-2 flex items-center gap-2 snx-kicker text-emerald-300/80">
          <Crosshair className="h-4 w-4" />
          Next move
        </div>
        <p className="snx-body">{next}</p>
      </div>
    </div>
  </section>
);
