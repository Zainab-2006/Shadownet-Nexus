# 👨‍💻 SHADOWNET NEXUS - Developer Integration Guide

## Overview

This guide explains how to extend the gamification system with backend APIs, database integration, and advanced features.

---

## 🔌 Backend Integration Points

### **Phase 1: REST API Endpoints** (Immediate)

#### **1. Character API**
```typescript
// GET /api/characters
// Returns all playable operators

app.get('/api/characters', (req, res) => {
  const characters = db.prepare(
    'SELECT * FROM characters WHERE is_playable = 1'
  ).all();
  
  res.json({
    characters: characters.map(c => ({
      id: c.id,
      codename: c.codename,
      realName: c.real_name,
      imageUrl: `/characters/${c.id}.jpg`,
      role: c.role,
      skills: JSON.parse(c.skills),
      stats: {
        attack: c.stat_attack,
        defense: c.stat_defense,
        speed: c.stat_speed,
        tech: c.stat_tech
      }
    }))
  });
});
```

#### **2. Challenge Generation API**
```typescript
// POST /api/generate/challenge
// Query: ?category=web&difficulty=hard&seed=12345

import { generatePCGChallenge } from '../src/lib/procedural';

app.post('/api/generate/challenge', (req, res) => {
  const { category, difficulty, seed } = req.query;
  
  const challenge = generatePCGChallenge(Number(seed));
  
  // Optionally validate/update challenge in DB
  db.prepare(`
    INSERT INTO pcg_challenges (id, name, category, flag, points, created_at)
    VALUES (?, ?, ?, ?, ?, datetime('now'))
  `).run(challenge.id, challenge.name, challenge.category, challenge.flag, challenge.points);
  
  res.json(challenge);
});
```

#### **3. Mission Generation API**
```typescript
// POST /api/generate/mission
// Query: ?chapter=1&operatorId=kai

import { generatePCGMission } from '../src/lib/procedural';

app.post('/api/generate/mission', (req, res) => {
  const { chapter, operatorId } = req.query;
  const seed = Date.now();
  
  const mission = generatePCGMission(seed, Number(chapter));
  
  res.json({
    ...mission,
    operatorId,
    sessionId: req.session.id // from session middleware
  });
});
```

#### **4. Flag Submission API**
```typescript
// POST /api/submit-flag
// Body: { challengeId, flag, operatorId }

app.post('/api/submit-flag', async (req, res) => {
  const { challengeId, flag, operatorId } = req.body;
  const userId = req.session.userId;
  
  // Fetch correct flag
  const challenge = db.prepare(
    'SELECT flag, points FROM challenges WHERE id = ?'
  ).get(challengeId);
  
  if (!challenge) {
    return res.status(404).json({ error: 'Challenge not found' });
  }
  
  if (challenge.flag === flag) {
    // Award points
    db.prepare(`
      INSERT INTO user_solves (user_id, challenge_id, operator_id, solved_at)
      VALUES (?, ?, ?, datetime('now'))
    `).run(userId, challengeId, operatorId);
    
    // Update score
    db.prepare(`
      UPDATE users SET score = score + ? WHERE id = ?
    `).run(challenge.points, userId);
    
    res.json({
      success: true,
      points: challenge.points,
      message: 'Flag correct!'
    });
  } else {
    res.status(400).json({ success: false, message: 'Incorrect flag' });
  }
});
```

#### **5. Leaderboard API**
```typescript
// GET /api/leaderboard
// Query: ?limit=50&operator=all

app.get('/api/leaderboard', (req, res) => {
  const limit = Number(req.query.limit) || 50;
  const operator = req.query.operator || 'all';
  
  let query = `
    SELECT u.id, u.username, u.score, u.avatar,
           COUNT(DISTINCT s.challenge_id) as challenges_solved
    FROM users u
    LEFT JOIN user_solves s ON u.id = s.user_id
  `;
  
  if (operator !== 'all') {
    query += ` WHERE u.current_operator_id = '${operator}'`;
  }
  
  query += ` GROUP BY u.id ORDER BY u.score DESC LIMIT ?`;
  
  const leaderboard = db.prepare(query).all(limit);
  
  res.json({ leaderboard, timestamp: new Date().toISOString() });
});
```

#### **6. Trust Update API**
```typescript
// POST /api/trust/update
// Body: { operatorId, delta }

app.post('/api/trust/update', (req, res) => {
  const { operatorId, delta } = req.body;
  const userId = req.session.userId;
  
  const current = db.prepare(`
    SELECT trust FROM user_trust WHERE user_id = ? AND operator_id = ?
  `).get(userId, operatorId)?.trust ?? 0;
  
  const newTrust = Math.max(-100, Math.min(100, current + delta));
  
  db.prepare(`
    INSERT OR REPLACE INTO user_trust (user_id, operator_id, trust, updated_at)
    VALUES (?, ?, ?, datetime('now'))
  `).run(userId, operatorId, newTrust);
  
  res.json({ operatorId, trust: newTrust });
});
```

---

## 🗄️ Database Schema

### **Required Tables**

```sql
-- Users (extend existing)
CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  avatar TEXT,
  score INTEGER DEFAULT 0,
  current_operator_id TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login DATETIME
);

-- User Trust System
CREATE TABLE IF NOT EXISTS user_trust (
  user_id TEXT NOT NULL,
  operator_id TEXT NOT NULL,
  trust INTEGER DEFAULT 0, -- -100 to +100
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, operator_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (operator_id) REFERENCES characters(id)
);

-- Challenge Solves
CREATE TABLE IF NOT EXISTS user_solves (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  challenge_id TEXT NOT NULL,
  operator_id TEXT,
  solved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  time_taken_seconds INTEGER,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (operator_id) REFERENCES characters(id)
);

-- Generated Challenges (from PCG)
CREATE TABLE IF NOT EXISTS pcg_challenges (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  difficulty TEXT NOT NULL,
  description TEXT,
  flag TEXT NOT NULL,
  points INTEGER NOT NULL,
  hints TEXT, -- JSON array
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Mission Progress
CREATE TABLE IF NOT EXISTS user_missions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  mission_id TEXT NOT NULL,
  operator_id TEXT,
  chapter INTEGER,
  status TEXT DEFAULT 'in_progress', -- in_progress, completed, failed
  objectives_json TEXT, -- JSON array of objective completion
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Trust-based Choices (mission branching)
CREATE TABLE IF NOT EXISTS mission_choices (
  id TEXT PRIMARY KEY,
  mission_id TEXT NOT NULL,
  choice_index INTEGER,
  option_text TEXT,
  trust_delta INTEGER,
  outcome TEXT, -- success, fail, neutral
  outcome_message TEXT
);

-- Character Data
CREATE TABLE IF NOT EXISTS characters (
  id TEXT PRIMARY KEY,
  codename TEXT NOT NULL UNIQUE,
  real_name TEXT,
  is_playable BOOLEAN DEFAULT 1,
  role TEXT,
  description TEXT,
  biography TEXT,
  skills TEXT, -- JSON array: ["Hacking", "Social Engineering", ...]
  stat_attack INTEGER DEFAULT 50,
  stat_defense INTEGER DEFAULT 50,
  stat_speed INTEGER DEFAULT 50,
  stat_tech INTEGER DEFAULT 50,
  faction TEXT, -- hero, villain, neutral
  tier TEXT -- operative, elite, boss
);

-- Leaderboard Snapshots (for historical tracking)
CREATE TABLE IF NOT EXISTS leaderboard_snapshots (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL,
  rank INTEGER,
  score INTEGER,
  challenges_solved INTEGER,
  snapshot_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_solves_user ON user_solves(user_id);
CREATE INDEX IF NOT EXISTS idx_user_trust_user ON user_trust(user_id);
CREATE INDEX IF NOT EXISTS idx_user_missions_user ON user_missions(user_id);
CREATE INDEX IF NOT EXISTS idx_challenges_category ON pcg_challenges(category);
```

---

## 🔄 Frontend-Backend Integration

### **Using the API from React Components**

#### **Example: Fetch and Display Challenges**
```tsx
// src/pages/CTF.tsx
import { useState, useEffect } from 'react';

export const CTF = () => {
  const [challenges, setChallenges] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    fetch('/api/generate/challenge?category=web&difficulty=hard&seed=' + Date.now())
      .then(r => r.json())
      .then(challenge => {
        setChallenges(prev => [...prev, challenge]);
        setLoading(false);
      });
  }, []);
  
  return (
    <div>
      {challenges.map(c => (
        <div key={c.id}>
          <h3>{c.name}</h3>
          <p>{c.points} points</p>
        </div>
      ))}
    </div>
  );
};
```

#### **Example: Submit Flag**
```tsx
// In any challenge component
const handleFlagSubmit = async (flag: string) => {
  const res = await fetch('/api/submit-flag', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      challengeId: challenge.id,
      flag,
      operatorId: selectedOperator.id
    })
  });
  
  const data = await res.json();
  
  if (data.success) {
    dispatch({ 
      type: 'ADD_SCORE', 
      payload: data.points 
    });
    audio.playSound('success');
  } else {
    audio.playSound('failure');
  }
};
```

#### **Example: Update Trust via API**
```tsx
// In decision component
const handleChoice = async (choice) => {
  const res = await fetch('/api/trust/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      operatorId: character.id,
      delta: choice.trustDelta
    })
  });
  
  const { trust } = await res.json();
  
  dispatch({
    type: 'UPDATE_TRUST',
    payload: { characterId: character.id, value: trust }
  });
};
```

---

## 🎵 Advanced Features

### **Audio Enhancements**

#### **Option 1: Use Procedural (Current)**
- ✅ Works offline
- ✅ No file downloads
- ✅ Infinitely configurable
- ✅ 10+ distinct sounds

#### **Option 2: Add Real Audio Files** (Optional)
```typescript
// Create public/audio/sfx/ directory
public/
├── audio/
│   ├── ui_click.mp3
│   ├── ui_hover.mp3
│   ├── success.mp3
│   ├── failure.mp3
│   └── ambient_loop.mp3

// Update useAudio.ts to fall back to files if available
const playSound = async (type: string) => {
  try {
    const audio = new Audio(`/audio/sfx/${type}.mp3`);
    audio.volume = masterVolume;
    audio.play();
  } catch {
    // Fall back to procedural
    generateProceduralSound(type);
  }
};
```

### **WebSocket Real-time Features**

#### **Multiplayer Leaderboard (Socket.io)**
```typescript
// server/server.ts
import { Server } from 'socket.io';

const io = new Server(server);

io.on('connection', (socket) => {
  // Broadcast new score
  socket.on('score-update', (data) => {
    io.emit('leaderboard-change', data);
  });
  
  // Chat in mission
  socket.on('mission-chat', (message) => {
    socket.broadcast.emit('mission-message', message);
  });
  
  // Trust event broadcast
  socket.on('trust-change', (data) => {
    io.emit('global-trust-update', data);
  });
});
```

#### **React Integration**
```tsx
// src/hooks/useLiveLeaderboard.ts
import { useEffect, useState } from 'react';
import { useWebSocket } from './useWebSocket';

export const useLiveLeaderboard = () => {
  const [leaderboard, setLeaderboard] = useState([]);
  const { on, off } = useWebSocket();
  
  useEffect(() => {
    on('leaderboard-change', (data) => {
      setLeaderboard(prev => 
        [...prev, data].sort((a, b) => b.score - a.score)
      );
    });
    
    return () => off('leaderboard-change');
  }, [on, off]);
  
  return leaderboard;
};
```

---

## 🧪 Testing Strategy

### **Unit Tests**
```typescript
// src/lib/__tests__/procedural.test.ts
import { describe, it, expect } from 'vitest';
import { generatePCGChallenge, createGameSession } from '../procedural';

describe('PCG System', () => {
  it('generates reproducible challenges with same seed', () => {
    const seed = 12345;
    const c1 = generatePCGChallenge(seed);
    const c2 = generatePCGChallenge(seed);
    
    expect(c1.flag).toBe(c2.flag);
    expect(c1.name).toBe(c2.name);
  });
  
  it('generates different challenges with different seeds', () => {
    const c1 = generatePCGChallenge(1);
    const c2 = generatePCGChallenge(2);
    
    expect(c1.id).not.toBe(c2.id);
  });
});
```

### **Integration Tests**
```typescript
// server/__tests__/api.test.ts
import request from 'supertest';
import app from '../server';

describe('Challenge API', () => {
  it('POST /api/generate/challenge returns valid challenge', async () => {
    const res = await request(app)
      .post('/api/generate/challenge')
      .query({ seed: 123 });
    
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('id');
    expect(res.body).toHaveProperty('flag');
    expect(res.body).toHaveProperty('points');
  });
  
  it('POST /api/submit-flag validates correctly', async () => {
    const res = await request(app)
      .post('/api/submit-flag')
      .send({
        challengeId: 'test_123',
        flag: 'CTF{correct_flag}',
        operatorId: 'kai'
      });
    
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
  });
});
```

---

## 📈 Performance Optimization

### **3D Rendering**
```typescript
// src/components/three/HeroScene.tsx - Auto detect performance
const isHighEnd = () => {
  const canvas = document.createElement('canvas');
  const gl = canvas.getContext('webgl');
  const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
  
  if (debugInfo) {
    const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
    // Use this to define quality settings
  }
};

// Adjust rendering quality
const config = {
  antialias: isHighEnd(),
  pixelRatio: isHighEnd() ? 2 : 1,
  shadowMap: isHighEnd(),
  particleCount: isHighEnd() ? 500 : 100
};
```

### **Audio Pooling**
```typescript
// src/hooks/useAudio.ts - Reuse oscillators
class AudioPool {
  private pool: OscillatorNode[] = [];
  
  acquire(): OscillatorNode {
    return this.pool.pop() || this.audioContext.createOscillator();
  }
  
  release(osc: OscillatorNode) {
    this.pool.push(osc);
  }
}
```

### **PCG Caching**
```typescript
// Memoize generated content
const generatedCache = new Map();

export const generatePCGChallenge = (seed: number) => {
  const key = `challenge_${seed}`;
  
  if (generatedCache.has(key)) {
    return generatedCache.get(key);
  }
  
  const challenge = _generateChallenge(seed);
  generatedCache.set(key, challenge);
  
  return challenge;
};
```

---

## 🚀 Deployment Checklist

### **Before Production**
- [ ] All API endpoints tested
- [ ] Database migrations run
- [ ] SSL/TLS configured
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Logging enabled
- [ ] Error tracking (Sentry) configured
- [ ] Database backups configured
- [ ] Load testing completed
- [ ] Security audit completed

### **Docker Compose**
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      NODE_ENV: production
      DATABASE_URL: sqlite://./db.sqlite3
    volumes:
      - ./data:/app/data

  postgres:
    image: postgres:15
    environment:
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## 📚 Learning Resources

- **Three.js**: https://threejs.org/docs/
- **React Three Fiber**: https://docs.pmnd.rs/react-three-fiber/
- **Web Audio API**: https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API
- **Socket.io**: https://socket.io/docs/
- **TypeScript**: https://www.typescriptlang.org/docs/

---

## 💬 Common Questions

**Q: The procedural audio is quiet. How do I fix it?**
A: Increase masterVolume in AudioContext (default 0.3). Adjust in useAudioContext initialization.

**Q: Can I use real GLTF models instead of the cone?**
A: Yes! Download from Sketchfab, place in public/models/, load with THREE.GLTFLoader in CyberpunkCity.

**Q: How do I add more sound types?**
A: Edit `generateProceduralSound()` in useAudio.ts, add new case with frequency envelope.

**Q: How do I make challenges harder?**
A: Edit `difficultyMultiplier` in procedural.ts based on `difficulty` parameter.

**Q: Can I track completion statistics?**
A: Yes! Query `user_solves` table: `GROUP BY operator_id, category` for analytics.

---

**Version 1.0.0** | Last Updated: March 24, 2026 | Ready for Backend Integration 🚀
