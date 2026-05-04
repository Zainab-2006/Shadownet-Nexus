import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function truncate(text: string, length: number = 100): string {
  if (text.length <= length) return text;
  return text.slice(0, length) + '...';
}

import { format } from 'date-fns';

export function formatDate(date: Date | string | number, formatStr: string = 'PPP'): string {
  return format(new Date(date), formatStr);
}

export function safeJsonParse<T>(json: string, fallback: T): T {
  try {
    return JSON.parse(json) as T;
  } catch {
    return fallback;
  }
}

export function debounce<T extends (...args: unknown[]) => unknown>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

/**
 * Toast error helper (assumes sonner or react-hot-toast available)
 */
export function toastError(message: string, title?: string): void {
  // Implementation depends on toast lib, e.g.:
  // toast.error(message, { title });
  console.error(title ? `${title}: ${message}` : message);
}
