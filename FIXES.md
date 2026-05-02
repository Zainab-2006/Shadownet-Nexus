# Shadownet-Nexus: Consolidated Fixes Roadmap

**Date Generated:** 2026-05-02  
**Total Issues:** 47  
**Status:** Ready for Implementation  

---

## Critical Fixes (BLOCKING - Fix First)

### ✋ [C1] Missing Required Environment Variable - Email Encryption Key

**Priority:** BLOCKING - Application will crash at runtime  
**Severity:** CRITICAL  
**File:** [springboot/src/main/java/com/shadownet/nexus/service/AuthService.java](springboot/src/main/java/com/shadownet/nexus/service/AuthService.java#L50)  
**Line:** 50  

**Current Code:**
```java
private static final String AES_ENCRYPTION_KEY = System.getenv("EMAIL_ENCRYPTION_KEY");
```

**Issue:**  
- `AES_ENCRYPTION_KEY` is initialized from `System.getenv("EMAIL_ENCRYPTION_KEY")` as a static final field
- Will be `null` if environment variable is not set before JVM starts
- Will cause `NullPointerException` when any email encryption is attempted
- No validation to fail fast

**Fix Steps:**
1. Add `@PostConstruct` validation method to fail fast if env var missing
2. Set `EMAIL_ENCRYPTION_KEY` environment variable in all deployment environments (.env, docker-compose, production)
3. Add meaningful error message in exception
4. Add unit test to verify exception on null key

**Example Fix:**
```java
@PostConstruct
public void validateEncryptionKey() {
    if (AES_ENCRYPTION_KEY == null || AES_ENCRYPTION_KEY.isEmpty()) {
        throw new IllegalStateException(
            "EMAIL_ENCRYPTION_KEY environment variable must be set before application startup"
        );
    }
}
```

**Impact:** Email encryption will completely fail; user registration and password reset features broken  
**Estimated Effort:** 15 minutes  
**Checklist:**
- [ ] Add validation in `@PostConstruct` method
- [ ] Add `.env.example` entry for `EMAIL_ENCRYPTION_KEY`
- [ ] Update docker-compose files with dummy key
- [ ] Update production deployment documentation
- [ ] Write unit test for validation
- [ ] Test application startup with missing env var (should fail fast)

---

## High Priority Fixes (Security/Stability)

### 🔐 [H1] CSRF Protection Disabled - Security Vulnerability

**Priority:** BLOCKING - Must fix before any production deployment  
**Severity:** HIGH (Security)  
**File:** [springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java](springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java#L61)  
**Line:** 61  

**Current Code:**
```java
.csrf(csrf -> csrf.disable())
```

**Issue:**  
- CSRF protection explicitly disabled for all endpoints
- Application vulnerable to Cross-Site Request Forgery attacks
- Sensitive operations (login, state changes, score submissions) completely unprotected
- Attackers can forge requests from other websites to manipulate game state

**Fix Steps:**
1. Remove `.csrf(csrf -> csrf.disable())` line or change to `.csrf(csrf -> csrf.enable())`
2. Configure CSRF token cookie settings
3. For SPA frontend: configure token repository for cookie-based CSRF protection
4. Test CSRF token generation and validation in integration tests

**Example Fix:**
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
)
```

**Impact:** Application is vulnerable to unauthorized game state manipulation, score injection, and team compromise  
**Estimated Effort:** 30 minutes  
**Checklist:**
- [ ] Re-enable CSRF protection
- [ ] Configure CSRF token cookie (HttpOnly=false for frontend access)
- [ ] Add CSRF token to frontend POST/PUT/DELETE requests
- [ ] Update frontend API client to include X-CSRF-TOKEN header
- [ ] Test with curl/Postman to verify CSRF token validation
- [ ] Document CSRF token handling in API_CONTRACT.md

---

### 🚪 [H2] Overly Permissive Authorization - All Endpoints Exposed

**Priority:** BLOCKING - Causes silent security bypass  
**Severity:** HIGH (Security)  
**File:** [springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java](springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java#L87)  
**Line:** 87  

**Current Code:**
```java
.anyRequest().permitAll()
```

**Issue:**  
- Final catch-all authorization rule allows all unauthenticated requests to ANY endpoint
- Any endpoint not explicitly listed in authorization rules becomes publicly accessible
- New endpoints silently become insecure by default
- Critical endpoints like `/admin/*`, `/user/*/permissions` may be exposed if not explicitly listed

**Fix Steps:**
1. Change `.anyRequest().permitAll()` to `.anyRequest().authenticated()`
2. Audit all endpoint URLs to ensure they are explicitly listed in `authorizeHttpRequests()`
3. Add explicit rules for all public endpoints (login, register, health check)
4. Add explicit rules for admin endpoints requiring ADMIN role
5. Test with unauthenticated request to random endpoint (should return 401 or 403)

**Example Fix:**
```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("/auth/**", "/health", "/health/**").permitAll()
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

**Impact:** Any new endpoint or forgotten endpoint is automatically public; potential data breach  
**Estimated Effort:** 45 minutes  
**Checklist:**
- [ ] Change final catch-all to `.authenticated()`
- [ ] Audit all existing endpoints to verify authorization rules exist
- [ ] Document which endpoints are public vs. protected
- [ ] Add integration tests for authorization on all endpoints
- [ ] Test unauthenticated access to protected endpoints (should fail)
- [ ] Review and update API_CONTRACT.md with authorization requirements

---

### 🌐 [H3] Wildcard CORS Origin - Subdomain Bypass Vulnerability

**Priority:** BLOCKING - Allows any unauthorized origin  
**Severity:** HIGH (Security)  
**File:** [springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java](springboot/src/main/java/com/shadownet/nexus/config/SecurityConfig.java#L41)  
**Line:** 41  

**Current Code:**
```
https://*.vercel.app
```

**Issue:**  
- Wildcard pattern allows ANY Vercel subdomain to access the API
- Any user-created Vercel preview deployment can access the API
- Includes potentially malicious preview deployments or forks
- Does not restrict to official Shadownet-Nexus domain

**Fix Steps:**
1. Replace wildcard with specific production domain: `https://shadownet-nexus.vercel.app`
2. For staging/preview deployments, use explicit URL: `https://staging-shadownet-nexus.vercel.app`
3. Make CORS origins configurable via environment variables (dev vs. prod)
4. Remove all wildcard patterns

**Example Fix (in application.yml or SecurityConfig):**
```java
.cors(cors -> cors.allowedOrigins(
    System.getenv("CORS_ALLOWED_ORIGINS")
        .split(",")  // e.g., "https://shadownet-nexus.vercel.app,https://localhost:5173"
))
```

**Impact:** Unauthorized origins can access API; CORS protection completely bypassed; data exposure  
**Estimated Effort:** 20 minutes  
**Checklist:**
- [ ] Update CORS configuration to use specific domains only
- [ ] Make CORS origins configurable via environment variable
- [ ] Update .env.example with production CORS origins
- [ ] Update docker-compose with localhost CORS for development
- [ ] Test CORS preflight with unauthorized origin (should fail)
- [ ] Test CORS with authorized origins (should succeed)

---

### 📋 [H4] React Hook Dependencies Missing - useEffect in CTF.tsx

**Priority:** HIGH - Causes state inconsistency bugs  
**Severity:** HIGH (Stability)  
**File:** [src/pages/CTF.tsx](src/pages/CTF.tsx#L193)  
**Line:** 193  

**Current Code:**
```typescript
useEffect(() => {
  if (session && !sessionLoading) {
    setSessionId(session.id);
    setCurrentStage(session.currentStage);
    setHintsUsed(session.hintsUsed);
    if (!timerId) startTimer();
  }
}, [session, sessionLoading]); // Missing: startTimer, timerId
```

**Issue:**  
- `startTimer` function is used but not in dependency array
- `timerId` is checked but not in dependency array
- Causes stale closures; timer may not properly start or state updates may be missed
- Timer could be called multiple times or not at all
- Can cause race conditions in CTF session management

**Fix Steps:**
1. Add missing dependencies: `startTimer` and `timerId`
2. Or extract timer management into separate `useEffect` hook
3. Test CTF mode to verify timer starts correctly
4. Run ESLint to catch similar issues

**Example Fix (Option 1 - Add Dependencies):**
```typescript
useEffect(() => {
  if (session && !sessionLoading) {
    setSessionId(session.id);
    setCurrentStage(session.currentStage);
    setHintsUsed(session.hintsUsed);
    if (!timerId) startTimer();
  }
}, [session, sessionLoading, startTimer, timerId]);
```

**Example Fix (Option 2 - Separate Effect for Timer):**
```typescript
useEffect(() => {
  if (session && !sessionLoading && !timerId) {
    startTimer();
  }
}, [session, sessionLoading]);
```

**Impact:** CTF timer may not start correctly; session state may become inconsistent; player scores lost  
**Estimated Effort:** 20 minutes  
**Checklist:**
- [ ] Fix dependency array in CTF.tsx useEffect
- [ ] Run ESLint to check for other missing dependencies
- [ ] Test CTF mode: verify timer starts when session loads
- [ ] Test CTF mode: verify timer doesn't restart when re-rendering
- [ ] Run Cypress E2E test for CTF flow

---

### 🗑️ [H5] Unchecked Type Casting in UserEvent Entity

**Priority:** HIGH - Runtime ClassCastException risk  
**Severity:** HIGH (Stability)  
**File:** [springboot/src/main/java/com/shadownet/nexus/entity/UserEvent.java](springboot/src/main/java/com/shadownet/nexus/entity/UserEvent.java#L113)  
**Line:** 113  

**Current Code:**
```java
return new ObjectMapper().readValue(metadataJson, Map.class);
```

**Issue:**  
- Raw type `Map.class` without generics causes "unchecked or unsafe operations" warning
- Compiler warning indicates potential ClassCastException at runtime
- Type safety lost; map values could be any type
- Hard to debug if wrong types are returned

**Fix Steps:**
1. Replace with `TypeReference<Map<String, Object>>() {}`
2. Or use more specific type if structure is known
3. Add null checks before accessing map values
4. Test with various metadata formats

**Example Fix:**
```java
return new ObjectMapper().readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
```

**Impact:** Runtime ClassCastException possible; metadata not properly parsed; user event tracking broken  
**Estimated Effort:** 10 minutes  
**Checklist:**
- [ ] Replace `Map.class` with `TypeReference<Map<String, Object>>()`
- [ ] Add null check for returned map
- [ ] Add unit test for metadata deserialization
- [ ] Verify no other raw type usage in codebase

---

### 📦 [H6] ESLint: Fast Refresh Export Pattern Violations

**Priority:** HIGH - Causes hot reload issues  
**Severity:** HIGH (Development Experience)  
**Type:** 13 ESLint warnings  
**Files Affected:**
- [src/components/ui/cyber-button.tsx](src/components/ui/cyber-button.tsx#L93) - Line 93
- [src/components/ui/cyber-card.tsx](src/components/ui/cyber-card.tsx#L124) - Line 124
- [src/components/ui/form.tsx](src/components/ui/form.tsx#L129) - Line 129
- [src/components/ui/navigation-menu.tsx](src/components/ui/navigation-menu.tsx#L111) - Line 111
- [src/components/ui/sidebar.tsx](src/components/ui/sidebar.tsx#L636) - Line 636
- [src/components/ui/sonner.tsx](src/components/ui/sonner.tsx#L27) - Line 27
- [src/components/ui/toggle.tsx](src/components/ui/toggle.tsx#L37) - Line 37
- [src/context/AudioProvider.tsx](src/context/AudioProvider.tsx#L46) - Line 46
- [src/context/AuthContext.tsx](src/context/AuthContext.tsx#L85) - Line 85
- [src/context/GameContext.tsx](src/context/GameContext.tsx#L206) - Line 206
- [src/context/GameContext.tsx](src/context/GameContext.tsx#L563) - Line 563
- [src/context/NarratorContext.tsx](src/context/NarratorContext.tsx#L90) - Line 90

**Issue:**  
- Files export both components AND constants/functions/utilities
- React Fast Refresh requires component-only exports in each file
- Violates `@next/next/no-export-all` and `@react-refresh/only-export-components` rules
- When file changes, all exports reload, not just components

**Fix Steps:**
1. For each affected file:
   - Create separate file for non-component exports (e.g., `cyber-button.utils.ts`, `auth.constants.ts`)
   - Move all constants/functions/utilities to new file
   - Keep only component exports in original file
   - Update all imports throughout codebase

**Example Pattern:**
```
Before:
  cyber-button.tsx → exports CyberButton component + BUTTON_SIZES constant

After:
  cyber-button.tsx → exports only CyberButton component
  cyber-button.constants.ts → exports BUTTON_SIZES constant
```

**Impact:** Hot module reloading fails; unexpected state loss during development; slower feedback loop  
**Estimated Effort:** 90 minutes (1-2 min per file × 13 files)  
**Checklist:**
- [ ] Audit each affected file
- [ ] Create `.constants.ts`, `.utils.ts`, or `.types.ts` files as needed
- [ ] Move exports to appropriate new files
- [ ] Update all import statements across codebase
- [ ] Run ESLint to verify warnings resolved
- [ ] Test hot reload in development

---

### 🚨 [H7] Deprecated Method Annotations Without Migration Guidance

**Priority:** HIGH - Creates confusion for API users  
**Severity:** HIGH (Maintainability)  
**Files Affected:**
- [springboot/src/main/java/com/shadownet/nexus/service/AccusationService.java](springboot/src/main/java/com/shadownet/nexus/service/AccusationService.java#L21) - Line 21: `submitAccusation()` method
- [springboot/src/main/java/com/shadownet/nexus/service/EvidenceService.java](springboot/src/main/java/com/shadownet/nexus/service/EvidenceService.java#L22) - Line 22

**Current Code:**
```java
@Deprecated
public void submitAccusation(...) {
    // No guidance on replacement
}
```

**Issue:**  
- Methods marked `@Deprecated` but no replacement guidance provided
- No timeline for removal (forRemoval flag missing)
- No clear migration path for client code
- Other developers don't know what to use instead

**Fix Steps:**
1. For each deprecated method:
   - Add complete `@Deprecated` annotation with `since`, `forRemoval`, and `message`
   - Provide clear migration instructions in message
   - Include replacement method name or alternative approach
   - Add same annotation to controller endpoints that use these methods

**Example Fix:**
```java
@Deprecated(
    since = "1.5.0",
    forRemoval = true,
    message = "Use submitAccusationV2() instead. " +
              "submitAccusation() will be removed in v2.0.0. " +
              "See migration guide at docs/v2-migration.md"
)
public void submitAccusation(...) {
    // Implementation
}
```

**Impact:** Client code stuck using deprecated APIs; unclear migration path; technical debt accumulation  
**Estimated Effort:** 15 minutes  
**Checklist:**
- [ ] Update @Deprecated annotations with since/forRemoval/message
- [ ] Ensure replacement methods exist or clearly document what to use instead
- [ ] Add migration guide in documentation
- [ ] Update JavaDoc on replacement method to reference deprecated version
- [ ] Search codebase for calls to deprecated methods and update them

---

### 📚 [H8] Deprecated javax.crypto Imports

**Priority:** HIGH - Future Java compatibility  
**Severity:** HIGH (Technical Debt)  
**Files Affected:**
- [springboot/src/main/java/com/shadownet/nexus/util/JwtUtil.java](springboot/src/main/java/com/shadownet/nexus/util/JwtUtil.java#L10) - Line 10
- [springboot/src/main/java/com/shadownet/nexus/service/AuthService.java](springboot/src/main/java/com/shadownet/nexus/service/AuthService.java#L18-L19) - Lines 18-19

**Issue:**  
- Using `javax.crypto` imports (old namespace from Java EE)
- Compiler shows "Some input files use or override a deprecated API" warning
- Code may break with future Java versions
- Maintainability concerns; appears as "old" code

**Fix Steps:**
1. Verify that `javax.crypto` is still the correct import (it is for Java Crypto API)
2. If using other `javax.*` namespaces, migrate to `jakarta.*` equivalents
3. For cryptography, verify using Java's built-in crypto API:
   - `javax.crypto.Cipher` ✓ (still valid)
   - `javax.crypto.SecretKey` ✓ (still valid)
4. Suppress compiler warning if `javax.crypto` is confirmed necessary

**Example Fix (if actually deprecated):**
```java
import javax.crypto.Cipher;  // Still valid in modern Java

// Or if using Spring Security:
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

**Example Fix (if warning can be suppressed):**
```java
@SuppressWarnings("deprecation")
import javax.crypto.Cipher;
```

**Impact:** Compilation warnings; potential incompatibility with future Java versions  
**Estimated Effort:** 20 minutes  
**Checklist:**
- [ ] Verify javax.crypto is necessary vs. Jakarta equivalents
- [ ] Check Java version requirements
- [ ] Add appropriate imports or @SuppressWarnings if necessary
- [ ] Run `mvn clean compile` to verify warnings resolved
- [ ] Document why javax.crypto is used if suppressing warning

---

## Medium Priority Fixes (Quality/Performance)

### 🛡️ [M1] Vulnerable Dependency: uuid Package CVE

**Priority:** MEDIUM - Update available but requires version bump  
**Severity:** MEDIUM (Security)  
**Package:** uuid  
**CVE:** GHSA-w5hq-g745-h8pq  
**Dependency Chain:** cypress → @cypress/request → uuid  
**Current Version:** < 14.0.0  

**Issue:**  
- uuid package missing buffer bounds check in v3/v5/v6 when buf is provided
- Potential buffer overflow vulnerability
- Affects: cypress (≥4.3.0) depends on vulnerable uuid version
- Requires cypress major version upgrade (currently ~13.6.x → ≥14.2.0)

**Fix Steps:**
1. Review cypress changelog for v14 breaking changes
2. Update cypress in package.json: `"cypress": "^14.2.0"`
3. Run `npm install` to resolve uuid to ≥14.0.0
4. Run `npm audit` to confirm vulnerability resolved
5. Run `npm test` and E2E tests to verify no regressions
6. Update CI/CD configuration if needed for new cypress version

**Risks:**
- Cypress 14.0.0 may have breaking changes
- May require test code updates
- May change test execution behavior

**Impact:** Buffer overflow in UUID generation; potential code execution in certain edge cases  
**Estimated Effort:** 30 minutes  
**Checklist:**
- [ ] Review cypress v14 breaking changes
- [ ] Update package.json
- [ ] Run npm install and verify npm audit shows no uuid vulnerabilities
- [ ] Run all npm scripts to verify compatibility
- [ ] Run Cypress E2E tests
- [ ] Commit and test in CI/CD

---

### 📉 [M2] Large Frontend Build Chunks - Performance Warning

**Priority:** MEDIUM - Large initial load time  
**Severity:** MEDIUM (Performance)  
**Build Output:** Multiple chunks > 500 kB after minification  
**Largest Chunk:**
- dist/assets/index-Cqc41UzC.js: **1,572.68 kB** (459.85 kB gzipped)

**Issue:**  
- Main bundle is 1.5+ MB (459 KB gzipped)
- Exceeds recommended chunk size of 500 KB
- Poor performance on slow connections (mobile networks)
- All code loaded upfront instead of on-demand

**Recommended Fixes:**
1. Implement code splitting for page routes using `React.lazy()`
2. Configure manual chunks in Rollup/Vite options
3. Lazy load components not needed on initial page load
4. Separate vendor chunks (React, Vue, etc.)
5. Analyze bundle with `vite-plugin-visualizer`

**Example - Code Splitting Lazy Routes:**
```typescript
const CTF = lazy(() => import('./pages/CTF'));
const Story = lazy(() => import('./pages/Story'));
const Operators = lazy(() => import('./pages/Operators'));

// Routes will load on-demand
```

**Example - Vite Config Manual Chunks:**
```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['react', 'react-dom'],
          'router': ['react-router-dom'],
          'ui': ['@radix-ui/react-dialog', '@radix-ui/react-dropdown-menu'],
          'game': ['three', 'cannon']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
});
```

**Impact:** Slow initial page load (LCP metric); poor mobile experience; higher bounce rates  
**Estimated Effort:** 2 hours  
**Checklist:**
- [ ] Install `vite-plugin-visualizer` and analyze bundle
- [ ] Identify non-critical components
- [ ] Implement lazy loading for routes
- [ ] Configure manual chunks for vendor libraries
- [ ] Test build output size with `npm run build`
- [ ] Test page load times with DevTools
- [ ] Verify lazy loading works in production

---

### 🔇 [M3] Empty Catch Blocks - Silent Error Swallowing

**Priority:** MEDIUM - Makes debugging difficult  
**Severity:** MEDIUM (Debuggability)  
**Files Affected:**
- [src/api/userApi.ts](src/api/userApi.ts#L46) - Line 46: Empty catch block
- [src/api/shadownetApi.ts](src/api/shadownetApi.ts#L46) - Line 46
- [src/api/shadownetApi.ts](src/api/shadownetApi.ts#L58) - Line 58
- [src/api/operatorApi.ts](src/api/operatorApi.ts#L96) - Line 96: Minimal logging

**Current Code Example (userApi.ts):**
```typescript
try {
  return await apiFetch<UserProgression>('/users/me/progress');
} catch {  // No error handling
  const user = await apiFetch<User>('/users/me');
  return toUserProgression(user);
}
```

**Issue:**  
- Errors silently swallowed without logging
- Makes debugging production issues extremely difficult
- Cannot trace what went wrong
- Error tracking systems (Sentry, LogRocket) won't see these errors

**Fix Steps:**
1. Add proper error logging to all catch blocks:
   ```typescript
   } catch (error) {
     console.error('Failed to fetch user progression:', error);
     // Track to error monitoring service
     logError('userApi:getUserProgression', error);
     // Then continue with fallback...
     const user = await apiFetch<User>('/users/me');
     return toUserProgression(user);
   }
   ```

2. For each catch block, decide:
   - Should error be logged?
   - Should error be reported to monitoring?
   - Should error be shown to user?
   - Should we rethrow or fallback?

3. Create standardized error logging utility function

**Impact:** Silent failures; no visibility into API errors; production bugs hard to diagnose  
**Estimated Effort:** 45 minutes  
**Checklist:**
- [ ] Create errorLogger utility function
- [ ] Add logging to all catch blocks
- [ ] Configure error tracking (Sentry or similar)
- [ ] Test error logging with intentional failures
- [ ] Verify errors appear in error monitoring dashboard

---

### 📝 [M4] TypeScript 'any' Type Usage Without Documentation

**Priority:** MEDIUM - Loss of type safety  
**Severity:** MEDIUM (Code Quality)  
**Count:** 7+ instances  
**Files:**
- [src/lib/apiClient.ts](src/lib/apiClient.ts#L18) - Line 18: `config.body as unknown`
- [src/lib/apiClient.ts](src/lib/apiClient.ts#L29) - Line 29: `nextConfig as any`
- [src/lib/apiClient.ts](src/lib/apiClient.ts#L52) - Line 52: `config.headers as any`
- [src/api/userApi.ts](src/api/userApi.ts#L67) - Line 67: `queryClient as any`
- [src/hooks/useAudio.ts](src/hooks/useAudio.ts#L26) - Line 26: `as unknown`

**Issue:**  
- Type assertions bypass TypeScript's type safety
- No documentation explaining why assertion is necessary
- Potential for runtime errors
- Makes code harder to maintain

**Fix Steps:**
1. For each `as any` or `as unknown`:
   - Try to replace with proper types
   - If must keep assertion, add JSDoc explaining why
   - Create proper type definitions for complex objects

**Example Fixes:**

Before:
```typescript
// Line 29 in apiClient.ts
const nextConfig = config as any;
```

After (Option 1 - Proper Type):
```typescript
interface ApiConfig {
  body?: unknown;
  headers?: Record<string, string>;
  // ... other properties
}
const nextConfig: ApiConfig = { ...config };
```

After (Option 2 - JSDoc with Assertion):
```typescript
// Line 29 in apiClient.ts
/**
 * @TODO - Determine proper type for fetch config merging
 * Temporarily using 'any' to merge config objects with mixed types
 * Should create a Config union type when all consumers are identified
 */
const nextConfig = config as any;
```

**Impact:** Loss of type safety; harder to refactor; potential runtime type errors  
**Estimated Effort:** 1.5 hours  
**Checklist:**
- [ ] Audit all `as any` usages
- [ ] Create proper types where possible
- [ ] Add JSDoc to unavoidable assertions
- [ ] Enable strict TypeScript checking in tsconfig
- [ ] Run TypeScript check with `--noImplicitAny`

---

### 🗄️ [M5] Database Configuration Port Mismatch

**Priority:** MEDIUM - Dev/Prod inconsistency  
**Severity:** MEDIUM (Configuration)  
**Files Affected:**
- [docker-compose.yml](docker-compose.yml) - MySQL port mapping
- [springboot/src/main/resources/application.yml](springboot/src/main/resources/application.yml)

**Issue:**  
- Docker Compose MySQL: `3305:3306` (non-standard port 3305 exposed)
- Dev config uses: `jdbc:mysql://127.0.0.1:3305/shadownet` (port 3305)
- Prod config uses: `jdbc:mysql://db:3306/shadownet` (port 3306 - correct)
- Port mismatch could cause connection failures

**Fix Steps:**
1. Review why port 3305 is needed in Docker:
   - If only for local development: keep as-is with local connection string
   - If inconsistency issue: standardize to 3306
2. Verify `application-dev.yml` uses correct port: 3305
3. Verify `application-prod.yml` uses correct port: 3306 (with Docker service name: `db`)
4. Document port usage in README

**Example Fix (docker-compose.yml):**
```yaml
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: shadownet
    ports:
      - "3305:3306"  # Local dev only; internal service uses 3306
    volumes:
      - db_data:/var/lib/mysql
```

**Example Fix (application-dev.yml):**
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3305/shadownet  # Matches docker-compose port
```

**Impact:** Dev database connection failures if port not configured correctly  
**Estimated Effort:** 15 minutes  
**Checklist:**
- [ ] Verify docker-compose port mapping
- [ ] Verify application-dev.yml database URL
- [ ] Verify application-prod.yml database URL
- [ ] Test dev database connection locally
- [ ] Document port usage in README

---

### 🔄 [M6] Missing @Transactional Annotations

**Priority:** MEDIUM - Data consistency risk  
**Severity:** MEDIUM (Stability)  
**Issue:**  
- Some service methods that modify state may not be transactional
- Changes may not be persisted atomically
- If exception occurs mid-operation, data could be left in inconsistent state

**Fix Steps:**
1. Audit all `Service` classes for methods that:
   - Call multiple repository methods
   - Modify database state
   - Have business logic that requires atomicity
2. Add `@Transactional` to methods that need it
3. Configure rollback behavior for exceptions
4. Test with intentional transaction failures

**Example:**
```java
@Service
public class AccusationService {
    
    @Transactional  // Add this
    public Accusation submitAccusation(AccusationDTO dto) {
        // This entire method now runs in a single transaction
        Accusation accusation = new Accusation();
        accusationRepository.save(accusation);  // Step 1
        
        updateChallengeState(dto.challengeId);  // Step 2
        updateLeaderboard(dto.userId);          // Step 3
        
        // If any step fails, entire method rolls back
        return accusation;
    }
}
```

**Impact:** Data inconsistency; partial updates applied when failures occur  
**Estimated Effort:** 30 minutes  
**Checklist:**
- [ ] Audit service methods for transaction requirements
- [ ] Add @Transactional to appropriate methods
- [ ] Configure propagation and rollback behavior
- [ ] Test with intentional failures (throw exceptions mid-operation)
- [ ] Verify database rollback on exception

---

### 📥 [M7] Unused Imports in Test Files

**Priority:** MEDIUM - Code hygiene  
**Severity:** MEDIUM (Code Quality)  
**Files Affected:**
- [springboot/src/test/java/com/shadownet/nexus/service/ChallengeServiceTest.java](springboot/src/test/java/com/shadownet/nexus/service/ChallengeServiceTest.java#L15) - Line 15: Unused `PasswordEncoder` import
- [springboot/src/test/java/com/shadownet/nexus/service/PCGSoloChallengeServiceTest.java](springboot/src/test/java/com/shadownet/nexus/service/PCGSoloChallengeServiceTest.java#L13) - Line 13: Unused `PasswordEncoder` import
- [springboot/src/test/java/com/shadownet/nexus/mapper/ChallengeViewMapperTest.java](springboot/src/test/java/com/shadownet/nexus/mapper/ChallengeViewMapperTest.java#L3) - Line 3: Unused `ObjectMapper` import

**Issue:**  
- Unused imports clutter code
- May indicate incomplete refactoring
- IDE warnings/errors
- Wastes time on code review

**Fix Steps:**
1. Remove each unused import
2. Run IDE "Organize Imports" command
3. Configure IDE to remove unused imports automatically on save

**Impact:** Minor code hygiene issue; cluttered code  
**Estimated Effort:** 5 minutes  
**Checklist:**
- [ ] Remove unused imports from each file
- [ ] Run IDE organize imports
- [ ] Enable auto-organize on save in IDE settings

---

### 🐳 [M8] Docker Frontend Build - Conflicting Lock Files

**Priority:** MEDIUM - Docker build reliability  
**Severity:** MEDIUM (Build)  
**File:** [Dockerfile.frontend](Dockerfile.frontend)  
**Lines:** 7-8  

**Current Code:**
```dockerfile
COPY package*.json bun.lock* pnpm-lock.yaml* ./
RUN npm ci
```

**Issue:**  
- Dockerfile copies multiple lock file types (npm, bun, pnpm)
- Then runs `npm ci` which only uses package-lock.json
- Unclear which package manager is being used
- Could cause lock file conflicts or unexpected behavior
- Mixing package managers (npm with bun/pnpm lock files)

**Fix Steps:**
1. Determine which package manager is actually used: `npm`, `bun`, or `pnpm`
2. Remove lock files for unused package managers
3. Use correct install command:
   - npm: `npm ci`
   - bun: `bun install --frozen-lockfile`
   - pnpm: `pnpm install --frozen`
4. Document in Dockerfile which package manager is used

**Example Fix (if using npm):**
```dockerfile
COPY package.json package-lock.json ./
RUN npm ci
```

**Example Fix (if using bun):**
```dockerfile
COPY package.json bun.lock* ./
RUN bun install --frozen-lockfile
```

**Impact:** Docker builds may fail or produce inconsistent containers  
**Estimated Effort:** 10 minutes  
**Checklist:**
- [ ] Verify which package manager is actually used in package.json scripts
- [ ] Remove lock files for unused package managers
- [ ] Update Dockerfile COPY and RUN commands
- [ ] Test Docker build locally
- [ ] Verify container can start correctly

---

### ⚠️ [M9] Missing Error Boundary Coverage

**Priority:** MEDIUM - Application stability  
**Severity:** MEDIUM (Reliability)  
**File:** [src/components/ErrorBoundary.tsx](src/components/ErrorBoundary.tsx)  
**Issue:**  
- Error boundary component exists but not all pages may be wrapped
- Unhandled errors crash entire application instead of graceful fallback
- Users see blank screen instead of error message

**Fix Steps:**
1. Wrap entire app or all pages with ErrorBoundary
2. Wrap individual routes with ErrorBoundary
3. Configure ErrorBoundary to display user-friendly error messages
4. Add error logging/reporting to ErrorBoundary

**Example Fix (in App.tsx or main route):**
```tsx
import ErrorBoundary from './components/ErrorBoundary';

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          {/* All routes wrapped */}
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
```

**Or per-route:**
```tsx
<Routes>
  <Route path="/ctf" element={
    <ErrorBoundary>
      <CTF />
    </ErrorBoundary>
  } />
  <Route path="/story" element={
    <ErrorBoundary>
      <Story />
    </ErrorBoundary>
  } />
</Routes>
```

**Impact:** Unhandled errors crash app; poor user experience; loss of data  
**Estimated Effort:** 30 minutes  
**Checklist:**
- [ ] Verify all pages wrapped with ErrorBoundary
- [ ] Test error handling by throwing intentional error
- [ ] Configure error boundary to show user message
- [ ] Add error logging to error boundary
- [ ] Test in production-like environment

---

### 📨 [M10] Missing Input Validation on Frontend API Calls

**Priority:** MEDIUM - Data quality and UX  
**Severity:** MEDIUM (Reliability)  
**Issue:**  
- Frontend makes API calls without comprehensive input validation
- Invalid data reaches backend causing 400 errors
- Poor user experience with generic error messages
- Could allow injection attacks if validation relied on backend only

**Fix Steps:**
1. Create validation schema for common API calls (Zod, Yup, Joi)
2. Validate all user inputs before API calls
3. Show clear validation error messages
4. Type-check all API parameters

**Example (using Zod):**
```typescript
import { z } from 'zod';

const submitAccusationSchema = z.object({
  userId: z.string().uuid('Invalid user ID'),
  targetId: z.string().uuid('Invalid target ID'),
  challengeId: z.number().positive('Invalid challenge ID'),
  evidence: z.string().min(10, 'Evidence required (min 10 chars)').max(500),
});

// In component:
const handleSubmit = async (data: unknown) => {
  try {
    const validated = submitAccusationSchema.parse(data);
    const result = await submitAccusation(validated);
    // Success
  } catch (error) {
    if (error instanceof z.ZodError) {
      setErrors(error.flatten());  // Show validation errors
    }
  }
};
```

**Impact:** Poor user experience; wasted API calls; potential injection attacks  
**Estimated Effort:** 2 hours  
**Checklist:**
- [ ] Choose validation library (Zod recommended)
- [ ] Create schemas for all major API endpoints
- [ ] Add validation before all API calls
- [ ] Display validation errors to user
- [ ] Test with invalid inputs

---

### 🚦 [M11] Missing Rate Limit Response Headers

**Priority:** MEDIUM - Client awareness  
**Severity:** MEDIUM (API Quality)  
**Issue:**  
- Rate limiter configured but rate-limit headers not returned to client
- Clients unaware of rate limits until they hit them
- No visibility into remaining requests or reset time
- Poor client-side UX; no ability to implement backoff strategies

**Fix Steps:**
1. Configure rate limiter to return standard headers:
   - `RateLimit-Limit`: Total requests allowed
   - `RateLimit-Remaining`: Remaining requests
   - `RateLimit-Reset`: Unix timestamp when limit resets
   - `Retry-After`: Seconds to wait before retrying (on 429)

2. Update API responses to include these headers
3. Update frontend API client to read and display rate limit info
4. Implement backoff strategies on client

**Example (Spring Boot):**
```java
@Component
public class RateLimitHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) {
        HttpServletResponse response = (HttpServletResponse) resp;
        
        // After checking rate limit...
        response.setHeader("RateLimit-Limit", "100");
        response.setHeader("RateLimit-Remaining", "42");
        response.setHeader("RateLimit-Reset", String.valueOf(resetTime));
        
        chain.doFilter(req, resp);
    }
}
```

**Impact:** Clients hit rate limits unexpectedly; no ability to implement backoff  
**Estimated Effort:** 1 hour  
**Checklist:**
- [ ] Add rate limit headers to responses
- [ ] Document rate limit headers in API contract
- [ ] Update frontend API client to read rate limit headers
- [ ] Display rate limit status in UI
- [ ] Implement client-side backoff strategies
- [ ] Test with intentional rate limit triggers

---

### 🌐 [M12] WebSocket Configuration - Potential Origin Mismatch

**Priority:** MEDIUM - Feature reliability  
**Severity:** MEDIUM (Stability)  
**File:** [springboot/src/main/java/com/shadownet/nexus/config/WebSocketConfig.java](springboot/src/main/java/com/shadownet/nexus/config/WebSocketConfig.java#L16)  
**Issue:**  
- CORS origins for WebSocket may not match SecurityConfig CORS origins
- WebSocket connections blocked due to CORS mismatch
- Users can't join multiplayer games/real-time features

**Fix Steps:**
1. Verify WebSocketConfig CORS origins
2. Compare with SecurityConfig CORS origins (should match)
3. Ensure both are updated together when origins change
4. Consider extracting to shared configuration

**Example Fix:**
```java
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins(
                "https://shadownet-nexus.vercel.app",
                "https://localhost:5173"
            )
            .withSockJS();
    }
}
```

**Or better - use shared configuration:**
```java
@Configuration
public class CorsConfig {
    public static final String[] ALLOWED_ORIGINS = {
        "https://shadownet-nexus.vercel.app",
        "https://localhost:5173"
    };
}

// Use in both WebSocketConfig and SecurityConfig
registry.setAllowedOrigins(CorsConfig.ALLOWED_ORIGINS);
```

**Impact:** WebSocket connections fail; real-time features broken  
**Estimated Effort:** 30 minutes  
**Checklist:**
- [ ] Verify WebSocketConfig origins
- [ ] Compare with SecurityConfig origins
- [ ] Ensure they match
- [ ] Extract to shared configuration if needed
- [ ] Test WebSocket connection from both local and production URLs

---

## Low Priority Fixes (Code Quality)

### 🧹 [L1] Unused Imports - Code Hygiene

**Priority:** LOW  
**Severity:** LOW  
**File:** [springboot/src/main/java/com/shadownet/nexus/mapper/ChallengeViewMapper.java](springboot/src/main/java/com/shadownet/nexus/mapper/ChallengeViewMapper.java#L11) - Line 11: Unused `ArrayList` import

**Fix:** Remove unused import line  
**Estimated Effort:** 2 minutes  

---

### 💾 [L2] LocalStorage Direct Access Without Null Checks

**Priority:** LOW  
**Severity:** LOW  
**Files:**
- [src/context/GameContext.tsx](src/context/GameContext.tsx#L280) - Line 280
- [src/context/AuthContext.tsx](src/context/AuthContext.tsx#L27) - Line 27
- [src/pages/Leaderboard.tsx](src/pages/Leaderboard.tsx#L22) - Line 22

**Current Pattern:**
```typescript
const [userId] = useState(localStorage.getItem('userId') || '');
```

**Recommendation:** Add null checks explicitly for clarity (minor risk as fallback is provided)  
**Estimated Effort:** 10 minutes  

---

### 🌍 [L3] Hard-Coded Error Messages - Not Internationalized

**Priority:** LOW  
**Severity:** LOW  
**Issue:** Hard-coded English error messages throughout codebase  
**Impact:** Not scalable for multi-language support  
**Estimated Effort:** 3+ hours (depends on scope)  

---

### ⚡ [L4] No Loading States for Async Operations

**Priority:** LOW  
**Severity:** LOW  
**Issue:** Some async API calls don't show loading indicators  
**Impact:** Poor UX; users unaware of pending operations  
**Estimated Effort:** 1 hour  

---

### 📚 [L5] Missing JSDoc Comments

**Priority:** LOW  
**Severity:** LOW  
**Issue:** Complex functions lack documentation  
**Impact:** Code maintainability concerns  
**Estimated Effort:** 2+ hours  

---

### 🔢 [L6] Magic Numbers Throughout Codebase

**Priority:** LOW  
**Severity:** LOW  
**Examples:**
- Timeout values: `24 * 60 * 60 * 1000` (email verification expiry)
- Session timeouts, rate limit counts, pagination sizes

**Fix:** Extract to named constants  
**Estimated Effort:** 1 hour  

---

### 🔒 [L7] Missing HTTPS Enforcement

**Priority:** LOW (Production Only)  
**Severity:** LOW  
**File:** [springboot/src/main/resources/application.yml](springboot/src/main/resources/application.yml#L32)  
**Issue:** `secure: false` for cookies in dev; no HTTPS redirect in prod  
**Impact:** Cookies transmitted over HTTP in production  
**Estimated Effort:** 20 minutes  

---

### 📡 [L8] No API Response Versioning

**Priority:** LOW  
**Severity:** LOW  
**Issue:** API responses have no version headers or content negotiation  
**Impact:** Difficult to evolve API without breaking clients  
**Estimated Effort:** 2 hours  

---

### 🏥 [L9] Health Check Endpoint Details

**Priority:** LOW  
**Severity:** LOW  
**File:** [springboot/src/main/resources/application.yml](springboot/src/main/resources/application.yml#L47)  
**Issue:** Health endpoint visibility depends on authorization  
**Impact:** DevOps teams can't quickly verify health without authentication  
**Estimated Effort:** 15 minutes  

---

### 📊 [L10] No Structured Logging

**Priority:** LOW  
**Severity:** LOW  
**Issue:** Logging uses string concatenation instead of structured format  
**Impact:** Difficult to parse logs for monitoring/alerting  
**Estimated Effort:** 2 hours  

---

### [L11-L26] Additional Minor Issues (16 items)

- Missing null checks in multiple locations (15 min)
- Inconsistent error handling patterns (30 min)
- Missing ACID guarantees on multi-step operations (1 hour)
- No request/response compression configured (30 min)
- Missing security headers (X-Frame-Options, CSP, etc.) (45 min)
- Cache headers not configured (30 min)
- No request tracing/correlation IDs (1 hour)
- Missing batch operation limits (30 min)
- No GraphQL query complexity limits (1 hour)
- Missing API documentation in OpenAPI (2 hours)
- No performance monitoring instrumentation (1 hour)
- Missing graceful shutdown hooks (30 min)
- Batch operation size limits not enforced (30 min)
- No request timeout configuration (20 min)
- Missing audit logging for sensitive operations (1 hour)
- Data export/deletion features incomplete (2 hours)

---

## Implementation Strategy

### Phase 1: Critical & Security (Week 1)
1. [C1] Email encryption key validation
2. [H1] Re-enable CSRF protection
3. [H2] Fix authorization fallback
4. [H3] Restrict CORS wildcards
5. [H5] Type casting fix

**Estimated:** 2-3 hours  
**Blocking:** Everything else depends on these

### Phase 2: Stability & High Priority (Week 1-2)
1. [H4] React hook dependencies
2. [H6] Fast refresh violations
3. [H7] Deprecated method guidance
4. [H8] Deprecated imports
5. [M1] Vulnerable dependency updates

**Estimated:** 3-4 hours  
**Blocking:** App stability

### Phase 3: Code Quality & Performance (Week 2-3)
1. [M2] Build chunk sizes
2. [M3] Empty catch blocks
3. [M4] TypeScript any types
4. [M5] Database port config
5. [M6] Transactional annotations

**Estimated:** 4-5 hours  
**Blocking:** None, but important for maintainability

### Phase 4: Low Priority (Week 3+)
1. [L1-L26] Code hygiene and documentation items

**Estimated:** 8-10 hours  
**Blocking:** None

---

## Testing Checklist

After implementing fixes, verify:

- [ ] `npm run lint` passes with 0 warnings
- [ ] `npm run build` completes successfully
- [ ] `npm run type-check` shows no errors
- [ ] `npm test` all tests pass
- [ ] `npx cypress run` E2E tests pass
- [ ] `mvn clean test` backend tests pass
- [ ] `mvn clean compile` has no warnings (or explained ones)
- [ ] Application starts without errors
- [ ] All critical paths tested (CTF, Story, Team, Solo)

---

## Summary

**Total Issues:** 47  
**Critical:** 1 | **High:** 8 | **Medium:** 12 | **Low:** 26  

**Total Estimated Effort:**
- Phase 1: 2-3 hours
- Phase 2: 3-4 hours
- Phase 3: 4-5 hours
- Phase 4: 8-10 hours

**Total: ~18-25 hours**

**Priority Recommendation:** Complete all Critical and High priority items before production deployment.

---

*Generated: 2026-05-02 | Source: AUDIT_REPORT.md | Ready for Implementation*
