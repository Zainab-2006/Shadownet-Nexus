import { chapters } from '@/data/gameData';

// Challenge-to-chapter unlock mapping (cumulative)
const CHALLENGE_GATES: Record<number, string[]> = {
  1: ['web-001'],                    // First Contact
  2: ['web-001', 'osint-001'],       // Ghost Protocol
  3: ['web-001', 'osint-001', 'crypto-001'], // Broken Cipher
  4: ['web-001', 'osint-001', 'crypto-001', 'misc-001'], // Critical Mass
  5: ['web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002'], // Counter-Strike
  6: ['web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002', 'forensics-001'], // Rising Phoenix
  7: ['web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002', 'forensics-001', 'crypto-002'], // Zero Hour
  8: ['web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002', 'forensics-001', 'crypto-002', 'pwn-002'] // Endgame
};

export interface StoryProgress {
  unlockedChapters: number[];
  completedChapters: number[];
  currentChapter: number;
  progressPercentage: number;
}

/**
 * Derive story progress from user's solved CTF challenges.
 * Unlocks chapters when gate challenges are solved.
 */
export function deriveProgressFromSolves(solves: string[]): StoryProgress {
  const progress: StoryProgress = {
    unlockedChapters: [1], // Ch1 always unlocked
    completedChapters: [],
    currentChapter: 1,
    progressPercentage: 0,
  };

  for (let chapterId = 1; chapterId <= chapters.length; chapterId++) {
    const gate = CHALLENGE_GATES[chapterId];
    if (!gate) continue;

    const chapterUnlocked = gate.every(challengeId => solves.includes(challengeId));
    
    if (chapterUnlocked) {
      if (!progress.unlockedChapters.includes(chapterId)) {
        progress.unlockedChapters.push(chapterId);
      }
      progress.currentChapter = Math.max(progress.currentChapter, chapterId);
      
      // Mark previous chapters as completed
      if (chapterId > 1) {
        progress.completedChapters.push(chapterId - 1);
      }
    }
  }

  // Dedupe arrays (defensive)
  progress.unlockedChapters = [...new Set(progress.unlockedChapters)].sort((a, b) => a - b);
  progress.completedChapters = [...new Set(progress.completedChapters)].sort((a, b) => a - b);

  // Calculate progress % (unlocked / total chapters)
  progress.progressPercentage = Math.round((progress.unlockedChapters.length / chapters.length) * 100);

  return progress;
}

// Story state transition helpers
export function canUnlockNextChapter(currentProgress: StoryProgress): boolean {
  return currentProgress.unlockedChapters.length < chapters.length;
}

export function getChapterStatus(chapterId: number, progress: StoryProgress): 'locked' | 'unlocked' | 'current' | 'completed' {
  if (progress.completedChapters.includes(chapterId)) return 'completed';
  if (progress.unlockedChapters.includes(chapterId)) {
    return progress.currentChapter === chapterId ? 'current' : 'unlocked';
  }
  return 'locked';
}

