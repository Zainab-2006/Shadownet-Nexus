# Shadownet Nexus Deployment TODO

## [ ] 1. Clean repo
- [ ] Remove springboot/target/, all *.log, node_modules
- [ ] Update .gitignore

## [ ] 2. Backend Postgres switch
- [ ] Edit pom.xml
- [ ] Edit application.yml
- [ ] Update CORS configs

## [ ] 3. Create GitHub repo & push clean code

## [ ] 4. Deploy Backend Render
- [ ] New Web Service > GitHub repo > root: springboot
- [ ] Build: ./mvnw clean package
- [ ] Start: java -jar target/*.jar
- [ ] Env vars + Render Postgres

## [ ] 5. Deploy Frontend Vercel
- [ ] Import repo root /
- [ ] Build: npm run build | Output: dist
- [ ] VITE_API_URL=backend/api

## [ ] 6. Test all features
- [ ] Login/register, operators, missions, leaderboard, story

## [ ] 7. Final URLs & changed files list
