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

export function shouldSubmitBookingAction(action) {
  return action === "PAY" || action === "SKIP";
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
