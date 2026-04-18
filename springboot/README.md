# Shadownet Nexus - Spring Boot Backend

This is the Java 17 + MySQL backend migration of the Shadownet Nexus CTF platform. The backend provides REST API endpoints for the React frontend.

## Why Spring Boot & Frontend Separation?

- **Backend (Spring Boot on port 3001)**: Provides REST API endpoints for authentication, challenges, leaderboards, and real-time features
- **Frontend (React)**: Runs in the browser and makes HTTP/WebSocket calls to the backend API
- They communicate over HTTP/JSON, allowing independent development and deployment

## Prerequisites

- Java 17+
- Maven 3.9+ (included in workspace)
- MySQL 8 (optional - H2 embedded database works for testing)

## Quick Start (With H2 Embedded Database - No MySQL Needed)

### 1. Build the application:
```bash
cd springboot
..\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

### 2. Run the application:
```bash
java -jar target\shadownet-nexus-1.0.0.jar
```

The server will start on **http://localhost:3001** with H2 embedded database.

### 3. Test health endpoint:
```powershell
Invoke-WebRequest -Uri http://localhost:3001/health -UseBasicParsing | Select-Object -ExpandProperty Content
```

Expected output: `{"timestamp":1775195143540,"status":"ok","db":"mysql"}`

## Setup With MySQL

### 1. Install and start MySQL:
```bash
# Windows: Make sure MySQL server is running
# Or use: mysql.exe start
```

### 2. Create database:
```sql
CREATE DATABASE shadownet_nexus;
```

### 3. Update connection settings:
Edit `src/main/resources/application-mysql.properties`:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3305/shadownet
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Run with MySQL profile:
```bash
java -jar target\shadownet-nexus-1.0.0.jar --spring.profiles.active=mysql
```

## API Endpoints

### Authentication
- **POST /api/register** - Register new user
  ```json
  { "email": "user@example.com", "username": "username", "password": "Password123!" }
  ```
  Returns: `{ "token": "eyJhbG..." }`

- **POST /api/login** - Login user
  ```json
  { "email": "user@example.com", "password": "Password123!" }
  ```
  Returns: `{ "token": "eyJhbG..." }`

### Challenges (Requires Authorization header: `Authorization: Bearer <token>`)
- **GET /api/challenges** - Get all challenges
- **POST /api/submit-flag** - Submit flag
  ```json
  { "challengeId": "web-001", "flag": "flag{...}" }
  ```
- **GET /api/search/challenges?q=<query>** - Search challenges

### Other Endpoints
- **GET /api/user** - Get current user profile
- **GET /api/leaderboard** - Get leaderboard
- **GET /api/operators** - Get operators list
- **POST /api/operators/select** - Select operator
  ```json
  { "operatorId": "op_hacker" }
  ```
- **GET /api/missions** - Get all missions
- **GET /api/missions/{id}** - Get mission details
- **GET /api/search/missions?q=<query>** - Search missions
- **GET /health** - Health check
- **GET /metrics** - Server metrics

## Database

### H2 Console (Testing)
When running with H2, access the database console at: **http://localhost:3001/h2-console**
- JDBC URL: `jdbc:h2:mem:shadownet_nexus`
- User: `sa`
- Password: (leave blank)

### Seeded Data
The application automatically creates sample data on startup:
- **Operators**: Cipher, Specter, Rook
- **Challenges**: Corporate Backdoor (web), Broken RSA (crypto)
- **Missions**: Data Heist, Cyber Defense

## WebSocket Support

The backend supports WebSocket connections at `/ws` for real-time features like team sessions and live updates.

## Testing with Postman/cURL

### Register a user:
```powershell
$body = @{ email='test@example.com'; username='testuser'; password='Test123!' } | ConvertTo-Json
Invoke-WebRequest -Uri http://localhost:3001/api/register -Method Post -ContentType "application/json" -Body $body -UseBasicParsing
```

### Get challenges with JWT token:
```powershell
$token = "your_jwt_token_here"
$headers = @{ "Authorization"="Bearer $token" }
Invoke-WebRequest -Uri http://localhost:3001/api/challenges -UseBasicParsing -Headers $headers
```

## Frontend Integration

The React frontend should make requests to this backend:

```javascript
// In React frontend (localhost:5173 or port your frontend runs on)
const API_BASE = 'http://localhost:3001';

// Register
const registerResponse = await fetch(`${API_BASE}/api/register`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, username, password })
});
const { token } = await registerResponse.json();
localStorage.setItem('token', token);

// Get challenges (with token)
const challengesResponse = await fetch(`${API_BASE}/api/challenges`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

## Development

Run tests:
```bash
mvn test
```

Run with debug logging:
```bash
mvn spring-boot:run -Ddebug
```

View logs:
The application logs are printed to console and include INFO, WARN, and ERROR level messages.

## Troubleshooting

### Port 3001 already in use:
```bash
# Find process using port 3001
netstat -ano | findstr :3001

# Kill the process
taskkill /PID <PID> /F
```

### JWT Secret Key Error:
The JWT secret must be at least 256 bits. The configuration has been set with a proper long key in `application.properties`.

### H2 Database Empty:
The application recreates the database on startup with H2 (create-drop mode). Seed data is automatically inserted on startup.

### MySQL Connection Issues:
- Verify MySQL is running
- Check credentials in `application-mysql.properties`
- Ensure database `shadownet_nexus` exists

## Architecture

```
┌─────────────────────┐
│   React Frontend    │ (Browser)
│  (localhost:5173)   │
└──────────┬──────────┘
           │ HTTP/WebSocket
           ▼
┌─────────────────────┐
│  Spring Boot API    │ (Java)
│ (localhost:3001)    │
└──────────┬──────────┘
           │ JDBC
           ▼
    ┌──────────────┐
    │ H2 or MySQL  │ (Database)
    └──────────────┘
```

## Deployment

For production:
1. Use MySQL instead of H2
2. Set proper JWT secret key
3. Enable HTTPS/SSL
4. Deploy on Oracle Cloud, AWS, or similar

See `deploy/k8s/` for Kubernetes configuration examples.
