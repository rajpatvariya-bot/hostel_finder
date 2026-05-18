# Quick Launch Guide

Choose the easiest method for your system:

## 🚀 **RECOMMENDED: Main Launcher**

### **Option 1: Batch File (Easiest)**
Double-click: **`run.bat`**

This will automatically:
- ✅ Start Backend (Spring Boot)
- ✅ Start Frontend (HTTP Server)  
- ✅ Open Browser to the app

**Keep the 2 terminal windows open!**

---

### **Option 2: VBS Launcher**
Double-click: **`Launch-App.vbs`**

Same as above, but with a GUI prompt.

---

### **Option 3: PowerShell**
Right-click and select "Run with PowerShell": **`run-powershell.ps1`**

More advanced, with better error handling and colored output.

---

## 🛠️ **Manual Start (If needed)**

### **Terminal 1 - Backend:**
```bash
cd backend
mvnw spring-boot:run
```

### **Terminal 2 - Frontend:**
```bash
cd frontend
python -m http.server 3000
```

### **Browser:**
```
http://localhost:3000
```

---

## ⚙️ **Individual Launchers**

### **Start Only Backend:**
Run: **`run-backend.bat`**

### **Start Only Frontend:**
Run: **`run-frontend.bat`**

### **Check Requirements:**
Run: **`check-requirements.bat`**

---

## 📋 **Requirements**

✓ Java 17+
✓ MySQL running (with hostel_finder database)
✓ Python 3.x (for frontend server)

---

## ✅ **Test Accounts**

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@demo.com | Demo@123 |
| Owner | owner@demo.com | Demo@123 |
| Student | student@demo.com | Demo@123 |

---

## 🔗 **Access Points**

| What | URL |
|------|-----|
| Main App | http://localhost:3000 |
| Owner Dashboard | http://localhost:3000/pages/owner-dashboard.html |
| Admin Portal | http://localhost:3000/pages/admin-login.html |
| API | http://localhost:8080/api |

---

## ❌ **Troubleshooting**

**"Port 8080 in use"**
→ Close other apps or change port in `backend/src/main/resources/application.properties`

**"Python not found"**
→ Install from https://python.org

**"MySQL error"**
→ Start MySQL service, verify credentials in `backend/src/main/resources/application.properties`

**"Maven compilation failed"**
→ Run: `cd backend && mvnw clean compile`

---

## 📖 **For More Details**

See: **`SETUP_AND_RUN.md`** for comprehensive setup guide

---

**Happy Hostel Hunting!** 🏠🔍

Just double-click `run.bat` and you're good to go! 🎉
