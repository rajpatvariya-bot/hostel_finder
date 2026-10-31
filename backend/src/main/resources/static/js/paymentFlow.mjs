export const DUMMY_PAYMENT_SESSION_KEY = "dummyBookingPayment";

function resolveRequestedRoomType(hostel, requestedRoomType) {
  if (!requestedRoomType) {
    return null;
  }

  const roomTypes = Array.isArray(hostel?.roomTypes) ? hostel.roomTypes : [];
  if (typeof requestedRoomType === "object") {
    return requestedRoomType;
  }

  return roomTypes.find((roomType) => roomType && roomType.id === requestedRoomType) || null;
}

export function derivePaymentDetails(hostel, requestedRoomType = null) {
  const roomTypes = Array.isArray(hostel?.roomTypes) ? hostel.roomTypes : [];
  const selectedRoomType = resolveRequestedRoomType(hostel, requestedRoomType);
  const fallbackRoomType = roomTypes
    .filter((roomType) => roomType && Number.isFinite(Number(roomType.pricePerMonth)))
    .sort((left, right) => Number(left.pricePerMonth) - Number(right.pricePerMonth))[0];
  const effectiveRoomType = selectedRoomType && Number.isFinite(Number(selectedRoomType.pricePerMonth))
    ? selectedRoomType
    : fallbackRoomType;

  if (effectiveRoomType) {
    return {
      hostelId: hostel.id,
      hostelName: hostel.hostelName,
      roomTypeId: effectiveRoomType.id ?? null,
      roomType: effectiveRoomType.roomType || "Standard Room",
      amount: Number(effectiveRoomType.pricePerMonth),
    };
  }

  return {
    hostelId: hostel?.id ?? null,
    hostelName: hostel?.hostelName ?? "Hostel",
    roomTypeId: null,
    roomType: "Standard Room",
    amount: Number(hostel?.pricePerMonth ?? 0),
  };
}

export function buildPostPaymentState(action, selectedMethod, paymentDetails) {
  return {
    hostelId: paymentDetails.hostelId,
    hostelName: paymentDetails.hostelName,
    roomTypeId: paymentDetails.roomTypeId ?? null,
    roomType: paymentDetails.roomType,
    amount: paymentDetails.amount,
    method: selectedMethod,
    status: action === "PAY" ? "SUCCESS" : "SKIPPED",
  };
}

export function buildBookingPayload({ hostelId, roomTypeId = null, requestedRoomCount, message }) {
  return {
    hostelId,
    roomTypeId,
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
