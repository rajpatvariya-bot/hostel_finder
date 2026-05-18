import { apiFetch } from "./apiClient.js";

const els = {
  hostelList: document.getElementById("hostelList"),
  inquiryList: document.getElementById("inquiryList"),
  addHostelBtn: document.getElementById("addHostelBtn"),
  hostelModal: document.getElementById("hostelModal"),
  closeModal: document.getElementById("closeModal"),
  hostelForm: document.getElementById("hostelForm"),
  citySelect: document.getElementById("citySelect"),
  areaSelect: document.getElementById("areaSelect"),
  roomTypesContainer: document.getElementById("roomTypesContainer"),
  addRoomTypeBtn: document.getElementById("addRoomTypeBtn"),
  tabHostels: document.getElementById("tabHostels"),
  tabInquiries: document.getElementById("tabInquiries"),
  sectionHostels: document.getElementById("sectionHostels"),
  sectionInquiries: document.getElementById("sectionInquiries"),
  logoutBtn: document.getElementById("logoutBtn"),
  
  verificationWidget: document.getElementById("verificationWidget"),
  verificationStatusText: document.getElementById("verificationStatusText"),
  verificationNoteText: document.getElementById("verificationNoteText"),
  verificationUploadDiv: document.getElementById("verificationUploadDiv"),
  aadhaarFile: document.getElementById("aadhaarFile"),
  uploadAadhaarBtn: document.getElementById("uploadAadhaarBtn"),
};

let editingHostelId = null;

async function init() {
  els.verificationWidget.style.display = "block";
  loadVerificationStatus();
  loadMyHostels();
  loadCities();
  await loadFacilities();
  
  els.tabHostels.onclick = () => showSection('hostels');
  els.tabInquiries.onclick = () => showSection('inquiries');
  
  els.addHostelBtn.onclick = () => {
    editingHostelId = null;
    els.hostelForm.reset();
    document.getElementById("modalTitle").textContent = "Add New Hostel";
    // Reset room types to a single empty row
    renderRoomTypes([]);
    els.hostelModal.style.display = "flex";
  };
  
  els.closeModal.onclick = () => {
    els.hostelModal.style.display = "none";
  };
  
  els.citySelect.onchange = (e) => {
    loadAreas(e.target.value);
  };
  
  els.hostelForm.onsubmit = async (e) => {
    e.preventDefault();
    await saveHostel();
  };

  els.addRoomTypeBtn?.addEventListener('click', () => addRoomTypeRow());

  els.logoutBtn.onclick = () => {
    localStorage.clear();
    window.location.href = "./index.html";
  };
}

function showSection(name) {
  if (name === 'hostels') {
    els.sectionHostels.style.display = "block";
    els.sectionInquiries.style.display = "none";
    els.tabHostels.classList.add("btn-primary");
    els.tabInquiries.classList.remove("btn-primary");
    loadMyHostels();
  } else {
    els.sectionHostels.style.display = "none";
    els.sectionInquiries.style.display = "block";
    els.tabInquiries.classList.add("btn-primary");
    els.tabHostels.classList.remove("btn-primary");
    loadInquiries();
  }
}

async function loadMyHostels() {
  try {
    const hostels = await apiFetch("/owner/hostels");
    renderHostels(hostels);
  } catch (err) {
    els.hostelList.innerHTML = `<p style="color:red">Error: ${err.message}</p>`;
  }
}

async function loadVerificationStatus() {
  try {
    const data = await apiFetch("/verification/status");
    els.verificationUploadDiv.style.display = "none";
    els.verificationNoteText.style.display = "none";

    switch(data.status) {
        case "NO_DOCUMENT_UPLOADED":
            els.verificationStatusText.innerHTML = "<strong>Status:</strong> Not Uploaded. Action required.";
            els.verificationUploadDiv.style.display = "flex";
            break;
        case "UPLOADED":
            els.verificationStatusText.innerHTML = "<strong>Status:</strong> <span style='color:#ffc107; font-weight:bold;'>Verification Pending</span>. Admin is reviewing your Aadhaar.";
            break;
        case "APPROVED":
            els.verificationStatusText.innerHTML = "<strong>Status:</strong> <span style='color:#28a745; font-weight:bold;'>Verified</span> ✅";
            break;
        case "REJECTED":
            els.verificationStatusText.innerHTML = "<strong>Status:</strong> <span style='color:#dc3545; font-weight:bold;'>Rejected</span> ❌";
            els.verificationNoteText.textContent = "Reason: " + (data.note || "Invalid document");
            els.verificationNoteText.style.display = "block";
            els.verificationUploadDiv.style.display = "flex";
            break;
    }
  } catch(e) {
      els.verificationStatusText.innerHTML = `<span style="color:red;">Failed to get verification status</span>`;
  }
}

els.uploadAadhaarBtn?.addEventListener("click", async () => {
    const fileItem = els.aadhaarFile.files[0];
    if (!fileItem) return alert("Please select a file first.");
    
    els.uploadAadhaarBtn.disabled = true;
    els.uploadAadhaarBtn.textContent = "Uploading...";
    
    const formData = new FormData();
    formData.append("file", fileItem);
    
    try {
        const token = localStorage.getItem("accessToken");
        const res = await fetch("/api/verification/upload", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });
        
        if (!res.ok) throw new Error(await res.text());
        
        alert("Aadhaar uploaded successfully!");
        els.aadhaarFile.value = "";
        loadVerificationStatus();
    } catch(e) {
        alert("Upload failed: " + e.message);
    } finally {
        els.uploadAadhaarBtn.disabled = false;
        els.uploadAadhaarBtn.textContent = "Upload Aadhaar";
    }
});

function renderHostels(items) {
  if (!items || items.length === 0) {
    els.hostelList.innerHTML = `<div class="card" style="grid-column: 1/-1; text-align:center;"><div class="card-body">You haven't added any hostels yet.</div></div>`;
    return;
  }

  const statusMap = {
    'DRAFT': { label: 'Draft - Not Published', color: '#95a5a6' },
    'PENDING_APPROVAL': { label: 'Pending Admin Approval', color: '#ff9800' },
    'PUBLISHED': { label: 'Live on Site', color: '#28dc8c' },
    'BLOCKED': { label: 'Blocked', color: '#dc3545' }
  };

  els.hostelList.innerHTML = "";
  items.forEach((h) => {
    const s = statusMap[h.status] || { label: h.status, color: '#999' };
    const mainImage = h.imageUrls && h.imageUrls[0] ? h.imageUrls[0] : `../img/hostels/room${(h.id % 2) + 1}.png`;
    const card = document.createElement("div");
    card.className = "card";
    
    // Show different buttons based on status
    let actionButtons = `
      <button class="btn btn-sm btn-primary" onclick="editHostel(${h.id})" style="flex:1;">Edit All</button>
      <button class="btn btn-sm" onclick="confirmDeleteHostel(${h.id})" style="flex:1; color:#dc3545; border-color:#dc3545;">Delete</button>
    `;
    
    if (h.status === 'DRAFT') {
      actionButtons = `
        <button class="btn btn-sm btn-primary" onclick="publishHostel(${h.id})" style="flex:1; background:#27ae60;">Publish</button>
        <button class="btn btn-sm" onclick="editHostel(${h.id})" style="flex:1;">Edit</button>
        <button class="btn btn-sm" onclick="confirmDeleteHostel(${h.id})" style="flex:1; color:#dc3545; border-color:#dc3545;">Delete</button>
      `;
    }
    
    card.innerHTML = `
      <div class="hostel-thumb" style="height:140px; background-image: url('${mainImage}'); background-size:cover; background-position:center; position:relative; border-radius:12px 12px 0 0;">
        <span style="position:absolute; top:10px; right:10px; background:${s.color}; color:white; padding:2px 8px; border-radius:4px; font-size:10px; font-weight:bold;">${s.label}</span>
      </div>
      <div class="card-body">
        <h3 style="margin:0 0 10px 0;">${escapeHtml(h.hostelName)}</h3>
        <p style="font-size:14px; opacity:0.8; margin-bottom:12px;">${escapeHtml(h.areaName)}, ${escapeHtml(h.cityName)}</p>
        <div style="display:flex; justify-content:space-between; margin-bottom:15px; font-size:13px;">
          <span><strong>₹${h.pricePerMonth}</strong>/mo</span>
          <span>${h.availableRooms}/${h.totalRooms} Rooms</span>
        </div>
        <div style="display:flex; gap:8px;">
          ${actionButtons}
        </div>
      </div>
    `;
    els.hostelList.appendChild(card);
  });
}

async function loadInquiries() {
  try {
    const inquiries = await apiFetch("/owner/inquiries");
    renderInquiries(inquiries);
  } catch (err) {
    els.inquiryList.innerHTML = `<p style="color:red">Error: ${err.message}</p>`;
  }
}

function renderInquiries(inquiries) {
  if (inquiries.length === 0) {
    els.inquiryList.innerHTML = `<div class="card" style="grid-column: 1/-1; text-align:center;"><div class="card-body">No student inquiries received yet.</div></div>`;
    return;
  }

  els.inquiryList.innerHTML = inquiries.map(i => `
    <div class="card">
      <div class="card-body">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <h4 style="margin:0;">${escapeHtml(i.studentName)}</h4>
          <span class="badge" style="background:${i.status === 'PENDING' ? '#ffc107' : (i.status === 'ACCEPTED' ? '#28a745' : '#dc3545')}; color:#fff; border:none;">${i.status}</span>
        </div>
        <p style="font-size:13px; margin:8px 0;">Hostel: <strong>${escapeHtml(i.hostelName)}</strong></p>
        <p style="font-size:13px; margin:4px 0;">Request Type: <strong>${escapeHtml(formatRequestType(i.requestType))}</strong></p>
        <p style="font-size:13px; margin:4px 0;">Rooms: <strong>${i.requestedRoomCount}</strong></p>
        <p style="font-size:13px; margin:4px 0;">Contact: <strong>${escapeHtml(i.studentPhone)}</strong></p>
        <div style="background:rgba(255,255,255,0.05); padding:10px; border-radius:4px; font-size:13px; margin:12px 0;">
          "${escapeHtml(i.message || 'No message')}"
        </div>
        ${i.status === 'PENDING' ? `
          <div style="display:flex; gap:8px;">
            <button class="btn btn-sm badge-ok" onclick="updateInquiryStatus(${i.id}, 'ACCEPTED')" style="flex:1; color:#fff;">Accept</button>
            <button class="btn btn-sm" onclick="updateInquiryStatus(${i.id}, 'REJECTED')" style="flex:1; background:#dc3545; color:#fff; border:none;">Reject</button>
          </div>
        ` : ''}
      </div>
    </div>
  `).join('');
}

async function updateInquiryStatus(id, status) {
  const note = prompt(`Enter a note for the student (optional) - Status will be ${status}:`, "");
  if (note === null) return;

  try {
    await apiFetch(`/owner/inquiries/${id}/status`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status, ownerNote: note })
    });
    loadInquiries();
  } catch (err) {
    alert("Update failed: " + err.message);
  }
}

async function confirmDeleteHostel(id) {
  if (confirm("Are you sure you want to delete this hostel? This action cannot be undone.")) {
    try {
      await apiFetch(`/owner/hostels/${id}`, { method: "DELETE" });
      loadMyHostels();
    } catch (err) {
      alert("Delete failed: " + err.message);
    }
  }
}

async function loadCities() {
  try {
    const cities = await apiFetch("/public/cities");
    els.citySelect.innerHTML = '<option value="">Select City</option>' + 
      cities.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  } catch (err) {
    console.error("Failed to load cities", err);
  }
}

async function loadFacilities() {
  const container = document.getElementById("facilityCheckboxes");
  try {
    const facilities = await apiFetch("/public/facilities");
    container.innerHTML = facilities.map(f => `
      <div style="display:flex; align-items:center; gap:5px; font-size:13px;">
        <input type="checkbox" class="fac-check" value="${f.id}" id="fac-${f.id}">
        <label for="fac-${f.id}">${f.name}</label>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = `<span style="color:red">Error: ${err.message}</span>`;
  }
}

async function loadAreas(cityId, preSelectAreaId = null) {
  if (!cityId) return;
  try {
    const areas = await apiFetch(`/public/areas?cityId=${cityId}`);
    els.areaSelect.innerHTML = areas.map(a => `<option value="${a.id}" ${a.id == preSelectAreaId ? 'selected':''}>${a.name}</option>`).join('');
  } catch (err) {
    console.error(err);
  }
}

async function saveHostel() {
  const facChecks = document.querySelectorAll(".fac-check:checked");
  const facilityIds = Array.from(facChecks).map(c => parseInt(c.value));

  const payload = {
    hostelName: document.getElementById("hostelName").value,
    cityId: parseInt(document.getElementById("citySelect").value),
    areaId: parseInt(document.getElementById("areaSelect").value),
    addressLine: document.getElementById("addressLine").value,
    description: document.getElementById("description").value,
    imageUrl: document.getElementById("imageUrl").value,
    pricePerMonth: parseFloat(document.getElementById("price").value),
    genderType: document.getElementById("genderType").value,
    messAvailable: document.getElementById("messAvailable").checked,
    totalRooms: parseInt(document.getElementById("totalRooms").value),
    availableRooms: parseInt(document.getElementById("availableRooms").value),
    facilityIds: facilityIds,
    roomTypes: gatherRoomTypes()
  };

  try {
    if (editingHostelId) {
      await apiFetch(`/owner/hostels/${editingHostelId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      await apiFetch("/owner/hostels", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }
    els.hostelModal.style.display = "none";
    els.hostelForm.reset();
    loadMyHostels();
  } catch (err) {
    alert("Failed to save: " + err.message);
  }
}

window.editHostel = async (id) => {
  editingHostelId = id;
  try {
    const h = await apiFetch(`/owner/hostels`); // Re-fetch list or specify endpoint
    const hostel = h.find(x => x.id == id);
    if (!hostel) return;
    
    document.getElementById("modalTitle").textContent = "Edit Hostel";
    document.getElementById("hostelName").value = hostel.hostelName;
    document.getElementById("citySelect").value = 1; // Default Indore
    
    await loadAreas(1, 1); // Default Indore, Area 1
    
    document.getElementById("addressLine").value = hostel.addressLine;
    document.getElementById("description").value = hostel.description || "";
    document.getElementById("imageUrl").value = hostel.imageUrls[0] || "";
    document.getElementById("price").value = hostel.pricePerMonth;
    document.getElementById("genderType").value = hostel.genderType;
    document.getElementById("messAvailable").checked = hostel.messAvailable;
    document.getElementById("totalRooms").value = hostel.totalRooms;
    document.getElementById("availableRooms").value = hostel.availableRooms;
    // Populate room types if provided
    renderRoomTypes(hostel.roomTypes || []);
    
    els.hostelModal.style.display = "flex";
  } catch (err) {
    alert("Failed to load hostel data: " + err.message);
  }
};

function addRoomTypeRow(data = {}) {
  const id = data.id || '';
  const container = document.createElement('div');
  container.className = 'room-type-row';
  container.style.display = 'grid';
  container.style.gridTemplateColumns = '1fr 120px 80px 80px 60px';
  container.style.gap = '8px';

  container.innerHTML = `
    <input type="hidden" class="rt-id" value="${id}">
    <input type="text" class="input rt-name" placeholder="Room Type (e.g. 2 Bed Room)" value="${escapeHtml(data.roomType || '')}">
    <input type="number" class="input rt-price" placeholder="Price" value="${data.pricePerMonth ?? ''}">
    <input type="number" class="input rt-total" placeholder="Total" value="${data.totalRooms ?? ''}">
    <div style="display:flex; gap:6px; align-items:center;"><input type="number" class="input rt-available" placeholder="Avail" value="${data.availableRooms ?? ''}" style="width:60px;"><button type="button" class="btn btn-sm btn-danger rt-remove">Remove</button></div>
  `;

  container.querySelector('.rt-remove').addEventListener('click', () => {
    container.remove();
  });

  els.roomTypesContainer.appendChild(container);
}

function renderRoomTypes(list) {
  els.roomTypesContainer.innerHTML = '';
  if (!list || list.length === 0) {
    addRoomTypeRow();
    return;
  }
  list.forEach(item => {
    // Map backend fields to form fields
    addRoomTypeRow({ id: item.id, roomType: item.roomType, pricePerMonth: item.pricePerMonth, totalRooms: item.totalRooms, availableRooms: item.availableRooms });
  });
}

function gatherRoomTypes() {
  const rows = Array.from(els.roomTypesContainer.querySelectorAll('.room-type-row'));
  return rows.map(r => {
    const idVal = r.querySelector('.rt-id').value;
    return {
      id: idVal ? parseInt(idVal) : null,
      roomType: r.querySelector('.rt-name').value.trim(),
      pricePerMonth: parseFloat(r.querySelector('.rt-price').value) || 0,
      totalRooms: parseInt(r.querySelector('.rt-total').value) || 0,
      availableRooms: parseInt(r.querySelector('.rt-available').value) || 0
    };
  }).filter(rt => rt.roomType && rt.pricePerMonth > 0 && rt.totalRooms > 0);
}

function escapeHtml(s) {
  return String(s ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

function formatRequestType(requestType) {
  return requestType === "BOOKING" ? "Booking" : "Inquiry";
}

window.publishHostel = async (id) => {
  if (confirm("Publish this hostel? It will be visible to all students on the platform.")) {
    try {
      await apiFetch(`/owner/hostels/${id}/publish`, { method: "PATCH" });
      loadMyHostels();
    } catch (err) {
      alert("Failed to publish: " + err.message);
    }
  }
};

window.updateInquiryStatus = updateInquiryStatus;
window.confirmDeleteHostel = confirmDeleteHostel;

init();
