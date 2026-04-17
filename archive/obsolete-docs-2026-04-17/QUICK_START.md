# Shadownet Nexus - Complete Setup Guide

## 🎯 Current Status

```
✅ Backend (Spring Boot):     RUNNING on http://localhost:3001
✅ Database (H2):              CONFIGURED (no setup needed)
✅ API Endpoints:             VERIFIED WORKING
✅ Authentication:            JWT tokens functional
✅ Test Data:                 Auto-seeded on startup
❓ Frontend (React):          Optional (requires Node.js)
```

---

## 🤔 Answer: "Why run Node.js?"

**Short Answer:**
- Spring Boot (Java) = Backend (port 3001)
- React (Node.js) = Frontend UI in Browser (port 5173)
- They talk via HTTP - completely separate systems
- **You don't need Node.js for the backend to work**
- **You only need Node.js if you want a UI in the browser**

**Visual:**
```
Your Browser              Spring Boot API         Database
http://5173               http://3001             H2/MySQL
  (React UI)    ←→    (Java Business Logic)   (Data Storage)
                   (HTTP/JSON Communication)
```

The backend works perfectly without the frontend!

---

## 🚀 Three Ways to Use Shadownet Nexus

### Option 1: Backend Only (✅ READY NOW - No Node.js needed)
Best for: API testing, Postman, integrations, custom clients

```powershell
# Test the backend
cd springboot
powershell -ExecutionPolicy Bypass -File TEST_API_SIMPLE.ps1

# Expected output:
# [1] Testing Health Endpoint... OK
# [2] Testing User Registration... OK
# [3] Testing Get Challenges... OK
# ... etc
```

✅ No Node.js installation needed
✅ Backend serves API on `http://localhost:3001`
✅ Can test with Postman, curl, PowerShell

---

### Option 2: Full Stack (Requires Node.js)
Best for: Complete CTF game experience

**Step 1: Install Node.js (One-time setup)**
- Go to https://nodejs.org/
- Download LTS version
- Run installer (default settings OK)
- Restart your terminal/computer

**Step 2: Start both services**
```powershell
# Terminal 1: Backend is already running
# It started automatically when you booted the project

# Terminal 2: Navigate to project root and start frontend
cd c:\Users\zain\Desktop\ctf\shadownet-nexus
npm install        # First time only - installs dependencies
npm run dev        # Starts development server
```

**Step 3: Open in browser**
```
http://localhost:5173
```

✅ You'll see the CTF UI
✅ Can register, play challenges
✅ Full game experience

---

### Option 3: Backend + Custom Frontend
Best for: your own UI (Vue, Angular, mobile apps, etc)

```
Use the API at http://localhost:3001
Build your own frontend however you want
```

---

## ✅ Verify Backend is Working

```powershell
# Quick health check
Invoke-WebRequest http://localhost:3001/health

# Should return:
# {"timestamp":1775195143540,"status":"ok","db":"mysql"}
```

---

## 📝 Test Script

Ready-to-run test that checks all endpoints:

```powershell
cd springboot
powershell -ExecutionPolicy Bypass -File TEST_API_SIMPLE.ps1
```

This will:
1. ✅ Check server is running
2. ✅ Test user registration
3. ✅ Test getting challenges
4. ✅ Test getting operators
5. ✅ Test leaderboards
6. ✅ Print summary

---

## 🔌 API Endpoints (Backend on :3001)

### Public (No Auth Required)
```
GET  /health
POST /api/register              # Create account
POST /api/login                 # Login
GET  /api/operators             # Operator list
GET  /api/leaderboard           # Leaderboard
GET  /api/missions              # Mission list
```

### Authenticated (Need JWT Token)
```
GET  /api/user                  # Your profile
GET  /api/challenges            # Your challenges
POST /api/submit-flag           # Submit flag
POST /api/operators/select      # Choose operator
```

---

## 🔑 Getting a JWT Token

```powershell
# Register new user
$body = @{
    email = "player@test.com"
    username = "player123"
    password = "Pass123!"
} | ConvertTo-Json

$res = Invoke-WebRequest -Uri http://localhost:3001/api/register `
    -Method Post -ContentType "application/json" -Body $body -UseBasicParsing

$token = ($res.Content | ConvertFrom-Json).token
echo $token  # Your JWT token - valid for 24 hours
```

Use token in future requests:
```powershell
$headers = @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest http://localhost:3001/api/challenges -Headers $headers
```

---

## 📚 Database Info

### Current Setup (H2 - In-Memory)
- No install needed
- Data resets on server restart
- Auto-populated with test data
- Good for development/testing

### Access H2 Console
```
http://localhost:3001/h2-console
Username: sa
Password: (leave blank)
```

### Switch to MySQL (Later)
When ready for persistent data:

1. Install MySQL
2. Create database: `CREATE DATABASE shadownet_nexus;`
3. Edit: `springboot/src/main/resources/application-mysql.properties`
4. Run: `java -jar target/shadownet-nexus-1.0.0.jar --spring.profiles.active=mysql`

---

## 🎮 What You Can Do Now

### Backend Only
- ✅ Test all API endpoints
- ✅ Register users
- ✅ Manage challenges
- ✅ Track scores
- ✅ Build custom clients

### With Node.js Frontend Added
- ✅ Everything above, PLUS:
- ✅ Browse challenges in browser UI
- ✅ Solve CTF puzzles
- ✅ See leaderboards
- ✅ Select operators
- ✅ Full game experience

---

## 🚨 Troubleshooting

### Backend not responding
```powershell
# Check if running
netstat -ano | findstr :3001

# If empty, start it
cd springboot
java -jar target\shadownet-nexus-1.0.0.jar
```

### Port 3001 in use
```powershell
# Find what's using it
netstat -ano | findstr :3001

# Kill it
taskkill /PID <PID> /F

# Then restart backend
java -jar target\shadownet-nexus-1.0.0.jar
```

### Token expired
- Tokens last 24 hours
- Register again to get new token
- Or use login endpoint

### Database empty
- H2 auto-creates and seeds on startup
- Wait 2-3 seconds after server starts
- Check `/h2-console` to verify data

---

## 📂 Project Structure

```
shadownet-nexus/
├── springboot/                      # Java Backend
│   ├── src/main/java/com/example/
│   │   ├── entity/                  # Database entities
│   │   ├── repository/              # Data access
│   │   ├── controller/              # API endpoints
│   │   ├── service/                 # Business logic
│   │   └── ...
│   ├── target/shadownet-nexus-1.0.0.jar  # Compiled JAR
│   ├── pom.xml                      # Maven config
│   └── TEST_API_SIMPLE.ps1          # Test script
│
├── src/                             # React Frontend
│   ├── components/                  # UI components
│   ├── pages/                       # Pages
│   ├── App.tsx
│   └── main.tsx
│
├── package.json                     # Frontend config
├── vite.config.ts                   # Build config
└── QUICK_START.md                   # This file
```

---

## ✨ Next Steps

### Right Now (No Node.js)
```bash
# Verify backend works
cd springboot
powershell -ExecutionPolicy Bypass -File TEST_API_SIMPLE.ps1
```

### Later (Add Frontend)
```bash
# Install Node.js from https://nodejs.org/
# Then in new terminal:
npm install
npm run dev
# Open http://localhost:5173
```

### For Production
See `deploy/k8s/shadownet-deployment.yaml`

---

## 📚 Documentation

- **Architecture Deep Dive**: See [ARCHITECTURE.md](ARCHITECTURE.md)
- **Backend Docs**: See [springboot/README.md](springboot/README.md)
- **Developer Guide**: See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)

---

**Status: READY FOR USE**

The backend is fully functional. Use Option 1 (backend only) now, or add Node.js later for the full UI experience!
