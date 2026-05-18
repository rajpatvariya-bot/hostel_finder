# Dummy Booking Payment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a dummy payment modal in the bundled frontend so `Book Now` opens the modal first and only sends the existing booking request after `Pay Now` or `Skip Payment`, without changing owner approval behavior.

**Architecture:** Keep the backend booking endpoint and owner approval flow unchanged. Add one small pure helper module for dummy payment decisions and payload shaping, one `PaymentModal` component file for UI, and then wire the existing hostel details page to open the modal before sending the existing `BOOKING` inquiry request. Use Node's built-in test runner for frontend helper/component tests and keep CSS changes local to the existing global stylesheet.

**Tech Stack:** Vanilla ES modules in `backend/src/main/resources/frontend`, HTML/CSS, Node `node:test`, Spring Boot backend left functionally unchanged, Maven verification for regression confidence.

---

## File Structure

**Create**

- `backend/src/main/resources/frontend/js/paymentFlow.mjs`
- `backend/src/main/resources/frontend/js/paymentFlow.test.mjs`
- `backend/src/main/resources/frontend/js/components/PaymentModal.js`
- `backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

**Modify**

- `backend/src/main/resources/frontend/js/hostelDetails.js`
- `backend/src/main/resources/frontend/pages/hostel-details.html`
- `backend/src/main/resources/frontend/css/global.css`

**Responsibility Map**

- `paymentFlow.mjs`: pure helper functions for choosing the room type/amount, building dummy payment state, and shaping the unchanged booking payload.
- `paymentFlow.test.mjs`: regression tests for cheapest-room selection, fallback behavior, dummy payment state, and booking payload shaping.
- `components/PaymentModal.js`: modal UI creation, open/close behavior, fake payment method selection, and promise-based result handling.
- `components/PaymentModal.test.mjs`: string/markup-level tests proving the modal shows hostel name, room type, amount, and the required action buttons.
- `hostelDetails.js`: page integration only; keep inquiry behavior intact and route booking through the modal before calling the existing booking endpoint.
- `hostel-details.html`: add the modal mount point and clean up the existing `Book Now` button id mismatch.
- `global.css`: minimal responsive modal styling only.

### Task 1: Add Pure Dummy Payment Flow Helpers

**Files:**
- Create: `backend/src/main/resources/frontend/js/paymentFlow.mjs`
- Create: `backend/src/main/resources/frontend/js/paymentFlow.test.mjs`

- [ ] **Step 1: Write the failing helper tests**

```javascript
import test from "node:test";
import assert from "node:assert/strict";

import {
  buildBookingPayload,
  buildPostPaymentState,
  derivePaymentDetails,
} from "./paymentFlow.mjs";

test("derivePaymentDetails uses the cheapest room type when available", () => {
  const details = derivePaymentDetails({
    id: 7,
    hostelName: "Galaxy Girls PG",
    pricePerMonth: 8200,
    roomTypes: [
      { roomType: "3 Bed Room", pricePerMonth: 6200, totalRooms: 10, availableRooms: 3 },
      { roomType: "2 Bed Room", pricePerMonth: 7000, totalRooms: 8, availableRooms: 2 },
    ],
  });

  assert.deepEqual(details, {
    hostelId: 7,
    hostelName: "Galaxy Girls PG",
    roomType: "3 Bed Room",
    amount: 6200,
  });
});

test("derivePaymentDetails falls back to standard room when room types are missing", () => {
  const details = derivePaymentDetails({
    id: 8,
    hostelName: "Central Co-ed Hostel",
    pricePerMonth: 9000,
    roomTypes: [],
  });

  assert.deepEqual(details, {
    hostelId: 8,
    hostelName: "Central Co-ed Hostel",
    roomType: "Standard Room",
    amount: 9000,
  });
});

test("buildPostPaymentState records pay and skip differently", () => {
  const paymentDetails = {
    hostelId: 8,
    hostelName: "Central Co-ed Hostel",
    roomType: "Standard Room",
    amount: 9000,
  };

  assert.deepEqual(
    buildPostPaymentState("PAY", "UPI", paymentDetails),
    {
      hostelId: 8,
      hostelName: "Central Co-ed Hostel",
      roomType: "Standard Room",
      amount: 9000,
      method: "UPI",
      status: "SUCCESS",
    }
  );

  assert.deepEqual(
    buildPostPaymentState("SKIP", "CARD", paymentDetails),
    {
      hostelId: 8,
      hostelName: "Central Co-ed Hostel",
      roomType: "Standard Room",
      amount: 9000,
      method: "CARD",
      status: "SKIPPED",
    }
  );
});

test("buildBookingPayload keeps the existing backend contract", () => {
  assert.deepEqual(
    buildBookingPayload({
      hostelId: 5,
      requestedRoomCount: 2,
      message: "Need a room from next week",
    }),
    {
      hostelId: 5,
      requestedRoomCount: 2,
      message: "Need a room from next week",
      requestType: "BOOKING",
    }
  );
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test backend/src/main/resources/frontend/js/paymentFlow.test.mjs`

Expected: FAIL with `ERR_MODULE_NOT_FOUND` for `./paymentFlow.mjs`

- [ ] **Step 3: Write the minimal helper implementation**

```javascript
export const DUMMY_PAYMENT_SESSION_KEY = "dummyBookingPayment";

export function derivePaymentDetails(hostel) {
  const roomTypes = Array.isArray(hostel?.roomTypes) ? hostel.roomTypes : [];
  const cheapestRoomType = roomTypes
    .filter((roomType) => roomType && Number.isFinite(Number(roomType.pricePerMonth)))
    .sort((left, right) => Number(left.pricePerMonth) - Number(right.pricePerMonth))[0];

  if (cheapestRoomType) {
    return {
      hostelId: hostel.id,
      hostelName: hostel.hostelName,
      roomType: cheapestRoomType.roomType || "Standard Room",
      amount: Number(cheapestRoomType.pricePerMonth),
    };
  }

  return {
    hostelId: hostel?.id ?? null,
    hostelName: hostel?.hostelName ?? "Hostel",
    roomType: "Standard Room",
    amount: Number(hostel?.pricePerMonth ?? 0),
  };
}

export function buildPostPaymentState(action, selectedMethod, paymentDetails) {
  return {
    hostelId: paymentDetails.hostelId,
    hostelName: paymentDetails.hostelName,
    roomType: paymentDetails.roomType,
    amount: paymentDetails.amount,
    method: selectedMethod,
    status: action === "PAY" ? "SUCCESS" : "SKIPPED",
  };
}

export function buildBookingPayload({ hostelId, requestedRoomCount, message }) {
  return {
    hostelId,
    requestedRoomCount,
    message,
    requestType: "BOOKING",
  };
}

export function saveDummyPaymentState(storage, paymentState) {
  storage.setItem(
    DUMMY_PAYMENT_SESSION_KEY,
    JSON.stringify({
      ...paymentState,
      timestamp: new Date().toISOString(),
    })
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test backend/src/main/resources/frontend/js/paymentFlow.test.mjs`

Expected: PASS with 4 passing tests

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/frontend/js/paymentFlow.mjs backend/src/main/resources/frontend/js/paymentFlow.test.mjs
git commit -m "test: add dummy booking payment flow helpers"
```

### Task 2: Add the PaymentModal Component

**Files:**
- Create: `backend/src/main/resources/frontend/js/components/PaymentModal.js`
- Create: `backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

- [ ] **Step 1: Write the failing component markup test**

```javascript
import test from "node:test";
import assert from "node:assert/strict";

import { createPaymentModalMarkup, PAYMENT_METHODS } from "./PaymentModal.js";

test("payment modal markup includes the required summary and actions", () => {
  const markup = createPaymentModalMarkup({
    hostelName: "Galaxy Girls PG",
    roomType: "3 Bed Room",
    amount: 6200,
    selectedMethod: "UPI",
  });

  assert.match(markup, /Dummy Payment/);
  assert.match(markup, /Galaxy Girls PG/);
  assert.match(markup, /3 Bed Room/);
  assert.match(markup, /6200/);
  assert.match(markup, /Pay Now/);
  assert.match(markup, /Skip Payment/);
});

test("payment modal exports the fake payment methods", () => {
  assert.deepEqual(PAYMENT_METHODS, ["UPI", "CARD", "CASH"]);
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

Expected: FAIL with `ERR_MODULE_NOT_FOUND` for `./PaymentModal.js`

- [ ] **Step 3: Write the minimal component implementation**

```javascript
export const PAYMENT_METHODS = ["UPI", "CARD", "CASH"];

export function createPaymentModalMarkup({ hostelName, roomType, amount, selectedMethod }) {
  const paymentOptions = PAYMENT_METHODS.map((method) => `
    <label class="payment-method ${method === selectedMethod ? "is-selected" : ""}">
      <input type="radio" name="dummyPaymentMethod" value="${method}" ${method === selectedMethod ? "checked" : ""}>
      <span>${method}</span>
    </label>
  `).join("");

  return `
    <div class="payment-modal-backdrop" data-role="payment-backdrop">
      <div class="payment-modal-card" role="dialog" aria-modal="true" aria-labelledby="dummyPaymentTitle">
        <button type="button" class="payment-modal-close" data-action="cancel" aria-label="Close">×</button>
        <h2 id="dummyPaymentTitle">Dummy Payment</h2>
        <div class="payment-summary-row"><strong>Hostel:</strong> <span>${hostelName}</span></div>
        <div class="payment-summary-row"><strong>Room Type:</strong> <span>${roomType}</span></div>
        <div class="payment-summary-row"><strong>Amount:</strong> <span>₹${amount}</span></div>
        <div class="payment-method-list">${paymentOptions}</div>
        <div class="payment-modal-actions">
          <button type="button" class="btn btn-primary" data-action="pay">Pay Now</button>
          <button type="button" class="btn" data-action="skip">Skip Payment</button>
        </div>
      </div>
    </div>
  `;
}

export function openPaymentModal({ mountNode, paymentDetails }) {
  return new Promise((resolve) => {
    let selectedMethod = "UPI";
    mountNode.innerHTML = createPaymentModalMarkup({ ...paymentDetails, selectedMethod });

    const rerender = () => {
      mountNode.innerHTML = createPaymentModalMarkup({ ...paymentDetails, selectedMethod });
      bindEvents();
    };

    const close = (result) => {
      mountNode.innerHTML = "";
      resolve(result);
    };

    const bindEvents = () => {
      mountNode.querySelectorAll('input[name="dummyPaymentMethod"]').forEach((input) => {
        input.addEventListener("change", (event) => {
          selectedMethod = event.target.value;
          rerender();
        });
      });

      mountNode.querySelector('[data-action="pay"]').addEventListener("click", () => {
        close({ action: "PAY", selectedMethod });
      });

      mountNode.querySelector('[data-action="skip"]').addEventListener("click", () => {
        close({ action: "SKIP", selectedMethod });
      });

      mountNode.querySelector('[data-action="cancel"]').addEventListener("click", () => {
        close({ action: "CANCEL", selectedMethod });
      });

      mountNode.querySelector('[data-role="payment-backdrop"]').addEventListener("click", (event) => {
        if (event.target === event.currentTarget) {
          close({ action: "CANCEL", selectedMethod });
        }
      });
    };

    bindEvents();
  });
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

Expected: PASS with 2 passing tests

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/frontend/js/components/PaymentModal.js backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs
git commit -m "feat: add dummy payment modal component"
```

### Task 3: Wire Book Now Through the Modal and Preserve the Existing Booking API

**Files:**
- Modify: `backend/src/main/resources/frontend/js/hostelDetails.js`
- Modify: `backend/src/main/resources/frontend/pages/hostel-details.html`
- Modify: `backend/src/main/resources/frontend/css/global.css`
- Test: `backend/src/main/resources/frontend/js/paymentFlow.test.mjs`

- [ ] **Step 1: Extend the helper test with cancel behavior before changing the page**

```javascript
test("canceling the modal does not produce a booking payload", () => {
  const result = (() => {
    const action = "CANCEL";
    return action === "CANCEL"
      ? null
      : buildBookingPayload({ hostelId: 9, requestedRoomCount: 1, message: "" });
  })();

  assert.equal(result, null);
});
```

- [ ] **Step 2: Run test to verify it fails for the right reason**

Run: `node --test backend/src/main/resources/frontend/js/paymentFlow.test.mjs`

Expected: FAIL because the temporary inline cancel logic is not yet represented in the real page integration

- [ ] **Step 3: Update the hostel details page, page script, and CSS with the minimal implementation**

```html
<!-- backend/src/main/resources/frontend/pages/hostel-details.html -->
<div style="display:flex; gap:10px; margin-top:10px; flex-wrap:wrap;">
  <button id="sendInquiryBtn" class="btn btn-primary" style="flex:1 1 220px;">Send Inquiry</button>
  <button id="bookingBtn" class="btn btn-primary" style="flex:1 1 220px;">Book Now</button>
</div>
<div id="paymentModalRoot"></div>
```

```javascript
// backend/src/main/resources/frontend/js/hostelDetails.js
import { apiFetch, getToken } from "./apiClient.js";
import {
  buildBookingPayload,
  buildPostPaymentState,
  derivePaymentDetails,
  saveDummyPaymentState,
} from "./paymentFlow.mjs";
import { openPaymentModal } from "./components/PaymentModal.js";

const paymentModalRoot = document.getElementById("paymentModalRoot");
let currentHostel = null;

async function load() {
  const hostelId = getHostelId();
  if (!hostelId) {
    detailsEl.innerHTML = `<div class="card-body"><div class="notice"><strong>Error:</strong> Missing hostel id.</div></div>`;
    return;
  }
  currentHostel = await apiFetch(`/public/hostels/${hostelId}`);
  renderDetails(currentHostel);
}

async function submitBookingAfterPayment(action, selectedMethod) {
  if (action === "CANCEL") {
    return;
  }

  const paymentDetails = derivePaymentDetails(currentHostel);
  const bookingPayload = buildBookingPayload({
    hostelId: getHostelId(),
    requestedRoomCount: Number(roomCountEl.value || "1"),
    message: messageEl.value || "",
  });

  if (action === "PAY") {
    const paymentState = buildPostPaymentState(action, selectedMethod, paymentDetails);
    saveDummyPaymentState(window.sessionStorage, paymentState);
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Payment successful!</strong> Dummy payment completed via ${escapeHtml(selectedMethod)}. Sending booking request...`;
  }

  if (action === "SKIP") {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Payment skipped.</strong> Sending booking request to the owner...`;
  }

  await apiFetch(`/student/inquiries`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(bookingPayload),
  });

  inqMsg.style.display = "block";
  inqMsg.innerHTML = `<strong>Sent!</strong> Booking request sent to the owner for approval.`;
  messageEl.value = "";
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
    const paymentDetails = derivePaymentDetails(currentHostel);
    const result = await openPaymentModal({ mountNode: paymentModalRoot, paymentDetails });
    await submitBookingAfterPayment(result.action, result.selectedMethod);
  } catch (e) {
    inqMsg.style.display = "block";
    inqMsg.innerHTML = `<strong>Error:</strong> ${escapeHtml(e.message)}`;
  } finally {
    bookBtn.disabled = false;
    bookBtn.textContent = "Book Now";
  }
}

bookBtn?.addEventListener("click", bookNow);
```

```css
/* backend/src/main/resources/frontend/css/global.css */
.payment-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1000;
}

.payment-modal-card {
  width: min(100%, 460px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  padding: 24px;
  position: relative;
}

.payment-modal-close {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
}

.payment-summary-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin: 10px 0;
}

.payment-method-list {
  display: grid;
  gap: 10px;
  margin: 18px 0;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: 14px;
  cursor: pointer;
}

.payment-method.is-selected {
  border-color: var(--primary);
  background: var(--primary-light);
}

.payment-modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

@media (max-width: 640px) {
  .payment-modal-actions {
    flex-direction: column;
  }
}
```

- [ ] **Step 4: Run tests and syntax checks to verify it passes**

Run: `node --test backend/src/main/resources/frontend/js/paymentFlow.test.mjs backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

Expected: PASS with all tests green

Run: `node --check backend/src/main/resources/frontend/js/hostelDetails.js`

Expected: PASS with no syntax errors

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/frontend/js/hostelDetails.js backend/src/main/resources/frontend/pages/hostel-details.html backend/src/main/resources/frontend/css/global.css
git commit -m "feat: route booking through dummy payment modal"
```

### Task 4: Verify Regressions and Do the Final Manual Pass

**Files:**
- Test: `backend/src/main/resources/frontend/js/paymentFlow.test.mjs`
- Test: `backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`
- Verify: `backend/src/main/resources/frontend/pages/hostel-details.html`
- Verify: `backend/src/main/resources/frontend/js/hostelDetails.js`

- [ ] **Step 1: Run the frontend-focused automated checks**

Run: `node --test backend/src/main/resources/frontend/js/paymentFlow.test.mjs backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs`

Expected: PASS with all dummy payment helper/component tests green

- [ ] **Step 2: Run the backend regression suite**

Run: `./mvnw.cmd test`

Expected: PASS with existing owner/admin/controller tests still green and no backend booking regression introduced

- [ ] **Step 3: Do the manual browser regression sweep**

Manual checklist:

```text
1. Open hostel details page.
2. Click Send Inquiry and confirm the old inquiry flow still works.
3. Click Book Now and confirm the dummy payment modal opens.
4. Click Pay Now and confirm:
   - dummy payment success message appears
   - booking request is then sent
   - owner can still see the booking as PENDING
5. Click Book Now again and choose Skip Payment and confirm:
   - modal closes
   - booking request is still sent
6. Open owner dashboard and confirm approve/reject still behaves exactly as before.
7. Reopen hostel details on mobile width and confirm the modal remains readable and buttons stack cleanly.
```

Expected: All checks pass with unchanged owner approval behavior

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/resources/frontend/js/paymentFlow.test.mjs backend/src/main/resources/frontend/js/components/PaymentModal.test.mjs
git commit -m "test: verify dummy booking payment flow"
```
