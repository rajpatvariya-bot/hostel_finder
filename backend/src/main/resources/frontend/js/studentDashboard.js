import { apiFetch, clearToken } from "./apiClient.js";

const inquiryList = document.getElementById("inquiryList");

document.getElementById("logoutBtn").addEventListener("click", () => {
  clearToken();
  window.location.href = "./index.html";
});

function escapeHtml(s) {
  return String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

async function deleteInquiry(id) {
  const confirmed = window.confirm("Delete this inquiry? This cannot be undone.");
  if (!confirmed) return;

  try {
    await apiFetch(`/student/inquiries/${id}`, { method: "DELETE" });
    await loadInquiries();
  } catch (error) {
    window.alert(`Failed to delete inquiry: ${error.message}`);
  }
}

async function loadInquiries() {
  try {
    const list = await apiFetch("/student/inquiries", { method: "GET" });
    if (!list || list.length === 0) {
      inquiryList.innerHTML = `<div class="notice">No inquiries found. Go search for a hostel!</div>`;
      return;
    }

    inquiryList.innerHTML = "";
    list.forEach(i => {
      const el = document.createElement("div");
      el.className = "card";
      el.innerHTML = `
        <div class="card-body">
          <div style="font-weight:bold; font-size:1.1rem;">${escapeHtml(i.hostelName)} - ${escapeHtml(i.areaName)}</div>
          <div style="margin-top:8px;">
            Status: <span class="badge ${i.status === 'PENDING' ? 'badge-primary' : (i.status === 'ACCEPTED' ? 'badge-ok' : '')}">${escapeHtml(i.status)}</span>
          </div>
          <div style="margin-top:8px; color:#555;">
            ${i.roomType ? `Room type: ${escapeHtml(i.roomType)} <br>` : ""}
            Rooms requested: ${i.requestedRoomCount} <br>
            Message: ${escapeHtml(i.message)}
          </div>
          <div style="margin-top:14px; display:flex; justify-content:flex-end;">
            <button type="button" class="btn btn-danger" data-delete-inquiry="${i.id}">Delete</button>
          </div>
        </div>
      `;
      el.querySelector("[data-delete-inquiry]")?.addEventListener("click", () => deleteInquiry(i.id));
      inquiryList.appendChild(el);
    });
  } catch (e) {
    inquiryList.innerHTML = `<div class="notice errorMessage">Error loading inquiries: ${escapeHtml(e.message)}</div>`;
  }
}

loadInquiries();
