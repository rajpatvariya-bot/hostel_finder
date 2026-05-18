# Dummy Booking Payment Design

**Date:** 2026-05-18

**Goal**

Add a dummy payment step to the student booking UI for testing purposes without breaking the current booking request flow or the existing owner approval system.

## Current Behavior

The current student booking flow already works:

1. Student opens hostel details.
2. Student clicks `Book Now`.
3. Frontend sends a booking request to `POST /api/student/inquiries`.
4. Backend stores an inquiry with `requestType = "BOOKING"` and `status = "PENDING"`.
5. Owner sees the request in the owner inquiries dashboard and can approve or reject it.

This existing owner approval behavior must remain unchanged.

## Desired Behavior

The dummy payment UI should sit in front of the current booking request.

New booking flow:

1. Student clicks `Book Now`.
2. Frontend opens a dummy payment modal instead of sending the request immediately.
3. Modal shows:
   - hostel name
   - room type
   - amount
   - fake payment methods: `UPI`, `Card`, `Cash`
   - `Pay Now` button
   - `Skip Payment` button
4. If student clicks `Pay Now`:
   - frontend simulates a successful payment
   - frontend stores dummy payment status in frontend state only
   - frontend shows a success message
   - frontend then sends the normal booking request to the existing backend endpoint
5. If student clicks `Skip Payment`:
   - modal closes
   - frontend still sends the normal booking request to the existing backend endpoint
6. Owner receives and handles the booking request exactly as before.

## Scope

### In Scope

- Add a new payment modal UI in the frontend bundled inside the backend project.
- Change the student `Book Now` click flow so the booking request is sent after the modal action, not before.
- Keep using the existing booking endpoint and owner approval flow.
- Store dummy payment state only on the frontend.
- Keep changes small and beginner-friendly.

### Out of Scope

- Real payment gateway integration
- Razorpay or any third-party payment SDK
- Database payment tables
- Backend payment APIs
- Owner-side payment validation
- Changes to approval/rejection logic
- Refactoring unrelated frontend or backend code

## Target Frontend

Use the frontend inside:

- `backend/src/main/resources/frontend`

Do not target `frontend1` for the main implementation.

## Technical Approach

### Frontend

The frontend will introduce a small `PaymentModal` component script that can be opened from the hostel details page.

The hostel details page currently owns the booking action. That page will be updated so:

- `Send Inquiry` stays unchanged
- `Book Now` opens the modal
- modal action controls when the real booking request is sent

The modal will not know anything about owner approval. It only gathers a dummy payment choice and resolves the student’s intent:

- `PAY`
- `SKIP`
- `CANCEL`

Only `PAY` and `SKIP` continue to send the booking request.

### Backend

Backend behavior should remain functionally unchanged.

The existing endpoint:

- `POST /api/student/inquiries`

should still receive the booking request with:

- `hostelId`
- `requestedRoomCount`
- `message`
- `requestType = "BOOKING"`

No owner-side API changes are required.

Backend changes should be avoided unless the bundled frontend copy depends on a tiny compatibility fix. The preferred implementation is zero backend logic changes.

## Room Type Display Rule

To keep the change minimal, the modal will not add a new room type selector for this feature.

Room type shown in the modal will be derived from hostel details:

1. If `roomTypes` exists and has items, use the cheapest room type.
2. If multiple room types share the same cheapest price, use the first cheapest match.
3. If no room type data exists, fall back to:
   - room type: `Standard Room`
   - amount: `pricePerMonth`

This keeps the UI simple and avoids changing the booking payload schema.

## Dummy Payment State

Dummy payment must be frontend-only.

Recommended state model:

- keep the active payment result in page state while the modal is open
- persist the most recent dummy payment result in `sessionStorage` for simple UI continuity on refresh during the same browser session

Stored fields may include:

- `hostelId`
- `hostelName`
- `roomType`
- `amount`
- `method`
- `status`
- `timestamp`

This is strictly for UI/testing and must not affect backend approval logic.

## UI Design

### Modal Content

The modal should show:

- title: `Dummy Payment`
- hostel name
- chosen room type
- amount
- radio-style fake payment methods:
  - `UPI`
  - `Card`
  - `Cash`
- primary button: `Pay Now`
- secondary button: `Skip Payment`
- close button or backdrop dismiss for canceling without sending the request

### Responsive Behavior

- centered modal on desktop
- full-width or near-full-width card on mobile
- stacked action buttons on narrow screens if needed
- clear spacing and readable labels

### Messaging

When `Pay Now` succeeds:

- show dummy payment success message
- then show the existing booking success message

When `Skip Payment` is used:

- skip payment messaging should be brief
- booking success messaging should still confirm that the owner received the request

When canceled:

- no booking request should be sent
- existing page should remain unchanged

## File Changes

### Create

- `backend/src/main/resources/frontend/js/components/PaymentModal.js`

### Modify

- `backend/src/main/resources/frontend/js/hostelDetails.js`
- `backend/src/main/resources/frontend/pages/hostel-details.html`

### Optional Minimal Styling Touch

If needed, add a small amount of modal styling in the existing frontend CSS file already used by the page rather than introducing a brand-new styling system.

## Data Flow

### Book Now Flow

1. User clicks `Book Now`.
2. Frontend gathers hostel details already loaded on the page.
3. Frontend derives:
   - hostel name
   - room type
   - amount
4. Frontend opens `PaymentModal`.
5. User chooses one action:
   - `Pay Now`
   - `Skip Payment`
   - close/cancel
6. If `Pay Now`:
   - simulate payment success in frontend state
   - continue to the existing booking POST
7. If `Skip Payment`:
   - continue to the existing booking POST without payment
8. If cancel:
   - stop
9. Existing backend booking request is sent unchanged.
10. Existing owner dashboard receives the booking request unchanged.

## Error Handling

### Frontend

- If hostel details are missing, do not open the modal.
- If booking POST fails after `Pay Now`, show booking error separately from dummy payment success.
- If booking POST fails after `Skip Payment`, show the existing booking error.
- Prevent double submits while payment simulation or booking POST is running.

### Backend

No new backend error handling is required unless a tiny compatibility fix becomes necessary.

## Testing Strategy

### Manual Tests

1. `Send Inquiry` still works exactly as before.
2. `Book Now` opens modal instead of sending request immediately.
3. `Pay Now` simulates success and then sends booking request.
4. `Skip Payment` closes the payment step and still sends booking request.
5. Canceling the modal sends no booking request.
6. Owner still sees booking requests in owner dashboard.
7. Owner can still approve or reject booking requests exactly as before.
8. Modal works on mobile width and desktop width.
9. Cheapest room type fallback displays correctly.
10. No room type data fallback displays `Standard Room`.

### Regression Focus

- booking endpoint unchanged
- request type unchanged
- owner approval unchanged
- inquiry flow unchanged

## Risks and Mitigations

### Risk: Bundled frontend is inconsistent with another frontend copy

Mitigation:

- implement against `backend/src/main/resources/frontend`
- avoid broad cross-project refactors

### Risk: User sees dummy payment success but booking request fails

Mitigation:

- show separate booking submission result after payment simulation
- do not imply owner received the request unless booking POST succeeds

### Risk: Existing page has mixed old experimental code

Mitigation:

- isolate new modal code in its own component file
- keep `hostelDetails.js` changes focused on booking flow only

## Implementation Summary

The safest implementation is a frontend-only dummy payment modal placed before the current booking POST. The modal decides whether to simulate payment or skip payment, and then the page sends the same booking request the system already uses. This preserves the current owner approval workflow while adding a realistic test UI for future payment integration.
