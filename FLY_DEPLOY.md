# Fly.io Deployment

This repo can run on Fly.io as two Fly apps:

- `shadownet-nexus-backend`: Spring Boot API, built from `Dockerfile`
- `shadownet-nexus-mysql`: private MySQL 8 app with a persistent Fly volume

Fly does not deploy `docker-compose.yml` directly. The backend connects to MySQL through Fly private networking at:

```text
shadownet-nexus-mysql.internal:3306
```

Fly may require billing information or prepaid credit before it allows app creation, even for small machines.

## 1. Install And Login

Install `flyctl`, then log in:

```powershell
fly auth login
```

## 2. Create The MySQL App

Create the Fly app and volume:

```powershell
fly apps create shadownet-nexus-mysql
fly volumes create mysql_data --size 1 --region bom --app shadownet-nexus-mysql
```

Set the MySQL passwords as Fly secrets. These are intentionally not stored in Git:

```powershell
fly secrets set MYSQL_PASSWORD=<mysql-password> MYSQL_ROOT_PASSWORD=<mysql-root-password> --app shadownet-nexus-mysql
```

Deploy MySQL from the repo root:

```powershell
fly deploy --config deploy/fly-mysql.toml --app shadownet-nexus-mysql
```

## 3. Create The Backend App

Create the backend app:

```powershell
fly apps create shadownet-nexus-backend
```

Set backend secrets:

```powershell
fly secrets set DB_PASSWORD=<mysql-password> JWT_SECRET=<real-64-plus-character-random-secret> --app shadownet-nexus-backend
```

Update `CORS_ORIGINS` in `fly.toml` to your actual frontend URL before production deploy.

Deploy the backend:

```powershell
fly deploy --app shadownet-nexus-backend
```

## 4. Verify

Check app status and logs:

```powershell
fly status --app shadownet-nexus-backend
fly logs --app shadownet-nexus-backend
```

Check the health endpoint:

```powershell
Invoke-WebRequest https://shadownet-nexus-backend.fly.dev/actuator/health
```

The expected database config for the backend is:

```env
DATABASE_URL=jdbc:mysql://shadownet-nexus-mysql.internal:3306/shadownet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=shadownet
DB_PASSWORD=<mysql-password>
```

If startup fails with `Access denied for user 'shadownet'`, the MySQL volume was probably initialized with a different password. Either change the password inside MySQL or recreate the MySQL app volume.
