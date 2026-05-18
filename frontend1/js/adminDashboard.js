import { apiFetch } from "./apiClient.js";

const ownersList = document.getElementById("ownersList");

async function init() {
  loadPendingVerifications();
}

async function loadPendingVerifications() {
  try {
    const verifications = await apiFetch("/admin/verifications");
    const pending = verifications.filter(v => v.status === "UPLOADED");
    renderVerifications(pending);
  } catch (err) {
    if (ownersList) ownersList.innerHTML = `<p style="color:red">Error: ${err.message}</p>`;
  }
}

function renderVerifications(verifications) {
  if (!ownersList) return;
  if (verifications.length === 0) {
    ownersList.innerHTML = `<p>No pending Aadhaar verifications.</p>`;
    return;
  }

  ownersList.innerHTML = `
    <table style="width:100%; border-collapse:collapse; margin-top:10px;">
      <thead>
        <tr style="border-bottom:2px solid #eee; text-align:left;">
          <th style="padding:10px;">Owner Name</th>
          <th style="padding:10px;">Aadhaar Document</th>
          <th style="padding:10px;">Submitted</th>
          <th style="padding:10px;">Action</th>
        </tr>
      </thead>
      <tbody>
        ${verifications.map(v => `
          <tr style="border-bottom:1px solid #eee;">
            <td style="padding:10px;">${escapeHtml(v.ownerName)}</td>
            <td style="padding:10px;">
                <a href="${v.fileUrl}" target="_blank" style="color:var(--primary); font-weight:bold;">View Aadhaar</a>
            </td>
            <td style="padding:10px;">${new Date(v.submittedAt).toLocaleDateString()}</td>
            <td style="padding:10px;">
              <button onclick="updateVerification(${v.documentId}, 'APPROVE')" style="background:#28a745; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer;">Approve</button>
              <button onclick="updateVerification(${v.documentId}, 'REJECT')" style="background:#dc3545; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer;">Reject</button>
            </td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function escapeHtml(s) {
  return String(s ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

window.updateVerification = async (docId, action) => {
  try {
    let note = "";
    if (action === "REJECT") {
        note = prompt("Reason for rejection:");
        if (note === null) return;
    }
    await apiFetch(`/admin/verifications/${docId}`, { 
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action, note })
    });
    alert(`Verification ${action.toLowerCase()}d!`);
    loadPendingVerifications();
  } catch (err) {
    alert("Failed: " + err.message);
  }
};

init();
