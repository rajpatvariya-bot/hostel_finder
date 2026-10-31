ALTER TABLE inquiries ADD COLUMN room_type_id BIGINT NULL;
ALTER TABLE inquiries ADD KEY idx_inquiries_room_type (room_type_id);
ALTER TABLE inquiries ADD CONSTRAINT fk_inquiries_room_type FOREIGN KEY (room_type_id) REFERENCES hostel_room_types(room_type_id) ON DELETE SET NULL;
