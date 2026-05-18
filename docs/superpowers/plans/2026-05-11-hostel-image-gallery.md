# Hostel Image Gallery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add multi-image hostel uploads, storage, management, and public galleries without breaking existing hostel data or the current backend-served frontend flow.

**Architecture:** Keep Spring Boot as the source of truth with a new `HostelImage` persistence model, Flyway migration for existing `image_url` data, multipart upload endpoints, and a filesystem-backed storage service. Extend the owner dashboard modal for upload management and upgrade the public listing/details pages with lightweight, dependency-free carousels and gallery views.

**Tech Stack:** Spring Boot, Spring MVC multipart upload, JPA/Hibernate, Flyway, vanilla JS, backend-served HTML/CSS, local filesystem uploads.

---

### Task 1: Image persistence and migration

**Files:**
- Create: `backend/src/main/java/com/hostelfinder/entity/HostelImage.java`
- Create: `backend/src/main/java/com/hostelfinder/repository/HostelImageRepository.java`
- Create: `backend/src/main/resources/db/migration/V10__hostel_image_gallery.sql`
- Modify: `backend/src/main/java/com/hostelfinder/entity/Hostel.java`

- [ ] Add JPA entity and hostel relation for ordered image records.
- [ ] Add Flyway migration to create `hostel_images`, migrate old `hostels.image_url` values into ordered rows, and keep old records intact.
- [ ] Keep cover-image compatibility by deriving the first image from gallery rows.

### Task 2: Backend upload and gallery APIs

**Files:**
- Modify: `backend/src/main/java/com/hostelfinder/controller/OwnerHostelController.java`
- Modify: `backend/src/main/java/com/hostelfinder/dto/OwnerHostelDtos.java`
- Modify: `backend/src/main/java/com/hostelfinder/dto/PublicDtos.java`
- Modify: `backend/src/main/java/com/hostelfinder/controller/PublicController.java`
- Modify: `backend/src/main/java/com/hostelfinder/repository/HostelRepository.java`
- Modify: `backend/src/main/java/com/hostelfinder/storage/FileStorageService.java`
- Create: `backend/src/main/java/com/hostelfinder/dto/HostelImageDtos.java`

- [ ] Write failing controller tests for upload validation, image deletion, and response image ordering.
- [ ] Add filesystem image validation for max count, max size, supported formats, and unique filenames.
- [ ] Add owner APIs for upload, delete, reorder, and fetch.
- [ ] Return ordered `imageUrls` and `coverImageUrl` from owner/public responses.

### Task 3: Owner dashboard upload UX

**Files:**
- Modify: `backend/src/main/resources/static/pages/owner-dashboard.html`
- Modify: `backend/src/main/resources/static/js/ownerDashboard.js`
- Modify: `backend/src/main/resources/static/css/global.css`

- [ ] Replace single image URL input with drag-and-drop multi-upload UI.
- [ ] Add preview grid, remove action, reorder controls, and upload progress states.
- [ ] Keep existing hostel save flow and bind uploaded image metadata to create/edit behavior.

### Task 4: Home page card carousel

**Files:**
- Modify: `backend/src/main/resources/static/js/publicSearch.js`
- Modify: `backend/src/main/resources/static/css/global.css`

- [ ] Add lightweight card carousel with arrows, dots, touch swipe, and placeholder fallback.
- [ ] Keep cards lazy-loaded and avoid auto-slide by default.

### Task 5: Hostel details gallery

**Files:**
- Modify: `backend/src/main/resources/static/pages/hostel-details.html`
- Modify: `backend/src/main/resources/static/js/hostelDetails.js`
- Modify: `backend/src/main/resources/static/css/global.css`

- [ ] Add gallery hero, thumbnails, fullscreen lightbox, and smooth image transitions.
- [ ] Reuse ordered image data and placeholder fallback.

### Task 6: Verification

**Files:**
- Modify: `backend/src/test/java/com/hostelfinder/controller/AdminHostelControllerTest.java` if fixture names need alignment
- Create: `backend/src/test/java/com/hostelfinder/controller/OwnerHostelControllerTest.java`
- Create: `backend/src/test/java/com/hostelfinder/controller/PublicControllerImageTest.java`

- [ ] Run focused backend tests for controller and migration-adjacent logic.
- [ ] Run `backend/mvnw.cmd test`.
- [ ] Manually verify owner upload flow, home carousel, and hostel details gallery in the browser.
