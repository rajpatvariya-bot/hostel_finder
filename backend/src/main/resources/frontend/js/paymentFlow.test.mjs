import test from "node:test";
import assert from "node:assert/strict";

import {
  buildBookingPayload,
  buildPostPaymentState,
  derivePaymentDetails,
  shouldSubmitBookingAction,
} from "./paymentFlow.mjs";

test("derivePaymentDetails uses the cheapest room type when available", () => {
  const details = derivePaymentDetails({
    id: 7,
    hostelName: "Galaxy Girls PG",
    pricePerMonth: 8200,
    roomTypes: [
      { id: 71, roomType: "3 Bed Room", pricePerMonth: 6200, totalRooms: 10, availableRooms: 3 },
      { id: 72, roomType: "2 Bed Room", pricePerMonth: 7000, totalRooms: 8, availableRooms: 2 },
    ],
  });

  assert.deepEqual(details, {
    hostelId: 7,
    hostelName: "Galaxy Girls PG",
    roomTypeId: 71,
    roomType: "3 Bed Room",
    amount: 6200,
  });
});

test("derivePaymentDetails prefers the explicitly selected room type when provided", () => {
  const details = derivePaymentDetails({
    id: 9,
    hostelName: "Apex Residency",
    pricePerMonth: 7600,
    roomTypes: [
      { id: 91, roomType: "1 Bed Room", pricePerMonth: 9000, totalRooms: 4, availableRooms: 1 },
      { id: 92, roomType: "3 Bed Room", pricePerMonth: 5800, totalRooms: 12, availableRooms: 5 },
    ],
  }, 91);

  assert.deepEqual(details, {
    hostelId: 9,
    hostelName: "Apex Residency",
    roomTypeId: 91,
    roomType: "1 Bed Room",
    amount: 9000,
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
    roomTypeId: null,
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
      roomTypeId: null,
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
      roomTypeId: null,
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
      roomTypeId: 44,
      requestedRoomCount: 2,
      message: "Need a room from next week",
    }),
    {
      hostelId: 5,
      roomTypeId: 44,
      requestedRoomCount: 2,
      message: "Need a room from next week",
      requestType: "BOOKING",
    }
  );
});

test("shouldSubmitBookingAction blocks cancel and allows pay or skip", () => {
  assert.equal(shouldSubmitBookingAction("PAY"), true);
  assert.equal(shouldSubmitBookingAction("SKIP"), true);
  assert.equal(shouldSubmitBookingAction("CANCEL"), false);
});
