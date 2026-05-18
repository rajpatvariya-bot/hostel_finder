# How to Run Hostel Finder Application

This guide explains how to start the Hostel Finder application with just one click!

## Quick Start (Recommended)

### Option 1: One-Click Launcher ⚡
Simply double-click **`run.bat`** in the project root directory.

This will:
1. ✅ Start the **Backend** (Spring Boot REST API on `http://localhost:8080`)
2. ✅ Start the **Frontend** (Web Server on `http://localhost:3000`)
3. ✅ Automatically open your browser to the app
4. ✅ Keep both services running

**That's it!** Your Hostel Finder app is ready to use.

---

## Prerequisites (First Time Only)

Before running, make sure you have:

### 1. **Java 17+**
```bash
java -version
```
If not installed, download from [oracle.com/java](https://oracle.com/java)

### 2. **MySQL Server Running**
Make sure MySQL is started and the database exists:
```bash
# Database name: hostel_finder
# Username: root
# Password: 1437
```

If you don't have MySQL set up:
- Download from [mysql.com](https://mysql.com)
- Create database: `CREATE DATABASE hostel_finder;`
- Or update credentials in `backend/src/main/resources/application.properties`

### 3. **Python 3.x** (for serving frontend)
```bash
python --version
```
If not installed, download from [python.org](https://python.org)

---

## Individual Scripts

### Run Backend Only
```bash
run-backend.bat
```
Starts Spring Boot on `http://localhost:8080/api`

### Run Frontend Only
```bash
run-frontend.bat
```
Serves frontend on `http://localhost:3000`

*(Requires backend already running)*

---

## Manual Start (Advanced)

If batch files don't work:

### Terminal 1 - Backend:
```bash
cd backend
mvnw spring-boot:run
```

### Terminal 2 - Frontend:
```bash
cd frontend
python -m http.server 3000
```

### Terminal 3 - Open Browser:
```
http://localhost:3000/pages/index.html
```

---

## Troubleshooting

### ❌ "Port 8080 already in use"
Another application is using port 8080. Options:
- Close the conflicting app
- Kill the Java process: `taskkill /F /IM java.exe`
- Or change port in `backend/src/main/resources/application.properties`:
  ```
  server.port=8081
  ```

### ❌ "Python not found"
Install Python or use Node.js alternative:
```bash
cd frontend
npx http-server -p 3000
```

### ❌ "MySQL connection refused"
Check if MySQL is running:
```bash
# Windows: Start MySQL service
# Or verify credentials in: backend/src/main/resources/application.properties
```

### ❌ "Maven compilation errors"
Clear cache and rebuild:
```bash
cd backend
mvnw clean compile
```

---

## Access the Application

Once running, open your browser:

| Component | URL | Note |
|-----------|-----|------|
| **Main App** | http://localhost:3000/pages/index.html | Public search & listings |
| **Owner Dashboard** | http://localhost:3000/pages/owner-dashboard.html | (Login required) |
| **Admin Portal** | http://localhost:3000/pages/admin-login.html | (Admin login) |
| **API Docs** | http://localhost:8080/api | Backend REST API |

---

## Test Credentials

**Admin Login:**
- Email: `admin@demo.com`
- Password: `Demo@123`

**Owner Login:**
- Email: `owner@demo.com`
- Password: `Demo@123`

**Student Login:**
- Email: `student@demo.com`
- Password: `Demo@123`

---

## Stopping the Application

1. Close the **Backend** terminal window
2. Close the **Frontend** terminal window
3. Close your browser tab

---

## Important Notes

⚠️ **Keep Terminal Windows Open**
- Don't close the terminal windows while using the app
- The servers must keep running

⚠️ **Database Persistence**
- All changes are saved to MySQL database
- Data persists after closing VS Code

⚠️ **First Run**
- Backend may take 5-10 seconds to start (compiling)
- Frontend server starts instantly
- Be patient for the browser to open

---

## Project Structure

```
minor3/
├── run.bat                 ← Main launcher (use this!)
├── run-backend.bat        ← Start backend only
├── run-frontend.bat       ← Start frontend only
├── backend/               ← Spring Boot REST API
│   ├── mvnw.cmd          ← Maven wrapper (runs automatically)
│   └── src/main/
│       ├── java/         ← Java controllers, services
│       └── resources/    ← Database migrations, config
└── frontend/             ← HTML/CSS/JavaScript
    ├── pages/            ← HTML pages
    ├── js/               ← JavaScript logic
    └── css/              ← Stylesheets
```

---

## Next Steps

1. ✅ Run: Double-click `run.bat`
2. ✅ Open `http://localhost:3000`
3. ✅ Login with test credentials
4. ✅ Explore the app!

**Questions?** Check the console output for errors.

Happy Hostel Hunting! 🏠🔍
