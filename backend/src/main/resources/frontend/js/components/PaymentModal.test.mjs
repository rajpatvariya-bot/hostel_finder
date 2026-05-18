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
