import { apiFetch, getToken } from "./apiClient.js";
import {
  buildBookingPayload,
  buildPostPaymentState,
  derivePaymentDetails,
  saveDummyPaymentState,
  shouldSubmitBookingAction,
} from "./paymentFlow.mjs";
import { openPaymentModal } from "./components/PaymentModal.js";

const detailsEl = document.getElementById("details");
const sendBtn = document.getElementById("sendInquiryBtn");
const bookBtn = document.getElementById("bookingBtn");
const roomCountEl = document.getElementById("roomCount");
const messageEl = document.getElementById("message");
const inqMsg = document.getElementById("inqMsg");
const paymentModalRoot = document.getElementById("paymentModalRoot");

let currentHostel = null;

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function getHostelId() {
  const id = new URLSearchParams(window.location.search).get("id");
  return id ? Number(id) : null;
}

function renderDetails(hostel) {
  const mainImage = hostel.imageUrls && hostel.imageUrls.length > 0
    ? hostel.imageUrls[0]
    : `../img/hostels/room${(hostel.id % 2) + 1}.png`;

  detailsEl.innerHTML = `
    <div style="width:100%; height:350px; background-image: url('${mainImage}'); background-size: cover; background-position: center; border-radius: 8px 8px 0 0; margin-top: -16px; margin-left: -16px; width: calc(100% + 32px);"></div>
    <div class="card-body">
      <div class="row" style="display:flex; justify-content:space-between; align-items:flex-start;">
        <div>
          <div style="font-size:28px; font-weight:800; margin-top:12px; color:#333;">${escapeHtml(hostel.hostelName)}</div>
          <div class="muted" style="font-size:16px; margin-bottom:15px;">${escapeHtml(hostel.addressLine)} • ${escapeHtml(hostel.areaName)}, ${escapeHtml(hostel.cityName)}</div>
        </div>
        <div class="chips" style="display:flex; gap:10px; flex-wrap:wrap; justify-content:flex-end;">
          <span class="chip" style="background:#5c2d91; color:white; padding:5px 15px; border-radius:20px; font-weight:bold;">Rs. ${Number(hostel.pricePerMonth).toFixed(0)}/mo</span>
          <span class="chip" style="background:#eee; padding:5px 15px; border-radius:20px;">${escapeHtml(hostel.genderType)}</span>
          <span class="chip" style="background:#eee; padding:5px 15px; border-radius:20px;">${hostel.messAvailable ? "Mess available" : "No mess"}</span>
          <span class="chip" style="background:#d4edda; color:#155724; padding:5px 15px; border-radius:20px;"><strong>Available:</strong> ${hostel.availableRooms}</span>
        </div>
      </div>

      <div style="margin-top:20px;">
        <h3 style="border-bottom:1px solid #eee; padding-bottom:10px;">Description</h3>
        <p style="line-height:1.6; color:#555; white-space: pre-line;">${escapeHtml(hostel.description || "No description provided.")}</p>
      </div>

      <div style="margin-top:20px;">
        <h3 style="border-bottom:1px solid #eee; padding-bottom:10px;">Facilities</h3>
        <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top:10px;">
          ${hostel.facilities.length ? hostel.facilities.map((facility) => `<span class="facility-tag" style="font-size:14px; padding:5px 12px;">${escapeHtml(facility)}</span>`).join("") : "—"}
        </div>
      </div>

      ${hostel.imageUrls && hostel.imageUrls.length > 1 ? `
        <div style="margin-top:20px;">
          <h3 style="border-bottom:1px solid #eee; padding-bottom:10px;">More Images</h3>
          <div style="display:grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap:10px; margin-top:10px;">
            ${hostel.imageUrls.slice(1).map((imageUrl) => `
              <img src="${imageUrl}" style="width:100%; height:120px; object-fit:cover; border-radius:4px; cursor:pointer;" onclick="window.open('${imageUrl}', '_blank')">
            `).join("")}
          </div>
        </div>
      ` : ""}
    </div>
  `;
}

async function load() {
  const hostelId = getHostelId();
  if (!hostelId) {
    detailsEl.innerHTML = `<div class="card-body"><div class="notice"><strong>Error:</strong> Missing hostel id.</div></div>`;
    return;
  }

  currentHostel = await apiFetch(`/public/hostels/${hostelId}`);
  renderDetails(currentHostel);
}

async function sendInquiry() {
  inqMsg.style.display = "none";
  if (!getToken()) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Login required:</strong> Please login first, then come back and send inquiry.`;
    return;
  }

  const hostelId = getHostelId();
  sendBtn.disabled = true;
  sendBtn.textContent = "Sending...";
  try {
    await apiFetch(`/student/inquiries`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        hostelId,
        requestedRoomCount: Number(roomCountEl.value || "1"),
        message: messageEl.value || "",
      }),
    });
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Sent!</strong> Owner will respond in their dashboard.`;
    messageEl.value = "";
  } catch (error) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Error:</strong> ${escapeHtml(error.message)}`;
  } finally {
    sendBtn.disabled = false;
    sendBtn.textContent = "Send Inquiry";
  }
}

function wait(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

async function submitBookingAfterPayment(result) {
  if (!shouldSubmitBookingAction(result.action)) {
    return false;
  }

  const paymentDetails = derivePaymentDetails(currentHostel);

  if (result.action === "PAY") {
    // This is a dummy UI-only payment step, so we only persist it in session storage.
    await wait(500);
    const paymentState = buildPostPaymentState(result.action, result.selectedMethod, paymentDetails);
    saveDummyPaymentState(window.sessionStorage, paymentState);
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Payment successful!</strong> Dummy payment completed via ${escapeHtml(result.selectedMethod)}. Sending booking request...`;
  } else {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Payment skipped.</strong> Sending booking request to the owner...`;
  }

  const bookingPayload = buildBookingPayload({
    hostelId: getHostelId(),
    requestedRoomCount: Number(roomCountEl.value || "1"),
    message: messageEl.value || "",
  });

  await apiFetch(`/student/inquiries`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(bookingPayload),
  });

  inqMsg.style.display = "block";
  inqMsg.innerHTML = `<strong>Sent!</strong> Booking request sent to the owner for approval.`;
  messageEl.value = "";
  return true;
}

async function bookNow() {
  inqMsg.style.display = "none";
  if (!getToken()) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Login required:</strong> Please login first, then come back and send inquiry.`;
    return;
  }

  if (!currentHostel) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Error:</strong> Hostel details are still loading. Please try again.`;
    return;
  }

  bookBtn.disabled = true;
  bookBtn.textContent = "Opening payment...";

  try {
    // The real booking request is sent only after the modal resolves to Pay or Skip.
    const paymentDetails = derivePaymentDetails(currentHostel);
    const result = await openPaymentModal({
      mountNode: paymentModalRoot,
      paymentDetails,
    });

    await submitBookingAfterPayment(result);
  } catch (error) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Error:</strong> ${escapeHtml(error.message)}`;
  } finally {
    bookBtn.disabled = false;
    bookBtn.textContent = "Book Now";
  }
}

sendBtn?.addEventListener("click", sendInquiry);
bookBtn?.addEventListener("click", bookNow);

await load();
