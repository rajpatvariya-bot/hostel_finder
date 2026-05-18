import { apiFetch, setToken } from "./apiClient.js";

const nameEl = document.getElementById("name");
const emailEl = document.getElementById("email");
const phoneEl = document.getElementById("phone");
const passEl = document.getElementById("password");
const btn = document.getElementById("regBtn");
const msg = document.getElementById("msg");

function showMessage(text, isError = true) {
  msg.style.display = "block";
  msg.innerHTML = isError ? `<strong>Error:</strong> ${escapeHtml(text)}` : text;
  msg.style.borderColor = isError ? "#f8d7da" : "#d1e7dd";
  msg.style.backgroundColor = isError ? "#f8d7da" : "#d1e7dd";
  msg.style.color = isError ? "#721c24" : "#0f5132";
}

function escapeHtml(s) {
  return String(s ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

async function register() {
  if (!nameEl || !emailEl || !phoneEl || !passEl) {
    console.error("Registration fields not found in the DOM.");
    return;
  }

  const payload = {
    name: nameEl.value.trim(),
    email: emailEl.value.trim(),
    phone: phoneEl.value.trim(),
    password: passEl.value,
  };

  if(!payload.name || !payload.email || !payload.phone || !payload.password) {
    showMessage("Please fill in all fields.");
    return;
  }

  btn.disabled = true;
  btn.textContent = "Registering...";
  msg.style.display = "none";

  console.log("Sending owner registration:", payload);

  try {
    const data = await apiFetch("/auth/register/owner", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    console.log("Registration response:", data);
    setToken(data.accessToken);
    localStorage.setItem("userRole", data.role || "OWNER");
    
    showMessage("Registration successful! Redirecting to dashboard...", false);
    setTimeout(() => {
      window.location.href = "./owner-dashboard.html";
    }, 1200);
  } catch (e) {
    console.error("Registration error:", e);
    showMessage(e.message || "Registration failed. Please try again.");
    btn.disabled = false;
    btn.textContent = "Register as Owner";
  }
}

btn?.addEventListener("click", register);
