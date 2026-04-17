export const hoverLift = {
  whileHover: { y: -4, scale: 1.015 },
  transition: { duration: 0.18 },
};

export const pressScale = {
  whileTap: { scale: 0.97 },
  transition: { duration: 0.08 },
};

export const fadeSlideUp = {
  initial: { opacity: 0, y: 14 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -8 },
  transition: { duration: 0.28 },
};

export const panelReveal = {
  initial: { opacity: 0, scale: 0.98, y: 10 },
  animate: { opacity: 1, scale: 1, y: 0 },
  transition: { duration: 0.22 },
};

export const pulseSuccess = {
  initial: { scale: 1, opacity: 1 },
  animate: { scale: [1, 1.03, 1], opacity: [1, 1, 1] },
  transition: { duration: 0.45 },
};

export const shakeError = {
  initial: { x: 0 },
  animate: { x: [0, -6, 6, -4, 4, 0] },
  transition: { duration: 0.32 },
};

export const softGlowFocus = {
  whileHover: {
    boxShadow: '0 0 0 1px rgba(0,255,255,0.18), 0 0 18px rgba(0,255,255,0.08)',
  },
  transition: { duration: 0.18 },
};
