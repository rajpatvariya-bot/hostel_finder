export const PAYMENT_METHODS = ["UPI", "CARD", "CASH"];

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

export function createPaymentModalMarkup({ hostelName, roomType, amount, selectedMethod }) {
  const paymentOptions = PAYMENT_METHODS.map((method) => `
    <label class="payment-method ${method === selectedMethod ? "is-selected" : ""}">
      <input type="radio" name="dummyPaymentMethod" value="${method}" ${method === selectedMethod ? "checked" : ""}>
      <span>${escapeHtml(method)}</span>
    </label>
  `).join("");

  return `
    <div class="payment-modal-backdrop" data-role="payment-backdrop">
      <div class="payment-modal-card" role="dialog" aria-modal="true" aria-labelledby="dummyPaymentTitle">
        <button type="button" class="payment-modal-close" data-action="cancel" aria-label="Close">x</button>
        <h2 id="dummyPaymentTitle">Dummy Payment</h2>
        <div class="payment-summary-row"><strong>Hostel:</strong> <span>${escapeHtml(hostelName)}</span></div>
        <div class="payment-summary-row"><strong>Room Type:</strong> <span>${escapeHtml(roomType)}</span></div>
        <div class="payment-summary-row"><strong>Amount:</strong> <span>Rs. ${escapeHtml(amount)}</span></div>
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

    const close = (result) => {
      mountNode.innerHTML = "";
      resolve(result);
    };

    const bindEvents = () => {
      mountNode.querySelectorAll('input[name="dummyPaymentMethod"]').forEach((input) => {
        input.addEventListener("change", (event) => {
          selectedMethod = event.target.value;
          render();
        });
      });

      mountNode.querySelector('[data-action="pay"]')?.addEventListener("click", () => {
        close({ action: "PAY", selectedMethod });
      });

      mountNode.querySelector('[data-action="skip"]')?.addEventListener("click", () => {
        close({ action: "SKIP", selectedMethod });
      });

      mountNode.querySelector('[data-action="cancel"]')?.addEventListener("click", () => {
        close({ action: "CANCEL", selectedMethod });
      });

      mountNode.querySelector('[data-role="payment-backdrop"]')?.addEventListener("click", (event) => {
        if (event.target === event.currentTarget) {
          close({ action: "CANCEL", selectedMethod });
        }
      });
    };

    const render = () => {
      mountNode.innerHTML = createPaymentModalMarkup({
        ...paymentDetails,
        selectedMethod,
      });
      bindEvents();
    };

    render();
  });
}
