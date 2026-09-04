# AI Resume Analyzer – Smart ATS Resume Evaluation System (Production Ready Cloud SaaS)

An enterprise-grade, full-stack ATS resume evaluation SaaS platform built with **Java 21 Spring Boot** and **React + TypeScript + Tailwind CSS**.

> **Complete 14-Step Application Architecture Roadmap**:
> - **Step 1**: Landing Page, Auth (Register/Login with JWT), Dashboard Layout with Dark/Light mode, Zod/Bean validation, PostgreSQL entity mapping.
> - **Step 2**: **Resume Upload Module** with file storage (`/uploads/resumes/`), file type/size validation (PDF & DOCX up to 10MB), paginated history with search & sorting, file download, file deletion, and resume metadata details.
> - **Step 3**: **Resume Parsing Engine** with **Apache PDFBox** & **Apache POI** text extraction, text cleaning & space normalization, regex matching (Email, Phone, LinkedIn, GitHub, Portfolio URLs, Name heuristics, Location), section detection (Summary, Skills, Experience, Education, Projects, Certifications, Languages, Achievements), `ParsedResume` entity persistence, and rich structured UI display with a collapsible raw text viewer.
> - **Step 4**: **ATS Analysis Engine (Java DSA Powered)** with **HashMap** word & skill frequency analysis, **HashSet** O(1) keyword detection, **PriorityQueue** max-heap skill ranking, **TreeMap** category statistics, **Comparator** priority suggestion sorting, **Greedy 100-Point Algorithm**, seeded skill keyword database, REST APIs, and **Recharts** visualizations (Pie, Bar, Line, Radar charts).
> - **Step 5**: **Job Description Matching Engine (Java DSA Powered)** with **HashSet** `O(1)` set intersection (Matched Skills), set difference (Missing Skills), and complement (Extra Skills), **PriorityQueue** missing skill ranking, **5-Part Weighted Match Formula** (Skills 40%, Keyword Coverage 20%, Experience 20%, Education 10%, Projects 10%), TXT/PDF file upload & paste input, REST APIs, and **Recharts** comparison dashboard.
> - **Step 6**: **Grammar & Readability Analysis Engine** with **LanguageTool Java API** integration, custom Java rule detectors (**WeakVerbDetector**, **PassiveVoiceDetector**, **ReadabilityAnalyzer** Flesch reading score, **RepeatedWordDetector**), **40-30-15-15 Weighted Writing Score**, REST APIs, and **Recharts** grammar dashboard.
> - **Step 7**: **AI Resume Improvement Engine** with pluggable **AIProvider abstraction layer** (**GeminiProvider**, **OpenAIProvider**, **RuleFallbackProvider**), **PromptBuilder** synthesizing Parsed Resume, ATS, Grammar & Job Match reports, `AIRecommendation` entity, REST APIs, and **React AI Recommendations Dashboard**.
> - **Step 8**: **Unified Analytics Dashboard Home** with backend `DashboardService` aggregator, `DashboardDto`, slide-over Notification Center drawer, 7 glassmorphic stats cards, 6 Recharts visualizations, tabbed analytics, Search & Filter toolbar, and Report Export tools.
> - **Step 9**: **Resume History & Version Management** with automatic `ResumeVersion` creation (Version 1, Version 2, Version 3...), `ResumeComparison` entity, `ComparisonEngine` (Java DSA Set Difference $B \setminus A$ for added skills, $A \setminus B$ for removed skills), version restoration, version renaming, REST APIs, and **React Version History & Side-by-Side Comparison Dashboards**.
> - **Step 10**: **Professional Report Generation System** with **OpenPDF (LGPL)** Java PDF document generation (`PdfGenerator`), `Report` entity, PDF download endpoints (`/api/reports/download/{id}`), REST APIs, and **React Reports Dashboard** featuring responsive HTML preview modal, Zoom controls (100%-175%), Print view, and PDF download triggers.
> - **Step 11**: **Enterprise Admin & Recruiter Dashboard** with Role-Based Access Control (**`ROLE_USER`**, **`ROLE_RECRUITER`**, **`ROLE_ADMIN`**), `Recruiter` and `ActivityLog` entities, User/Role Management, Recruiter Management, System Activity Audit Logs, Candidate Talent Pool Search engine, and **React Admin (`/admin`) & Recruiter (`/recruiter`) Portals**.
> - **Step 12**: **SaaS Notifications & Asynchronous Background Job Processing** with **Spring Async ThreadPool** (`saasTaskExecutor`), **Spring Mail** HTML notification templates (`EmailServiceImpl` with fallback logging), **Spring Scheduling** cron maintenance, **Spring STOMP WebSocket** (`/ws`), `JobQueue` and `Notification` entities, REST APIs, and **React Background Job Dashboard (`/dashboard/jobs`)**.
> - **Step 13**: **Production Testing, Security Hardening & Performance Optimization** with **Spring Cache** (`ConcurrentMapCacheManager`), **Spring Boot Actuator** health indicators (`/actuator/health`), **FileSecurityValidator** (Magic-Bytes `%PDF-` & `PK\x03\x04` validation), **PerformanceInterceptor** & **LoggingInterceptor**, **JUnit 5 & Mockito** unit test suite (`AuthServiceTest`, `AtsAnalysisEngineTest`, `JobMatchingEngineTest`), **React ErrorBoundary**, and **Playwright E2E Test Suite** (`app.spec.ts`).
> - **Step 14**: **Production Cloud Deployment & DevOps System** featuring **Docker Multi-Stage Build** images, **Docker Compose** container orchestration, **Nginx Reverse Proxy** with SSL/TLS security headers & STOMP WebSocket proxying, **Springdoc OpenAPI 3 / Swagger UI** (`/swagger-ui.html`), **GitHub Actions CI/CD Pipeline**, **Automated PostgreSQL & Uploads Backup Scripts** (`scripts/backup.sh`), and **Prometheus Monitoring** (`monitoring/prometheus.yml`).

---

## 🛠️ Tech Stack & Complete System Architecture

### Frontend
- **Framework**: React 18, TypeScript, Vite
- **WebSockets**: SockJS, STOMP Client
- **Data Visualizations**: Recharts
- **Styling**: Tailwind CSS, PostCSS, Glassmorphism
- **Animations**: Framer Motion
- **Form & Validation**: React Hook Form, Zod
- **Icons**: Lucide React
- **HTTP Client**: Axios

### Backend & Core Services
- **Core**: Java 21, Spring Boot 3.3.x
- **Documentation**: Springdoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
- **DevOps & Cloud**: Docker, Docker Compose, Nginx, Prometheus, Grafana, GitHub Actions
- **Caching Engine**: Spring Cache (`ConcurrentMapCacheManager`)
- **Monitoring & Metrics**: Spring Boot Actuator (`/actuator/health`)
- **Asynchronous Engine**: `@EnableAsync` with `ThreadPoolTaskExecutor`
- **Mail Engine**: Spring Boot Starter Mail (`JavaMailSender`)
- **WebSocket Broker**: Spring STOMP WebSocket (`/ws`)
- **Security**: Spring Security 6, JWT (`jjwt`), BCrypt, RBAC (`ROLE_USER`, `ROLE_RECRUITER`, `ROLE_ADMIN`)
- **PDF Engine**: OpenPDF 1.3.39 (`com.github.librepdf:openpdf`)
- **Parsing Libraries**: Apache PDFBox 3.0.2, Apache POI 5.2.5
- **Grammar Engine**: LanguageTool v6.4
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL 16, H2 Database fallback

---

## 🚀 Quick Run Instructions

### 1. Launch with Docker Compose (Production Ready)
```bash
cp .env.example .env
docker-compose up -d --build
```
Access points:
- **Web App**: `http://localhost`
- **OpenAPI Swagger UI**: `http://localhost/swagger-ui/index.html`
- **Actuator Health**: `http://localhost:8080/actuator/health`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`

### 2. Manual Development Mode
```bash
# Backend
cd backend && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```

---

## ✅ Step 14 Final Verification Checklist

- [x] **OpenAPI 3 / Swagger UI Integration**: Added `springdoc-openapi-starter-webmvc-ui:2.5.0` to `pom.xml` and created `OpenApiConfig.java`.
- [x] **Docker Multi-Stage Build**: Created `docker/Dockerfile.backend` and `docker/Dockerfile.frontend`.
- [x] **Docker Compose Multi-Container**: Configured `docker-compose.yml` orchestrating PostgreSQL 16, Redis 7, Backend, Frontend, Prometheus, and Grafana.
- [x] **Nginx Reverse Proxy**: Configured `nginx/nginx.conf` and `nginx/conf.d/default.conf` routing `/`, `/api/`, `/ws/` (WebSocket Upgrade headers), and SSL security headers.
- [x] **CI/CD Pipeline**: Configured `.github/workflows/ci-cd.yml` running linting, unit tests, frontend build, and Docker compilation.
- [x] **Backup & Recovery Scripts**: Created `scripts/backup.sh` and `scripts/restore.sh`.
- [x] **Prometheus Scraper Config**: Created `monitoring/prometheus.yml`.
- [x] **Production Cloud Guides**: Created `DEPLOYMENT.md` and `ARCHITECTURE.md`.
- [x] **Zero Errors**: TypeScript (`npx tsc --noEmit`) and Vite production bundle (`npm run build`) compiled with **0 errors**.
