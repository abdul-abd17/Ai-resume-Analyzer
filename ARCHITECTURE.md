# ResuMatch System Architecture & Technical Specifications

Comprehensive system architecture design document for **ResuMatch ATS Evaluation System**.

---

## 🏛️ High-Level System Architecture

```
                               ┌────────────────────────────────────────┐
                               │           Users / Recruiters           │
                               │        (Browser / WebSockets)          │
                               └───────────────────┬────────────────────┘
                                                   │
                                                   ▼
                                       ┌────────────────────────┐
                                       │  Nginx Reverse Proxy   │
                                       │ (Port 80/443 SSL Proxy)│
                                       └───────────┬────────────┘
                                                   │
         ┌─────────────────────────────────────────┼─────────────────────────────────────────┐
         ▼                                         ▼                                         ▼
┌──────────────────────────┐             ┌──────────────────────────┐             ┌──────────────────────────┐
│   React Frontend (Vite)  │             │ Java 21 Spring Boot API  │             │   Prometheus & Grafana   │
│ (Tailwind, Recharts UI)  │             │ (JWT, DSA Engines, Async)│             │ (Actuator Health & CPU)  │
└──────────────────────────┘             └─────────┬────────────────┘             └──────────────────────────┘
                                                   │
                                     ┌─────────────┴─────────────┐
                                     ▼                           ▼
                          ┌────────────────────┐      ┌────────────────────┐
                          │ PostgreSQL 16 DB   │      │   Redis Cache      │
                          │(Spring Data JPA)   │      │(Spring Cache Engine│
                          └────────────────────┘      └────────────────────┘
```

---

## ⚙️ Core Engines & Design Patterns

1. **ATS Analysis Engine (`com.airesume.analyzer.service.ats`)**:
   - Uses `HashMap` word frequency counters ($O(n)$) and `HashSet` keyword matching ($O(1)$) to calculate a weighted 100-point ATS compatibility score.
2. **Job Description Matching Engine (`com.airesume.analyzer.service.job`)**:
   - Computes set intersection ($A \cap B$) for matched skills and set difference ($B \setminus A$) for missing skills using a 5-part weighted match formula (Skills 40%, Keyword Coverage 20%, Experience 20%, Education 10%, Projects 10%).
3. **Grammar & Readability Engine (`com.airesume.analyzer.service.grammar`)**:
   - Evaluates writing quality using LanguageTool v6.4, custom passive voice detectors, weak action verb detectors, and the Flesch Reading Ease index.
4. **AI Recommendation Subsystem (`com.airesume.analyzer.service.ai`)**:
   - Pluggable provider abstraction (`AIProvider`) supporting Gemini, OpenAI, and Rule Fallback engine.
5. **Resume Versioning Comparison Engine (`com.airesume.analyzer.service.version`)**:
   - Computes set differences ($B \setminus A$ for added skills, $A \setminus B$ for removed skills) across historical resume versions.
6. **OpenPDF Document Generator (`com.airesume.analyzer.service.report`)**:
   - Generates LGPL multi-page PDF evaluation reports with Cover Page, executive summary, tables, and headers/footers.
7. **SaaS Asynchronous & Notification Engine (`com.airesume.analyzer.service.saas`)**:
   - Offloads long-running tasks to `ThreadPoolTaskExecutor` and broadcasts STOMP WebSocket messages over `/ws`.
