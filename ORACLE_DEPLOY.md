# Oracle Cloud Deployment

This project is best deployed on Oracle Cloud Always Free as one Ubuntu VM running the existing Docker Compose stack:

- React frontend
- Spring Boot backend
- MySQL
- Redis

The repo is already verified locally with Docker Compose, so Oracle only needs Docker, Git, open firewall ports, and the cloned repo.

## 1. Create The VM

In Oracle Cloud Infrastructure, create an Always Free compute instance:

- Image: Ubuntu 22.04 or 24.04
- Shape: `VM.Standard.A1.Flex`
- OCPUs: 1 or 2
- Memory: 6 GB or 12 GB
- Boot volume: 50 GB
- Public IP: enabled

If A1 capacity is unavailable, try another availability domain in the same home region.

## 2. Open Network Ports

In the instance VCN security list or network security group, allow inbound TCP:

- `22` for SSH
- `80` for HTTP, if using Nginx later
- `443` for HTTPS, if using Nginx and TLS later
- `5173` for direct frontend testing
- `3001` for direct backend testing

For production, prefer exposing only `80` and `443` through Nginx.

## 3. Install Docker On The VM

SSH into the instance:

```bash
ssh ubuntu@YOUR_ORACLE_PUBLIC_IP
```

Install Docker and Git:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker ubuntu
```

Log out and SSH back in so the Docker group change applies.

## 4. Deploy The App

Clone and start the stack:

```bash
git clone https://github.com/Zainab-2006/Shadownet-Nexus.git
cd Shadownet-Nexus
docker compose up -d --build
```

Check status:

```bash
docker compose ps
docker compose logs --tail 100 app
```

Direct test URLs:

```text
http://YOUR_ORACLE_PUBLIC_IP:5173
http://YOUR_ORACLE_PUBLIC_IP:3001/actuator/health
```

## 5. Environment Values

The Docker Compose stack uses the internal MySQL service name:

```env
DATABASE_URL=jdbc:mysql://db:3306/shadownet?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=shadownet
DB_PASSWORD=<your-db-password>
```

Set these before starting the stack if you want to override defaults:

```bash
export DB_PASSWORD='replace-with-a-strong-password'
export MYSQL_ROOT_PASSWORD='replace-with-a-strong-root-password'
export JWT_SECRET='replace-with-a-real-64-plus-character-random-secret'
docker compose up -d --build
```

## 6. Optional Nginx Reverse Proxy

After direct testing works, add Nginx and point your domain to the Oracle public IP. Proxy:

- `/` to `http://127.0.0.1:5173`
- `/api/` to `http://127.0.0.1:3001/api/`
- `/actuator/health` to `http://127.0.0.1:3001/actuator/health`

Then use Certbot for HTTPS.

## 7. Maintenance

Update deployment:

```bash
cd Shadownet-Nexus
git pull
docker compose up -d --build
```

View logs:

```bash
docker compose logs -f app
docker compose logs -f db
```

Stop the stack:

```bash
docker compose down
```
