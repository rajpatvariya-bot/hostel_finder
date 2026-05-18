import test from "node:test";
import assert from "node:assert/strict";

import { getHostelDetailsAccessState } from "./hostelDetailsAccess.mjs";

test("guest users stay on hostel details page and see login guidance", () => {
  const state = getHostelDetailsAccessState(null, null);

  assert.equal(state.shouldRedirect, false);
  assert.equal(state.canSubmit, false);
  assert.match(state.message, /login required/i);
});

test("student users can submit inquiries and bookings", () => {
  const state = getHostelDetailsAccessState("token", "STUDENT");

  assert.equal(state.shouldRedirect, false);
  assert.equal(state.canSubmit, true);
  assert.equal(state.message, "");
});
