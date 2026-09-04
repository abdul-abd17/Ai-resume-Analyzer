# ResuMatch Production Deployment Manual & Architecture Guide

Complete, step-by-step production deployment manual for the **AI Resume Analyzer System**. This guide explains how to deploy the application so it can be accessed from any computer or mobile device using a public HTTPS URL (`https://your-domain.com`).

---

## 🏛️ Architecture Overview

The system consists of 3 primary decoupled components:
1. **Frontend**: React + TypeScript SPA bundled with Vite, served via Nginx with HTML5 History mode fallback (`try_files $uri $uri/ /index.html;`).
2. **Backend**: Spring Boot 3 Java service executing parsing engines, scoring formulas, and AI prompt synthesis.
3. **Database & Storage**: PostgreSQL 15+ relational database and mounted persistent directory volume (`/app/uploads`).

---

## ⚡ Option A: Cloud Dual Platform Deployment (Render / Railway + Vercel)

This is the easiest cloud deployment path without managing virtual machines.

### Step 1: Provision Managed PostgreSQL Database
1. Sign up for a free PostgreSQL database on **Supabase**, **Neon**, or **Render**.
2. Copy the Connection String URL:
   `postgres://postgres:<PASSWORD>@<HOST>:5432/<DATABASE_NAME>?sslmode=require`

### Step 2: Deploy Backend API (Render / Railway / Fly.io)
1. Push your repository to GitHub.
2. Log into **Render.com** $\rightarrow$ **New** $\rightarrow$ **Web Service**.
3. Connect your repository, set Build Context to `./backend`, and environment to **Docker** (using `docker/Dockerfile.backend`) or **Java runtime**.
4. Configure Environment Variables in Render:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<HOST>:5432/<DATABASE_NAME>?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME`: `<DB_USER>`
   - `SPRING_DATASOURCE_PASSWORD`: `<DB_PASSWORD>`
   - `JWT_SECRET`: `<MINIMUM_32_CHAR_SECURE_RANDOM_KEY>`
   - `CORS_ALLOWED_ORIGINS`: `https://<YOUR-FRONTEND-SUBDOMAIN>.vercel.app`
   - `GEMINI_API_KEY`: `<YOUR_GEMINI_API_KEY>`
   - `OPENAI_API_KEY`: `<YOUR_OPENAI_API_KEY>`
   - `AI_ACTIVE_PROVIDER`: `gemini`
5. Deploy Web Service and note your Public Backend URL (e.g. `https://airesume-backend.onrender.com`).
6. Test Actuator Health Endpoint in browser:
   `https://airesume-backend.onrender.com/actuator/health` $\rightarrow$ `{"status":"UP"}`

### Step 3: Deploy Frontend SPA (Vercel / Netlify)
1. Log into **Vercel.com** $\rightarrow$ **Add New Project**.
2. Select your GitHub repository, choose **Root Directory** as `frontend`, and Build Command `npm run build`.
3. Add Environment Variable:
   - `VITE_API_BASE_URL`: `https://airesume-backend.onrender.com/api`
4. Click **Deploy**. Vercel generates a public HTTPS URL (e.g. `https://resumatch-ats.vercel.app`).
5. Update `CORS_ALLOWED_ORIGINS` in Render backend service to match your live Vercel URL.

---

## 🐳 Option B: Single-Host VPS Deployment (Docker Compose + Let's Encrypt SSL)

For self-hosting on AWS EC2, DigitalOcean Droplet, Linode, or GCP Compute Engine.

### Step 1: Server Preparation
- **Recommended VM Spec**: 2 vCPU, 4GB RAM (Ubuntu 22.04 LTS).
- **Domain Record**: Create DNS `A Record` pointing `yourdomain.com` to your VM Public IP.

### Step 2: Install Docker & Git
```bash
sudo apt update && sudo apt install -y docker.io docker-compose git certbot python3-certbot-nginx
sudo systemctl enable --now docker
```

### Step 3: Configure Environment Variables
```bash
git clone https://github.com/your-username/ai-resume-analyzer.git
cd ai-resume-analyzer
cp .env.example .env
nano .env
```
Ensure `.env` contains:
```env
SPRING_PROFILES_ACTIVE=prod
POSTGRES_DB=airesume_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=SuperSecurePass123!
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
CORS_ALLOWED_ORIGINS=https://yourdomain.com
GEMINI_API_KEY=AIzaSyYourProductionGeminiKeyHere
AI_ACTIVE_PROVIDER=gemini
```

### Step 4: Launch Production Stack
```bash
docker-compose up -d --build
```
Verify running containers:
```bash
docker-compose ps
```

### Step 5: Secure with Let's Encrypt HTTPS
```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## 🔒 Security & Production Checklist

1. **JWT Secret Enforcement**: Production startup **fails fast** if `JWT_SECRET` is unconfigured or under 256 bits.
2. **H2 Console Isolation**: Disabled in production (`spring.h2.console.enabled: false`). Accessible only under local `dev` profile.
3. **Database SQL Masking**: `show-sql: false` prevents schema telemetry leakage into stdout.
4. **Log Sanitization**: PII, API keys, bearer tokens, and SQL bind variables are stripped from production logs.
5. **Ownership Control**: Resource authorization enforced across all API endpoints (HTTP 403 on IDOR attempts).
6. **File Security**: `FileSecurityValidator` enforces magic bytes (`%PDF-`, `PK\x03\x04`), 10MB size cap, Zip bomb limits (30MB), and path traversal bounds.

---

## 🧪 Testing the Live Deployment

From any computer or mobile browser on an external network:
1. Open `https://your-domain.com`.
2. Register a new user account and log in.
3. Upload a sample `.pdf` or `.docx` resume.
4. Click **Run ATS Analysis** $\rightarrow$ Verify genuine ATS score calculation.
5. Paste a Job Description $\rightarrow$ Verify job match scoring.
6. Click **Generate AI Recommendations** $\rightarrow$ Verify evidence-grounded suggestions with `[VERIFY METRIC]` flags.
7. Click **Export PDF Report** $\rightarrow$ Download clean, fully formatted analysis PDF.
