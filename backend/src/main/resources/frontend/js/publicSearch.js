import { apiFetch } from "./apiClient.js";

const CITY_ID = 1; // Seeded as "Indore" in backend migration

const els = {
  areaSelect: document.getElementById("areaSelect"),
  genderSelect: document.getElementById("genderSelect"),
  minPrice: document.getElementById("minPrice"),
  maxPrice: document.getElementById("maxPrice"),
  availableOnly: document.getElementById("availableOnly"),
  facilityChips: document.getElementById("facilityChips"),
  searchBtn: document.getElementById("searchBtn"),
  hostelList: document.getElementById("hostelList"),
};

let facilities = [];
let selectedFacilityIds = new Set();

function renderFacilities() {
  els.facilityChips.innerHTML = "";
  facilities.forEach((f) => {
    const label = document.createElement("label");
    label.className = "chip";
    label.innerHTML = `<input type="checkbox" data-id="${f.id}"> ${f.name}`;
    label.querySelector("input").addEventListener("change", (e) => {
      const id = Number(e.target.getAttribute("data-id"));
      if (e.target.checked) selectedFacilityIds.add(id);
      else selectedFacilityIds.delete(id);
    });
    els.facilityChips.appendChild(label);
  });
}

function renderHostels(items) {
  if (!items || items.length === 0) {
    els.hostelList.innerHTML = `<div class="notice"><strong>No results</strong> Try changing filters.</div>`;
    return;
  }

  els.hostelList.innerHTML = "";
  items.forEach((h) => {
    const card = document.createElement("a");
    card.className = "hostel-card";
    card.href = `./hostel-details.html?id=${h.id}`;
    const mainImage = h.imageUrl ? h.imageUrl : `../img/hostels/room${(h.id % 2) + 1}.png`;
    card.innerHTML = `
      <div class="hostel-thumb" style="background-image: url('${mainImage}'); background-size: cover; background-position: center;"></div>
      <div class="hostel-meta">
        <div class="row" style="display:flex; justify-content:space-between; align-items:center;">
          <div style="font-weight:700; font-size:16px;">${escapeHtml(h.hostelName)}</div>
          <span class="badge badge-primary" style="background:#5c2d91; color:white; padding:2px 8px; border-radius:4px;">₹${Number(h.pricePerMonth).toFixed(0)}</span>
        </div>
        <div class="row muted" style="font-size:14px; margin:5px 0;">
          <span>${escapeHtml(h.areaName)}, ${escapeHtml(h.cityName)}</span>
          <span class="badge badge-ok" style="color:green;">${h.availableRooms} Left</span>
        </div>
        <div class="row" style="display:flex; gap:5px; margin-top:8px; flex-wrap:wrap;">
          <span class="badge">${escapeHtml(h.genderType)}</span>
          <span class="badge">${h.messAvailable ? "Mess" : "No"}</span>
          ${h.facilities ? h.facilities.map(f => `<span class="facility-tag">${escapeHtml(f)}</span>`).join('') : ''}
        </div>
      </div>
    `;
    els.hostelList.appendChild(card);
  });
}

function escapeHtml(s) {
  return String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function buildQuery() {
  const params = new URLSearchParams();
  params.set("cityId", String(CITY_ID));

  if (els.areaSelect.value) params.set("areaId", els.areaSelect.value);
  if (els.genderSelect.value) params.set("genderType", els.genderSelect.value);
  if (els.minPrice.value) params.set("minPrice", els.minPrice.value);
  if (els.maxPrice.value) params.set("maxPrice", els.maxPrice.value);
  if (els.availableOnly.checked) params.set("availableOnly", "true");
  if (selectedFacilityIds.size > 0) params.set("facilityIds", Array.from(selectedFacilityIds).join(","));

  params.set("page", "0");
  params.set("size", "10");
  return params.toString();
}

async function loadFilters() {
  try {
    const [areas, facs] = await Promise.all([
      apiFetch(`/public/areas?cityId=${CITY_ID}`),
      apiFetch(`/public/facilities`),
    ]);

    areas.forEach((a) => {
      const opt = document.createElement("option");
      opt.value = a.id;
      opt.textContent = a.name;
      els.areaSelect.appendChild(opt);
    });

    facilities = facs;
    renderFacilities();
  } catch (err) {
    console.error("Failed to load filters:", err);
    // Show error but don't block the page
    els.facilityChips.innerHTML = `<span style="color:red">Error loading filters: ${escapeHtml(err.message)}</span>`;
  }
}

async function search() {
  els.searchBtn.disabled = true;
  els.searchBtn.textContent = "Loading...";
  els.hostelList.innerHTML = `<div class="notice">Loading hostels...</div>`;
  
  try {
    const q = buildQuery();
    const result = await apiFetch(`/public/hostels?${q}`);
    
    if (!result || !result.items) {
      renderHostels([]);
      return;
    }
    
    renderHostels(result.items);
  } catch (e) {
    console.error("Search error:", e);
    els.hostelList.innerHTML = `
      <div class="notice" style="color: #e74c3c;">
        <strong>⚠️ Cannot Connect to Backend</strong><br>
        <small style="color: #95a5a6;">
          Error: ${escapeHtml(e.message)}<br><br>
          💡 Try refreshing the page (F5) or wait a moment for the backend to fully start.<br>
          ☝️ Make sure both terminal windows are still open.
        </small>
      </div>
    `;
  } finally {
    els.searchBtn.disabled = false;
    els.searchBtn.textContent = "Search";
  }
}

els.searchBtn.addEventListener("click", search);

function updateHeader() {
  const token = localStorage.getItem("accessToken");
  const role = localStorage.getItem("userRole");
  const nav = document.querySelector(".nav");
  if (token && role && nav) {
    let dashboardUrl = "./login.html";
    if (role === "ADMIN") dashboardUrl = "./admin-dashboard.html";
    else if (role === "OWNER") dashboardUrl = "./owner-dashboard.html";
    else if (role === "STUDENT") dashboardUrl = "./student-dashboard.html";
    
    nav.innerHTML = `
      <a class="btn" href="${dashboardUrl}">Go to Dashboard</a>
      <button class="btn" onclick="localStorage.clear(); location.reload();">Logout</button>
    `;
  }
}

// Initialize page with better error handling
updateHeader();

// Show loading message while fetching
els.hostelList.innerHTML = `<div class="notice">Starting up... Loading hostels...</div>`;

(async () => {
  try {
    await loadFilters();
    await search();
  } catch (err) {
    console.error("Initialization error:", err);
  }
})();

