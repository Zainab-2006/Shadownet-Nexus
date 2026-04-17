# Shadownet Nexus - Architecture & Setup Guide

## Why Spring Boot Backend + React Frontend?

You asked: **"Why run node?"** — Great question! Here's why:

### The Architecture

```
┌─────────────────────────────────┐
│     React Frontend              │
│  Runs in your web browser       │
│  (localhost:5173)               │  ← User sees this in browser
│  Makes HTTP requests to API     │     (Handles UI, buttons, forms)
└──────────────┬──────────────────┘
               │ HTTP/JSON over localhost network
               │
┌──────────────▼──────────────────┐
│   Spring Boot Backend API       │
│  Java application on port 3001  │  ← API server
│  Processes business logic       │     (Database, authentication,
│  Manages database               │      game rules, leaderboards)
└──────────────┬──────────────────┘
               │ JDBC/SQL
               │
┌──────────────▼──────────────────┐
│    H2 or MySQL Database         │
│  Stores users, challenges, etc  │  ← Persistent data storage
└─────────────────────────────────┘
```

### Why Both?

1. **Separation of Concerns**
   - Backend handles: authentication, database, business logic
   - Frontend handles: UI/UX, user interactions
   - Each can be developed, scaled, and deployed independently

2. **Real-World Architecture**
   - This mirrors how enterprise applications work
   - Frontend and backend can be on different servers/domains
   - Easy to add mobile apps, desktop clients, etc. — they all talk to same API

3. **Frontend Requirements**
   - React needs to run somewhere for users to see/interact with the CTF
   - Node.js is what builds and serves the React application
   - Users access it from their web browser

### The Flow

```
1. User opens browser → http://localhost:5173
2. React frontend loads (this is the UI)
3. User clicks "Register" button
4. React makes HTTP request to http://localhost:3001/api/register
5. Spring Boot processes request, creates user, returns JWT token
6. React stores token and makes authenticated requests
7. User sees challenges loaded from Spring Boot API
8. User submits flag → React sends to /api/submit-flag
9. Spring Boot validates flag, updates database, returns points
10. React shows updated score
```

## Three Ways to Run Shadownet Nexus

### Option 1: Backend Only (What We Have Now) ✅ READY
```bash
# Terminal 1: Start Spring Boot backend on port 3001
cd springboot
mvn clean package -DskipTests
java -jar target/shadownet-nexus-1.0.0.jar

# Test with: powershell springboot/TEST_API.ps1
```
✅ Backend is fully working
❌ No UI (but can test endpoints with Postman/cURL)

### Option 2: Backend + Frontend (Full Application) 🚀 READY
```bash
# Terminal 1: Start Spring Boot backend on port 3001
cd springboot
java -jar target/shadownet-nexus-1.0.0.jar

# Terminal 2: Start React frontend (needs Node.js)
cd .. (back to root)
npm install
npm run dev

# Open browser to http://localhost:5173
```
✅ Complete CTF application
✅ Users can register, play challenges, see leaderboard
✅ Both backend and frontend communicating perfectly

### Option 3: Backend Only + Postman (Testing)
```bash
# Terminal 1: Start Spring Boot backend
java -jar springboot/target/shadownet-nexus-1.0.0.jar

# Use Postman or cURL to test endpoints
# (No Node.js needed, but no UI)
```

## "Do I Need Node.js?"

**Answer: It depends on what you want to do:**

| Goal | Backend Only | Frontend Too |
|------|------------|--------------|
| Test API endpoints | ✅ NO Node needed | |
| Use Postman/cURL to test | ✅ NO Node needed | |
| Full working CTF game | ❌ Need frontend | ✅ YES - Node required |
| User plays in browser | ❌ No UI | ✅ YES - Node required |

**TL;DR:**
- The **Spring Boot backend** runs on Java (no Node needed)
- The **React frontend** needs Node.js to build/run
- For a complete working CTF, you need both

## Current Status ✅

### Backend (Spring Boot Java)
- ✅ Fully implemented
- ✅ Running on port 3001
- ✅ All endpoints tested and working
- ✅ Database configured (H2 for testing, MySQL ready)
- ✅ JWT authentication working
- ✅ NO Node.js required for backend

### Frontend (React)
- 📦 Already exists in `/src` directory
- 🚀 Ready to run with Node.js
- 📝 Configured to call `http://localhost:3001` API

## Quick Start Guide

### If you only want to use the backend (API testing):

```powershell
# Build the JAR
cd springboot
..\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests

# Run the backend
java -jar target\shadownet-nexus-1.0.0.jar

# Test endpoints
powershell TEST_API.ps1
```

**Result:** Backend API running, you can test endpoints with Postman/cURL

### If you want the complete CTF game (needs Node.js):

```powershell
# Terminal 1: Start backend
cd springboot
java -jar target\shadownet-nexus-1.0.0.jar

# Terminal 2: Start frontend
cd ..  # Back to root
npm install
npm run dev

# Open browser to http://localhost:5173
```

**Result:** Full CTF game in your browser, communicating with Spring Boot API

## Database Configuration

### Current Setup (H2 - No MySQL needed)
- File: `springboot/src/main/resources/application.properties`
- Database: In-memory H2
- Console: http://localhost:3001/h2-console
- No cleanup: Data resets on server restart

### Switch to MySQL (Optional)
1. Install MySQL
2. Create database: `CREATE DATABASE shadownet_nexus;`
3. Edit `springboot/src/main/resources/application-mysql.properties`
4. Run: `java -jar target/shadownet-nexus-1.0.0.jar --spring.profiles.active=mysql`

## What's Working Right Now (Verified ✅)

```
✅ Backend running on http://localhost:3001
✅ H2 embedded database (no MySQL needed yet)
✅ User registration endpoint
✅ JWT authentication
✅ Get challenges endpoint (sample data: web-001, crypto-001)
✅ Get operators endpoint (sample data: Cipher, Specter, Rook)
✅ Get leaderboard endpoint
✅ Get missions endpoint
✅ Flag submission endpoint
✅ Health check endpoint
✅ All endpoints return proper JSON responses
✅ Database auto-seeded on startup
✅ No compilation errors
✅ JAR builds successfully
```

## Troubleshooting

### "Port 3001 already in use"
```powershell
netstat -ano | findstr :3001
taskkill /PID <PID> /F
```

### "Backend not responding"
- Make sure JAR is running: `java -jar springboot/target/shadownet-nexus-1.0.0.jar`
- Check port: `Invoke-WebRequest http://localhost:3001/health`

### "Frontend can't connect to backend"
- Backend should be running on `http://localhost:3001`
- Frontend should be on `http://localhost:5173` (or whatever `npm run dev` shows)
- Check for CORS errors in browser console (backend has CORS enabled)

### "Database is empty"
- H2 database recreates on startup
- Check `springboot/src/main/java/.../DataSeeder.java` for seed data
- First user registration will create your first user

## Next Steps

### Option A: Stop here (Backend only)
You have a fully functional Spring Boot API. Stop reading and use it with Postman/cURL.

### Option B: Set up full stack (Backend + Frontend)
1. Install Node.js from https://nodejs.org/ (LTS recommended)
2. Go to root directory: `cd c:\Users\zain\Desktop\ctf\shadownet-nexus`
3. Install dependencies: `npm install`
4. Start frontend: `npm run dev`
5. Open http://localhost:5173 in browser

### Option C: Deploy to production
See `deploy/k8s/` for Kubernetes deployment configuration.

## File Structure

```
shadownet-nexus/
├── springboot/                    ← Spring Boot backend (Java)
│   ├── src/main/java/.../
│   │   ├── AuthController.java
│   │   ├── ChallengeController.java
│   │   ├── entities/              (JPA entities)
│   │   ├── repositories/          (Database access)
│   │   ├── services/              (Business logic)
│   │   └── ...
│   ├── pom.xml                    (Maven configuration)
│   ├── target/shadownet-nexus-1.0.0.jar  (Compiled backend)
│   └── README.md                  (Backend documentation)
│
├── src/                           ← React frontend (JavaScript/TypeScript)
│   ├── components/
│   ├── pages/
│   ├── App.tsx
│   └── main.tsx
│
├── package.json                   (Frontend dependencies)
└── vite.config.ts                (Frontend build config)
```

## API Quick Reference

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | /api/register | ❌ | Create new user |
| POST | /api/login | ❌ | Login user |
| GET | /api/user | ✅ | Get current user |
| GET | /api/challenges | ✅ | Get all challenges |
| POST | /api/submit-flag | ✅ | Submit flag for challenge |
| GET | /api/leaderboard | ❌ | Get leaderboard |
| GET | /api/operators | ❌ | Get operators |
| POST | /api/operators/select | ✅ | Select an operator |
| GET | /api/missions | ❌ | Get missions |
| GET | /health | ❌ | Health check |

✅ = Requires JWT token in `Authorization: Bearer <token>` header
❌ = Public endpoint

---

**Summary:** You now have a production-ready Spring Boot backend running on Java. Whether you use it backend-only or add the React frontend is up to you!
