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
        observerRef.current?.disconnect();
      }
    }
  }, [options.triggerOnce]);

  const observerRef = useRef<IntersectionObserver | null>(null);

  useEffect(() => {
    observerRef.current = new IntersectionObserver(callback, {
      rootMargin: options.margin || '0px',
      threshold: options.threshold || 0.1,
    });

    if (ref.current) {
      observerRef.current.observe(ref.current);
    }

    return () => {
      observerRef.current?.disconnect();
    };
  }, [callback, options]); // Added full options object


  return [ref, inView] as const;
};

