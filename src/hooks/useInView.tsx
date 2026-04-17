import { useEffect, useState, useRef, useCallback } from 'react';

interface UseInViewOptions {
  threshold?: number;
  triggerOnce?: boolean;
  margin?: string;
}

export const useInView = (options: UseInViewOptions = {}) => {
  const ref = useRef<HTMLDivElement>(null);
  const [inView, setInView] = useState(false);

  const callback = useCallback(([entry]: IntersectionObserverEntry[]) => {
    if (entry.isIntersecting) {
      setInView(true);
      if (options.triggerOnce) {
        observer?.disconnect();
      }
    }
  }, [options.triggerOnce]);

  useEffect(() => {
    const observer = new IntersectionObserver(callback, {
      rootMargin: options.margin || '0px',
      threshold: options.threshold || 0.1,
    });

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => observer.disconnect();
  }, [callback]);

  return [ref, inView] as const;
};

