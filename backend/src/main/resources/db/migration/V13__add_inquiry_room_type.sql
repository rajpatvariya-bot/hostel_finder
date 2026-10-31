ALTER TABLE inquiries
  ADD COLUMN room_type_id BIGINT NULL AFTER hostel_id,
  ADD CONSTRAINT fk_inquiries_room_type
    FOREIGN KEY (room_type_id) REFERENCES hostel_room_types(room_type_id)
    ON DELETE SET NULL,
  ADD KEY idx_inquiries_room_type (room_type_id);
