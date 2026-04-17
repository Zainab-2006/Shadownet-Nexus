import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import StoryScene from '@/pages/StoryScene';
import OperatorStory from '@/pages/OperatorStory';
import * as storyApi from '@/api/storyApi';
import * as gameContext from '@/context/GameContext';
import * as operatorApi from '@/api/operatorApi';
import { AuthProvider } from '@/context/AuthContext';

// Mock entire modules
vi.mock('@/api/storyApi');
vi.mock('@/context/GameContext');
vi.mock('@/api/operatorApi');
vi.mock('@/components/three/HeroScene', () => ({ default: () => <div data-testid="hero-scene" /> }));
vi.mock('@/components/layout/ParticleBackground', () => ({ default: () => <div data-testid="particle-background" /> }));

const mockScene = {
  id: 1,
  chapterId: 1,
  sceneNumber: 1,
  content: 'Test scene content.',
  sceneType: 'CHOICE' as const,
  choices: [
    { id: 1, text: 'Choice 1', trustImpact: 5 },
    { id: 2, text: 'Choice 2', trustImpact: -3 },
  ],
};

const mockSelectedOperator = { id: '1', codename: 'TestOp' };

const MockGameProvider: React.FC<{ children: React.ReactNode; value?: any }> = ({ children, value }) => (
  <div data-testid="mock-game-provider">{children}</div>
);

const renderWithProviders = (ui: React.ReactElement, initialEntry: string, routePath: string) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[initialEntry]}>
          <MockGameProvider>
            <Routes>
              <Route path={routePath} element={ui} />
            </Routes>
          </MockGameProvider>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

describe('Story Runtime Verification', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(storyApi.useGetSceneQuery).mockReturnValue({ data: mockScene, isLoading: false } as any);
    vi.mocked(storyApi.useMakeDecisionMutation).mockReturnValue({ mutate: vi.fn(), isPending: false } as any);
    vi.mocked(gameContext.useGame).mockReturnValue({ selectedOperator: mockSelectedOperator } as any);
    vi.mocked(operatorApi.useCharacters).mockReturnValue({ data: [mockSelectedOperator], isLoading: false } as any);
  });

  it('OperatorStory renders operator details', () => {
    renderWithProviders(<OperatorStory />, '/story/operator/1', '/story/operator/:id');
    expect(screen.getByText('TestOp')).toBeInTheDocument();
  });

  it('StoryScene renders scene content and choices', () => {
    renderWithProviders(<StoryScene />, '/story/operator/1/scene/1', '/story/operator/:id/scene/:sceneId');
    expect(screen.getByText('Test scene content.')).toBeInTheDocument();
    expect(screen.getByText('Choice 1')).toBeInTheDocument();
    expect(screen.getByText('Choice 2')).toBeInTheDocument();
  });

  it('StoryScene handles choice interaction', async () => {
    const mockMutate = vi.fn();
    vi.mocked(storyApi.useMakeDecisionMutation).mockReturnValue({ mutate: mockMutate, isPending: false } as any);

    const { getByText } = renderWithProviders(<StoryScene />, '/story/operator/1/scene/1', '/story/operator/:id/scene/:sceneId');

    fireEvent.click(getByText('Choice 1'));
    await waitFor(() => expect(mockMutate).toHaveBeenCalled());
  });

  it('StoryScene shows loading state', () => {
    vi.mocked(storyApi.useGetSceneQuery).mockReturnValue({ data: null, isLoading: true } as any);
    renderWithProviders(<StoryScene />, '/story/operator/1/scene/1', '/story/operator/:id/scene/:sceneId');
    expect(screen.getByText('Loading scene...')).toBeInTheDocument();
  });
});
