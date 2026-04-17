import type { ReactNode } from 'react';
import { motion } from 'framer-motion';
import { fadeSlideUp, hoverLift, panelReveal, pressScale, pulseSuccess, shakeError, softGlowFocus } from './motionPresets';

export const FadeSlideUp = ({ children }: { children: ReactNode }) => <motion.div {...fadeSlideUp}>{children}</motion.div>;
export const HoverLift = ({ children }: { children: ReactNode }) => <motion.div {...hoverLift}>{children}</motion.div>;
export const Pressable = ({ children }: { children: ReactNode }) => <motion.div {...pressScale}>{children}</motion.div>;
export const RevealPanel = ({ children }: { children: ReactNode }) => <motion.div {...panelReveal}>{children}</motion.div>;
export const GlowFocus = ({ children }: { children: ReactNode }) => <motion.div {...softGlowFocus}>{children}</motion.div>;
export const SuccessPulse = ({ children }: { children: ReactNode }) => <motion.div {...pulseSuccess}>{children}</motion.div>;
export const ErrorShake = ({ children }: { children: ReactNode }) => <motion.div {...shakeError}>{children}</motion.div>;
