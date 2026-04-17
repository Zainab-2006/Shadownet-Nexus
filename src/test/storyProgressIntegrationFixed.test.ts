import { describe, it, expect, beforeAll, afterAll } from 'vitest';

const API_BASE = process.env.RUN_LIVE_BACKEND_TESTS === 'true' ? 'http://localhost:3001' : '';

describe.skipIf(!API_BASE)('Story Progress Integration Tests (Fixed)', () => {
  let token: string;
  let userId: string;

  beforeAll(async () => {
    const unique = Date.now();
    const registerResponse = await fetch(`${API_BASE}/api/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: `test-${unique}@example.com`,
        password: 'Password123!',
        username: `testuser${unique}`,
      }),
    });
    expect(registerResponse.status).toBe(201);
    const registerData = await registerResponse.json();
    token = registerData.token;
    userId = registerData.user.id;
    expect(userId).toBeTruthy();
  });

  it('should get initial story progress', async () => {
    const response = await fetch(`${API_BASE}/api/users/me/story-progress`, {
      headers: { 'Authorization': `Bearer ${token}` },
      credentials: 'include',
    });
    expect(response.status).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('storyProgress');
  });

  it('should set and get story progress', async () => {
    const progressData = {
      completedChapters: [1, 2],
      unlockedChapters: [1, 2, 3],
      lastPlayedChapter: 2,
    };

    const setResponse = await fetch(`${API_BASE}/api/users/me/story-progress`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      credentials: 'include',
      body: JSON.stringify({ storyProgress: JSON.stringify(progressData) }),
    });
    expect(setResponse.status).toBe(200);

    const getResponse = await fetch(`${API_BASE}/api/users/me/story-progress`, {
      headers: { 'Authorization': `Bearer ${token}` },
      credentials: 'include',
    });
    expect(getResponse.status).toBe(200);
    const getData = await getResponse.json();
    expect(JSON.parse(getData.storyProgress)).toEqual(progressData);
  });

  afterAll(async () => {
    token = '';
    userId = '';
  });
});
