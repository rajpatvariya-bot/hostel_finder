import { apiFetch, getToken } from "./apiClient.js";
import {
  buildBookingPayload,
  buildPostPaymentState,
  derivePaymentDetails,
  saveDummyPaymentState,
} from "./paymentFlow.mjs";

const CITY_ID = 1;
const MIN_PRICE_GAP = 1000;
const DEFAULT_HOSTEL_PLACEHOLDER = "../img/hostels/room1.png";
const INLINE_PAYMENT_METHODS = ["UPI", "CARD", "CASH"];
const DIRECT_ROOM_TYPE_LIMIT = 2;

const els = {
  keywordInput: document.getElementById("keywordInput"),
  keywordSearchBtn: document.getElementById("keywordSearchBtn"),
  areaSelect: document.getElementById("areaSelect"),
  genderSelect: document.getElementById("genderSelect"),
  minPrice: document.getElementById("minPrice"),
  maxPrice: document.getElementById("maxPrice"),
  minPriceValue: document.getElementById("minPriceValue"),
  maxPriceValue: document.getElementById("maxPriceValue"),
  priceSliderFill: document.getElementById("priceSliderFill"),
  availableOnly: document.getElementById("availableOnly"),
  facilityChips: document.getElementById("facilityChips"),
  searchBtn: document.getElementById("searchBtn"),
  hostelList: document.getElementById("hostelList"),
};

let facilities = [];
let selectedFacilityIds = new Set();
let hostelsById = new Map();

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function formatCurrency(value) {
  return `Rs ${Number(value || 0).toLocaleString("en-IN")}`;
}

function syncPriceRange(source) {
  if (!els.minPrice || !els.maxPrice || els.minPrice.type !== "range" || els.maxPrice.type !== "range") {
    return;
  }

  let minValue = Number(els.minPrice.value);
  let maxValue = Number(els.maxPrice.value);

  if (maxValue - minValue < MIN_PRICE_GAP) {
    if (source === "min") {
      minValue = maxValue - MIN_PRICE_GAP;
      els.minPrice.value = String(minValue);
    } else {
      maxValue = minValue + MIN_PRICE_GAP;
      els.maxPrice.value = String(maxValue);
    }
  }

  if (els.minPriceValue) els.minPriceValue.textContent = formatCurrency(minValue);
  if (els.maxPriceValue) els.maxPriceValue.textContent = formatCurrency(maxValue);

  if (els.priceSliderFill) {
    const min = Number(els.minPrice.min);
    const max = Number(els.maxPrice.max);
    const left = ((minValue - min) / (max - min)) * 100;
    const right = ((maxValue - min) / (max - min)) * 100;
    els.priceSliderFill.style.left = `${left}%`;
    els.priceSliderFill.style.width = `${right - left}%`;
  }
}

function renderFacilities() {
  els.facilityChips.innerHTML = "";
  facilities.forEach((facility) => {
    const label = document.createElement("label");
    label.className = "chip";
    label.innerHTML = `<input type="checkbox" data-id="${facility.id}"> ${escapeHtml(facility.name)}`;
    label.querySelector("input")?.addEventListener("change", (event) => {
      const id = Number(event.target.getAttribute("data-id"));
      if (event.target.checked) selectedFacilityIds.add(id);
      else selectedFacilityIds.delete(id);
    });
    els.facilityChips.appendChild(label);
  });
}

function getHostelImages(hostel) {
  if (Array.isArray(hostel.imageUrls) && hostel.imageUrls.length > 0) {
    return hostel.imageUrls;
  }

  return [hostel.coverImageUrl || hostel.imageUrl || `../img/hostels/room${(hostel.id % 2) + 1}.png` || DEFAULT_HOSTEL_PLACEHOLDER];
}

function createCarouselMarkup(images, carouselId, altBase) {
  const slides = images.map((image, index) => `
    <div class="carousel-slide">
      <img class="carousel-image" src="${image}" alt="${altBase} photo ${index + 1}" loading="lazy">
    </div>
  `).join("");

  const controls = images.length > 1 ? `
    <button type="button" class="carousel-arrow carousel-arrow-prev" data-carousel-prev aria-label="Previous image">&#8249;</button>
    <button type="button" class="carousel-arrow carousel-arrow-next" data-carousel-next aria-label="Next image">&#8250;</button>
    <div class="carousel-dots">
      ${images.map((_, index) => `
        <button type="button" class="carousel-dot ${index === 0 ? "is-active" : ""}" data-carousel-dot="${index}" aria-label="Show image ${index + 1}"></button>
      `).join("")}
    </div>
  ` : "";

  return `
    <div class="carousel-shell" data-carousel-root="${carouselId}" data-carousel-count="${images.length}" data-carousel-index="0">
      <div class="carousel-track">${slides}</div>
      ${controls}
    </div>
  `;
}

function createRoomTypeRowMarkup(roomType) {
  const availableRooms = Number(roomType.availableRooms || 0);
  const actionLabel = availableRooms > 0 ? "Book Now" : "Unavailable";

  return `
    <div class="hostel-room-type-row">
      <div class="hostel-room-type-main">
        <strong>${escapeHtml(roomType.roomType)}</strong>
        <div class="hostel-room-type-meta">
          <span>₹${Number(roomType.pricePerMonth || 0).toLocaleString("en-IN")}/mo</span>
          <span>${availableRooms}/${escapeHtml(roomType.totalRooms)} available</span>
        </div>
      </div>
      <button
        type="button"
        class="btn btn-primary hostel-room-type-book-btn"
        data-action="open-inline-booking"
        data-room-type-id="${roomType.id ?? ""}"
        ${availableRooms > 0 ? "" : "disabled"}
      >
        ${actionLabel}
      </button>
    </div>
  `;
}

function createRoomTypeSectionMarkup(hostel) {
  const roomTypes = Array.isArray(hostel.roomTypes) ? hostel.roomTypes : [];
  if (!roomTypes.length) {
    return `
      <div class="hostel-room-types-shell" data-room-type-section>
        <div class="hostel-room-types-header">
          <span>Room Options</span>
        </div>
        <div class="hostel-room-types">
          <p class="hostel-room-types-empty">No room types available right now.</p>
        </div>
      </div>
    `;
  }

  const directRoomTypes = roomTypes.slice(0, DIRECT_ROOM_TYPE_LIMIT);
  const extraRoomTypes = roomTypes.slice(DIRECT_ROOM_TYPE_LIMIT);

  return `
    <div class="hostel-room-types-shell" data-room-type-section>
      <div class="hostel-room-types-header">
        <span>Room Options</span>
        <span>${roomTypes.length} type${roomTypes.length === 1 ? "" : "s"}</span>
      </div>
      <div class="hostel-room-types">
        <div class="hostel-room-types-list">
          ${directRoomTypes.map(createRoomTypeRowMarkup).join("")}
        </div>
        ${extraRoomTypes.length ? `
          <button
            type="button"
            class="hostel-room-types-toggle"
            data-action="toggle-room-types"
            aria-expanded="false"
          >
            View More Rooms (${extraRoomTypes.length})
          </button>
          <div class="hostel-room-types-extra" hidden>
            ${extraRoomTypes.map(createRoomTypeRowMarkup).join("")}
          </div>
        ` : ""}
        <div
          class="hostel-inline-booking-panel"
          hidden
          data-booking-panel
          data-hostel-id="${hostel.id}"
          data-selected-room-type-id=""
          data-selected-method="${INLINE_PAYMENT_METHODS[0]}"
        ></div>
      </div>
    </div>
  `;
}

function renderHostels(items) {
  if (!items || items.length === 0) {
    els.hostelList.innerHTML = `<div class="notice"><strong>No results</strong> Try changing filters.</div>`;
    return;
  }

  hostelsById = new Map(items.map((item) => [String(item.id), item]));
  els.hostelList.innerHTML = "";

  items.forEach((hostel) => {
    const card = document.createElement("article");
    card.className = "hostel-card hostel-card-clickable";
    card.tabIndex = 0;
    card.dataset.detailsUrl = `./hostel-details.html?id=${hostel.id}`;

    const images = getHostelImages(hostel);
    card.innerHTML = `
      <div class="hostel-thumb">
        ${createCarouselMarkup(images, `card-${hostel.id}`, escapeHtml(hostel.hostelName))}
      </div>
      <div class="hostel-meta">
        <div class="row hostel-card-primary" style="display:flex; justify-content:space-between; align-items:center;">
          <div style="font-weight:700; font-size:16px;">${escapeHtml(hostel.hostelName)}</div>
          <span class="badge badge-primary" style="background:#5c2d91; color:white; padding:2px 8px; border-radius:4px;">₹${Number(hostel.pricePerMonth || 0).toFixed(0)}</span>
        </div>
        <div class="row muted hostel-card-location" style="font-size:14px; margin:5px 0;">
          <span>${escapeHtml(hostel.areaName)}, ${escapeHtml(hostel.cityName)}</span>
          <span class="badge badge-ok" style="color:green;">${hostel.availableRooms} Left</span>
        </div>
        <div class="row" style="display:flex; gap:5px; margin-top:8px; flex-wrap:wrap;">
          <span class="badge">${escapeHtml(hostel.genderType)}</span>
          <span class="badge">${hostel.messAvailable ? "Mess" : "No"}</span>
          ${hostel.facilities ? hostel.facilities.map((facility) => `<span class="facility-tag">${escapeHtml(facility)}</span>`).join("") : ""}
        </div>
        ${createRoomTypeSectionMarkup(hostel)}
      </div>
    `;

    els.hostelList.appendChild(card);
  });

  initCarousels(els.hostelList);
}

function buildInlineBookingPanelMarkup(hostel, roomType, selectedMethod, status = { tone: "", message: "" }) {
  const paymentDetails = derivePaymentDetails(hostel, roomType.id);
  const paymentMethodOptions = INLINE_PAYMENT_METHODS.map((method) => `
    <button
      type="button"
      class="inline-payment-method ${method === selectedMethod ? "is-selected" : ""}"
      data-action="select-payment-method"
      data-method="${method}"
    >
      ${escapeHtml(method)}
    </button>
  `).join("");

  return `
    <div class="hostel-inline-booking-header">
      <div>
        <strong>${escapeHtml(roomType.roomType)}</strong>
        <div class="hostel-inline-booking-subtitle">${formatCurrency(paymentDetails.amount)} per month • ${escapeHtml(roomType.availableRooms)} room(s) left</div>
      </div>
      <button type="button" class="hostel-inline-booking-close" data-action="cancel-inline-booking" aria-label="Close booking options">×</button>
    </div>
    <div class="inline-payment-methods">${paymentMethodOptions}</div>
    <div class="inline-booking-actions">
      <button type="button" class="btn btn-primary" data-action="submit-inline-booking" data-booking-mode="PAY">Pay & Book</button>
      <button type="button" class="btn" data-action="submit-inline-booking" data-booking-mode="SKIP">Skip Payment</button>
    </div>
    ${status.message ? `<div class="inline-booking-status ${status.tone ? `is-${status.tone}` : ""}">${status.message}</div>` : ""}
  `;
}

function renderInlineBookingPanel(panel, hostel, roomTypeId, status = { tone: "", message: "" }) {
  const roomType = Array.isArray(hostel.roomTypes)
    ? hostel.roomTypes.find((item) => String(item.id) === String(roomTypeId))
    : null;
  if (!roomType) {
    panel.hidden = true;
    panel.innerHTML = "";
    return;
  }

  const selectedMethod = panel.dataset.selectedMethod || INLINE_PAYMENT_METHODS[0];
  panel.dataset.selectedRoomTypeId = String(roomType.id ?? "");
  panel.dataset.selectedMethod = selectedMethod;
  panel.hidden = false;
  panel.innerHTML = buildInlineBookingPanelMarkup(hostel, roomType, selectedMethod, status);
}

function closeInlineBookingPanel(panel) {
  panel.hidden = true;
  panel.innerHTML = "";
  panel.dataset.selectedRoomTypeId = "";
  panel.dataset.selectedMethod = INLINE_PAYMENT_METHODS[0];
}

function navigateToDetails(url) {
  if (!url) return;
  window.location.href = url;
}

async function submitInlineBooking(panel, mode, buttonEl) {
  const hostel = hostelsById.get(panel.dataset.hostelId);
  const roomTypeId = panel.dataset.selectedRoomTypeId;
  const roomType = Array.isArray(hostel?.roomTypes)
    ? hostel.roomTypes.find((item) => String(item.id) === String(roomTypeId))
    : null;

  if (!hostel || !roomType) {
    renderInlineBookingPanel(panel, hostel || { roomTypes: [] }, roomTypeId, {
      tone: "error",
      message: "Selected room type is no longer available. Please refresh and try again.",
    });
    return;
  }

  if (!getToken()) {
    renderInlineBookingPanel(panel, hostel, roomType.id, {
      tone: "error",
      message: "Login with a student account to continue booking.",
    });
    return;
  }

  const role = localStorage.getItem("userRole");
  if (role && role !== "STUDENT") {
    renderInlineBookingPanel(panel, hostel, roomType.id, {
      tone: "error",
      message: `Only student accounts can book rooms. Current role: ${escapeHtml(role)}.`,
    });
    return;
  }

  const idleLabel = buttonEl.textContent;
  buttonEl.disabled = true;
  buttonEl.textContent = mode === "PAY" ? "Processing..." : "Sending...";

  try {
    const paymentDetails = derivePaymentDetails(hostel, roomType.id);
    const selectedMethod = panel.dataset.selectedMethod || INLINE_PAYMENT_METHODS[0];

    if (mode === "PAY") {
      const paymentState = buildPostPaymentState("PAY", selectedMethod, paymentDetails);
      saveDummyPaymentState(window.sessionStorage, paymentState);
    }

    await apiFetch("/student/inquiries", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(buildBookingPayload({
        hostelId: hostel.id,
        roomTypeId: roomType.id ?? null,
        requestedRoomCount: 1,
        message: "",
      })),
    });

    renderInlineBookingPanel(panel, hostel, roomType.id, {
      tone: "success",
      message: mode === "PAY"
        ? `Payment recorded and booking request sent for ${escapeHtml(roomType.roomType)}.`
        : `Booking request sent for ${escapeHtml(roomType.roomType)}.`,
    });
  } catch (error) {
    renderInlineBookingPanel(panel, hostel, roomType.id, {
      tone: "error",
      message: escapeHtml(error.message),
    });
  } finally {
    buttonEl.disabled = false;
    buttonEl.textContent = idleLabel;
  }
}

function handleHostelListClick(event) {
  const toggleButton = event.target.closest('[data-action="toggle-room-types"]');
  if (toggleButton) {
    const extra = toggleButton.parentElement?.querySelector(".hostel-room-types-extra");
    if (!extra) return;
    const isOpen = toggleButton.getAttribute("aria-expanded") === "true";
    extra.hidden = isOpen;
    toggleButton.setAttribute("aria-expanded", String(!isOpen));
    toggleButton.textContent = isOpen
      ? `View More Rooms (${extra.children.length})`
      : "Show Fewer Rooms";
    return;
  }

  const openBookingButton = event.target.closest('[data-action="open-inline-booking"]');
  if (openBookingButton) {
    const card = openBookingButton.closest(".hostel-card");
    const panel = card?.querySelector("[data-booking-panel]");
    const hostel = hostelsById.get(card?.querySelector("[data-booking-panel]")?.dataset.hostelId || "");
    const roomTypeId = openBookingButton.dataset.roomTypeId;
    if (panel && hostel) {
      renderInlineBookingPanel(panel, hostel, roomTypeId);
    }
    return;
  }

  const selectMethodButton = event.target.closest('[data-action="select-payment-method"]');
  if (selectMethodButton) {
    const panel = selectMethodButton.closest("[data-booking-panel]");
    const hostel = hostelsById.get(panel?.dataset.hostelId || "");
    if (!panel || !hostel) return;
    panel.dataset.selectedMethod = selectMethodButton.dataset.method || INLINE_PAYMENT_METHODS[0];
    renderInlineBookingPanel(panel, hostel, panel.dataset.selectedRoomTypeId);
    return;
  }

  const cancelButton = event.target.closest('[data-action="cancel-inline-booking"]');
  if (cancelButton) {
    const panel = cancelButton.closest("[data-booking-panel]");
    if (panel) {
      closeInlineBookingPanel(panel);
    }
    return;
  }

  const submitButton = event.target.closest('[data-action="submit-inline-booking"]');
  if (submitButton) {
    const panel = submitButton.closest("[data-booking-panel]");
    if (!panel) return;
    submitInlineBooking(panel, submitButton.dataset.bookingMode || "SKIP", submitButton);
    return;
  }

  const card = event.target.closest(".hostel-card[data-details-url]");
  if (!card) return;

  if (event.target.closest("button, input, label, [data-room-type-section], [data-booking-panel], [data-carousel-root]")) {
    return;
  }

  navigateToDetails(card.dataset.detailsUrl);
}

function handleHostelListKeydown(event) {
  const card = event.target.closest(".hostel-card[data-details-url]");
  if (!card || event.target !== card) return;
  if (event.key !== "Enter" && event.key !== " ") return;
  event.preventDefault();
  navigateToDetails(card.dataset.detailsUrl);
}

function buildQuery() {
  const params = new URLSearchParams();
  params.set("cityId", String(CITY_ID));

  const keyword = els.keywordInput?.value?.trim();
  if (keyword) params.set("keyword", keyword);
  if (els.areaSelect?.value) params.set("areaId", els.areaSelect.value);
  if (els.genderSelect?.value) params.set("genderType", els.genderSelect.value);
  if (els.minPrice?.value) params.set("minPrice", els.minPrice.value);
  if (els.maxPrice?.value) params.set("maxPrice", els.maxPrice.value);
  if (els.availableOnly?.checked) params.set("availableOnly", "true");
  if (selectedFacilityIds.size > 0) params.set("facilityIds", Array.from(selectedFacilityIds).join(","));

  params.set("page", "0");
  params.set("size", "10");
  return params.toString();
}

async function loadFilters() {
  try {
    const [areas, facs] = await Promise.all([
      apiFetch(`/public/areas?cityId=${CITY_ID}`),
      apiFetch("/public/facilities"),
    ]);

    areas.forEach((area) => {
      const option = document.createElement("option");
      option.value = area.id;
      option.textContent = area.name;
      els.areaSelect?.appendChild(option);
    });

    facilities = facs;
    renderFacilities();
  } catch (error) {
    console.error("Failed to load filters:", error);
    if (els.facilityChips) {
      els.facilityChips.innerHTML = `<span style="color:red">Error loading filters: ${escapeHtml(error.message)}</span>`;
    }
  }
}

async function search() {
  els.searchBtn.disabled = true;
  if (els.keywordSearchBtn) els.keywordSearchBtn.disabled = true;
  els.searchBtn.textContent = "Loading...";
  if (els.keywordSearchBtn) els.keywordSearchBtn.textContent = "Searching...";
  els.hostelList.innerHTML = `<div class="notice">Loading hostels...</div>`;

  try {
    const result = await apiFetch(`/public/hostels?${buildQuery()}`);
    renderHostels(result?.items || []);
  } catch (error) {
    console.error("Search error:", error);
    els.hostelList.innerHTML = `
      <div class="notice" style="color: #e74c3c;">
        <strong>Cannot Connect to Backend</strong><br>
        <small style="color: #95a5a6;">
          Error: ${escapeHtml(error.message)}<br><br>
          Try refreshing the page or wait a moment for the backend to fully start.
        </small>
      </div>
    `;
  } finally {
    els.searchBtn.disabled = false;
    if (els.keywordSearchBtn) els.keywordSearchBtn.disabled = false;
    els.searchBtn.textContent = "Search";
    if (els.keywordSearchBtn) els.keywordSearchBtn.textContent = "Search";
  }
}

function initCarousels(scope) {
  scope.querySelectorAll("[data-carousel-root]").forEach((root) => {
    if (root.dataset.bound === "true") return;
    root.dataset.bound = "true";

    const track = root.querySelector(".carousel-track");
    const dots = Array.from(root.querySelectorAll("[data-carousel-dot]"));
    const total = Number(root.dataset.carouselCount || "1");
    let touchStartX = 0;

    const update = (index) => {
      const normalized = ((index % total) + total) % total;
      root.dataset.carouselIndex = String(normalized);
      if (track) track.style.transform = `translateX(-${normalized * 100}%)`;
      dots.forEach((dot, dotIndex) => {
        dot.classList.toggle("is-active", dotIndex === normalized);
      });
    };

    root.querySelector("[data-carousel-prev]")?.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
      update(Number(root.dataset.carouselIndex || "0") - 1);
    });

    root.querySelector("[data-carousel-next]")?.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
      update(Number(root.dataset.carouselIndex || "0") + 1);
    });

    dots.forEach((dot) => {
      dot.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();
        update(Number(dot.dataset.carouselDot || "0"));
      });
    });

    root.addEventListener("touchstart", (event) => {
      touchStartX = event.changedTouches[0]?.clientX || 0;
    }, { passive: true });

    root.addEventListener("touchend", (event) => {
      const touchEndX = event.changedTouches[0]?.clientX || 0;
      const delta = touchEndX - touchStartX;
      if (Math.abs(delta) < 30) return;
      update(Number(root.dataset.carouselIndex || "0") + (delta < 0 ? 1 : -1));
    }, { passive: true });
  });
}

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

els.searchBtn?.addEventListener("click", search);
els.keywordSearchBtn?.addEventListener("click", search);
els.keywordInput?.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    event.preventDefault();
    search();
  }
});
els.minPrice?.addEventListener("input", () => syncPriceRange("min"));
els.maxPrice?.addEventListener("input", () => syncPriceRange("max"));
els.hostelList?.addEventListener("click", handleHostelListClick);
els.hostelList?.addEventListener("keydown", handleHostelListKeydown);

updateHeader();
syncPriceRange("max");
els.hostelList.innerHTML = `<div class="notice">Starting up... Loading hostels...</div>`;

(async () => {
  try {
    await loadFilters();
    await search();
  } catch (error) {
    console.error("Initialization error:", error);
  }
})();
