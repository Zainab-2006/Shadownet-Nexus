import { describe, it, expect, vi } from 'vitest';
import { deriveProgressFromSolves, StoryProgress, getChapterStatus, canUnlockNextChapter } from '@/utils/storyUtils';
import { chapters } from '@/data/gameData';

// Mock chapters length for tests
vi.mock('@/data/gameData', () => ({
  chapters: [{ id: 1 }, { id: 2 }, { id: 3 }, { id: 4 }, { id: 5 }, { id: 6 }, { id: 7 }, { id: 8 }],
}));

describe('deriveProgressFromSolves', () => {
  it('starts with Chapter 1 unlocked, 0% progress', () => {
    const solves: string[] = [];
    const progress = deriveProgressFromSolves(solves);
    
    expect(progress.unlockedChapters).toEqual([1]);
    expect(progress.completedChapters).toEqual([]);
    expect(progress.currentChapter).toBe(1);
    expect(progress.progressPercentage).toBe(13); // 1/8 ≈ 12.5 → 13
  });

  it('unlocks Ch2 after web-001 + osint-001', () => {
    const solves = ['web-001', 'osint-001'];
    const progress = deriveProgressFromSolves(solves);
    
    expect(progress.unlockedChapters).toEqual([1, 2]);
    expect(progress.completedChapters).toEqual([1]);
    expect(progress.currentChapter).toBe(2);
    expect(progress.progressPercentage).toBe(25); // 2/8
  });

  it('unlocks up to Ch4 with cumulative gates', () => {
    const solves = ['web-001', 'osint-001', 'crypto-001', 'misc-001'];
    const progress = deriveProgressFromSolves(solves);
    
    expect(progress.unlockedChapters).toEqual([1, 2, 3, 4]);
    expect(progress.completedChapters).toEqual([1, 2, 3]);
    expect(progress.currentChapter).toBe(4);
    expect(progress.progressPercentage).toBe(50); // 4/8
  });

  it('completes all chapters with full gate solves', () => {
    const fullSolves = [
      'web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002',
      'forensics-001', 'crypto-002', 'pwn-002'
    ];
    const progress = deriveProgressFromSolves(fullSolves);
    
    expect(progress.unlockedChapters).toEqual([1,2,3,4,5,6,7,8]);
    expect(progress.completedChapters).toEqual([1,2,3,4,5,6,7]);
    expect(progress.currentChapter).toBe(8);
    expect(progress.progressPercentage).toBe(100);
  });

  it('partial gates unlock only up to met requirements', () => {
    // Missing pwn-002 for Ch8
    const solves = ['web-001', 'osint-001', 'crypto-001', 'misc-001', 'web-002', 'forensics-001', 'crypto-002'];
    const progress = deriveProgressFromSolves(solves);
    
    expect(progress.unlockedChapters).toHaveLength(7);
    expect(progress.unlockedChapters).not.toContain(8);
    expect(progress.progressPercentage).toBe(88); // 7/8 ≈ 87.5 → 88
  });
});

describe('getChapterStatus', () => {
  const sampleProgress: StoryProgress = {
    unlockedChapters: [1, 2, 3],
    completedChapters: [1],
    currentChapter: 2,
    progressPercentage: 37,
  };

  it('correctly identifies locked chapters', () => {
    expect(getChapterStatus(4, sampleProgress)).toBe('locked');
  });

  it('identifies current chapter', () => {
    expect(getChapterStatus(2, sampleProgress)).toBe('current');
  });

  it('identifies unlocked but not current', () => {
    expect(getChapterStatus(3, sampleProgress)).toBe('unlocked');
  });

  it('identifies completed chapters', () => {
    expect(getChapterStatus(1, sampleProgress)).toBe('completed');
  });
});

describe('canUnlockNextChapter', () => {
  it('returns true when chapters remain', () => {
    const partial: StoryProgress = { unlockedChapters: [1,2], completedChapters: [1], currentChapter: 2, progressPercentage: 25 };
    expect(canUnlockNextChapter(partial)).toBe(true);
  });

  it('returns false when all unlocked', () => {
    const full: StoryProgress = { unlockedChapters: [1,2,3,4,5,6,7,8], completedChapters: [1,2,3,4,5,6,7], currentChapter: 8, progressPercentage: 100 };
    expect(canUnlockNextChapter(full)).toBe(false);
  });
});

