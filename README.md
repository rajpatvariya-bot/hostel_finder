# Hostel Finder Web Application (Indore) — College Project

This repo has **two folders**:

- `frontend/` → HTML/CSS/JS pages (you edit mostly this)
- `backend/` → Spring Boot REST API + MySQL

## How data flows (easy viva explanation)

### Public search (no login)
1. Open `frontend/pages/index.html`
2. `frontend/js/publicSearch.js` calls:
   - `GET /api/public/areas?cityId=1`
   - `GET /api/public/facilities`
   - `GET /api/public/hostels?...filters...`
3. Spring Boot reads from **MySQL** and returns JSON
4. JS renders hostel cards.

### Hostel details + inquiry (login required)
1. Open a hostel from search → `frontend/pages/hostel-details.html?id=...`
2. JS calls `GET /api/public/hostels/{id}`
3. Clicking **Send Inquiry** calls `POST /api/student/inquiries` (requires JWT).

## Setup (Backend)

### 1) Create MySQL database
Create a DB named `hostel_finder`.

### 2) Configure DB user/password
Edit:
- `backend/src/main/resources/application.properties`

### 3) Run backend
From `backend/`:

```bat
.\mvnw.cmd spring-boot:run
```

Flyway will create tables and insert demo seed data (Indore, areas, facilities, demo hostels).

Backend runs on:
- `http://localhost:8080`

## Setup (Frontend)

Because this frontend uses JS modules (`type="module"`), you must serve it over `http://` (not `file://`).

From `frontend/`:

```bat
python -m http.server 5500
```

Then open:
- `http://localhost:5500/pages/index.html`

Because the frontend calls `http://localhost:8080/api/...`, keep backend running.

## Demo endpoints you can show

- `GET /api/public/areas?cityId=1`
- `GET /api/public/facilities`
- `GET /api/public/hostels?cityId=1&availableOnly=true`
- `GET /api/public/hostels/{id}`

## Next planned features (after the demo works)
- JWT implementation fully (login/register)
- Owner verification pages wired
- Inquiry creation and owner accept/reject
- Real-time availability (transactional room reservation)

