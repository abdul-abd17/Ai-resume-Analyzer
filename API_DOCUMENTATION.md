# AI Resume Analyzer API Documentation (Complete Steps 1 - 14)

Base URL: `http://localhost:8080/api`

Swagger OpenAPI 3 UI: `http://localhost:8080/swagger-ui/index.html` or `http://localhost/swagger-ui/index.html` (via Nginx)

---

## Authentication Endpoints (Step 1)
- `POST /auth/register`
- `POST /auth/login`
- `GET /users/profile`

---

## Resume Upload Module Endpoints (Step 2)
- `POST /resumes/upload`
- `GET /resumes`
- `GET /resumes/{id}`
- `GET /resumes/download/{id}`
- `DELETE /resumes/{id}`

---

## Resume Parser Module Endpoints (Step 3)
- `POST /parser/parse/{resumeId}`
- `GET /parser/{resumeId}`
- `GET /parser/raw/{resumeId}`

---

## ATS Analysis Engine Endpoints (Step 4)
- `POST /analysis/analyze/{resumeId}`
- `GET /analysis/{resumeId}`
- `GET /analysis/history`
- `DELETE /analysis/{id}`

---

## Job Description Matching Engine Endpoints (Step 5)
- `POST /job-description/upload`
- `POST /job-description/compare/{resumeId}?jobDescriptionId={jdId}`
- `GET /job-description`
- `GET /job-description/{id}`
- `GET /job-description/match-analysis/{resumeId}`
- `DELETE /job-description/{id}`

---

## Grammar & Readability Engine Endpoints (Step 6)
- `POST /grammar/analyze/{resumeId}`
- `GET /grammar/{resumeId}`
- `DELETE /grammar/{id}`

---

## AI Resume Improvement Engine Endpoints (Step 7)
- `POST /ai/analyze/{resumeId}`
- `GET /ai/{resumeId}`
- `DELETE /ai/{id}`

---

## Unified Analytics Dashboard Endpoints (Step 8)
- `GET /dashboard`
- `GET /dashboard/stats`
- `GET /dashboard/charts`
- `GET /dashboard/activity`

---

## Resume History & Version Management Endpoints (Step 9)
- `GET /versions`
- `GET /versions/{id}`
- `PUT /versions/{id}/rename`
- `POST /versions/{id}/restore`
- `DELETE /versions/{id}`
- `POST /versions/compare`

---

## Professional Report Generation Endpoints (Step 10)
- `POST /reports/generate/{resumeId}`
- `GET /reports`
- `GET /reports/{id}`
- `GET /reports/download/{id}`
- `DELETE /reports/{id}`

---

## Enterprise Admin & Recruiter Endpoints (Step 11)
- `GET /admin/dashboard`
- `GET /admin/users`
- `PUT /admin/users/{id}`
- `DELETE /admin/users/{id}`
- `GET /admin/recruiters`
- `GET /admin/logs`
- `POST /admin/roles`
- `GET /recruiter/candidates`

---

## SaaS Notifications & Background Jobs Endpoints (Step 12)
- `GET /jobs`
- `POST /jobs/{id}/retry`
- `POST /jobs/{id}/cancel`
- `GET /notifications`
- `PUT /notifications/{id}/read`
- `DELETE /notifications/clear-all`

---

## Actuator Monitoring & OpenAPI Swagger Endpoints (Steps 13 & 14)
- `GET /actuator/health`: System & Database Health Check
- `GET /v3/api-docs`: OpenAPI 3 JSON Spec
- `GET /swagger-ui/index.html`: Interactive Swagger UI Console
