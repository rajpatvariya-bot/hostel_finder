import { apiFetch, setToken } from "./apiClient.js";

const els = {
  loginSection: document.getElementById("loginSection"),
  registerSection: document.getElementById("registerSection"),
  toggleLoginBtn: document.getElementById("toggleLoginBtn"),
  toggleRegisterBtn: document.getElementById("toggleRegisterBtn"),
  linkToRegister: document.getElementById("linkToRegister"),
  linkToLogin: document.getElementById("linkToLogin"),
  
  loginEmail: document.getElementById("loginEmail"),
  loginPassword: document.getElementById("loginPassword"),
  loginSubmitBtn: document.getElementById("loginSubmitBtn"),
  
  regName: document.getElementById("regName"),
  regEmail: document.getElementById("regEmail"),
  regPhone: document.getElementById("regPhone"),
  regPassword: document.getElementById("regPassword"),
  regGender: document.getElementById("regGender"),
  regAge: document.getElementById("regAge"),
  registerSubmitBtn: document.getElementById("registerSubmitBtn"),
  
  msg: document.getElementById("msg"),
};

function showMessage(text, isError = true) {
  els.msg.style.display = "block";
  els.msg.innerHTML = `<strong>${isError ? 'Error:' : 'Message:'}</strong> ${escapeHtml(text)}`;
  els.msg.style.borderColor = isError ? "#f8d7da" : "#d4edda";
  els.msg.style.backgroundColor = isError ? "#f8d7da" : "#d4edda";
  els.msg.style.color = isError ? "#721c24" : "#155724";
}

function clearMessage() {
  els.msg.style.display = "none";
  els.msg.innerHTML = "";
}

function escapeHtml(s) {
  return String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function showLogin(e) {
  if(e) e.preventDefault();
  els.loginSection.style.display = "block";
  els.registerSection.style.display = "none";
  els.toggleLoginBtn.classList.add("btn-primary");
  els.toggleRegisterBtn.classList.remove("btn-primary");
  clearMessage();
}

function showRegister(e) {
  if(e) e.preventDefault();
  els.loginSection.style.display = "none";
  els.registerSection.style.display = "block";
  els.toggleRegisterBtn.classList.add("btn-primary");
  els.toggleLoginBtn.classList.remove("btn-primary");
  clearMessage();
}

els.toggleLoginBtn?.addEventListener("click", showLogin);
els.toggleRegisterBtn?.addEventListener("click", showRegister);
els.linkToLogin?.addEventListener("click", showLogin);
els.linkToRegister?.addEventListener("click", showRegister);

async function performLogin() {
  els.loginSubmitBtn.disabled = true;
  els.loginSubmitBtn.textContent = "Logging in...";
  clearMessage();
  try {
    const data = await apiFetch("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: els.loginEmail.value.trim(),
        password: els.loginPassword.value,
      }),
    });
    setToken(data.accessToken);
    localStorage.setItem("userRole", data.role);

    if (data.role === "ADMIN") window.location.href = "./admin-dashboard.html";
    else if (data.role === "OWNER") window.location.href = "./owner-dashboard.html";
    else if (data.role === "STUDENT") window.location.href = "./student-dashboard.html";
    else window.location.href = "./index.html";
  } catch (e) {
    showMessage(e.message);
  } finally {
    els.loginSubmitBtn.disabled = false;
    els.loginSubmitBtn.textContent = "Login";
  }
}

async function performRegister() {
  const payload = {
    name: els.regName.value,
    email: els.regEmail.value,
    phone: els.regPhone.value,
    password: els.regPassword.value,
    gender: els.regGender.value,
    age: parseInt(els.regAge.value) || 0,
  };

  if (!payload.name || !payload.email || !payload.phone || !payload.password) {
    showMessage("Please fill all required fields");
    return;
  }

  els.registerSubmitBtn.disabled = true;
  els.registerSubmitBtn.textContent = "Registering...";
  clearMessage();

  try {
    const data = await apiFetch("/auth/register/student", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    setToken(data.accessToken);
    localStorage.setItem("userRole", "STUDENT");
    showMessage("Registration successful! Redirecting...", false);
    
    setTimeout(() => {
      window.location.href = "./student-dashboard.html";
    }, 1500);
  } catch (e) {
    showMessage(`Registration failed: ${e.message}`);
  } finally {
    els.registerSubmitBtn.disabled = false;
    els.registerSubmitBtn.textContent = "Register";
  }
}

els.loginSubmitBtn?.addEventListener("click", performLogin);
els.registerSubmitBtn?.addEventListener("click", performRegister);
