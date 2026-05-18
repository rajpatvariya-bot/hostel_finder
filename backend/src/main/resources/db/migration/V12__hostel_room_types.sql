CREATE TABLE hostel_room_types (
  room_type_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hostel_id BIGINT NOT NULL,
  room_type VARCHAR(120) NOT NULL,
  price_per_month DECIMAL(10,2) NOT NULL,
  total_rooms INT NOT NULL DEFAULT 0,
  available_rooms INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_hostel_room_types_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(hostel_id) ON DELETE CASCADE,
  UNIQUE KEY uk_hostel_room_types_name (hostel_id, room_type),
  KEY idx_hostel_room_types_hostel (hostel_id),
  KEY idx_hostel_room_types_price (price_per_month)
);

INSERT INTO hostel_room_types (hostel_id, room_type, price_per_month, total_rooms, available_rooms)
SELECT h.hostel_id, 'Standard Room', h.price_per_month, h.total_rooms, h.available_rooms
FROM hostels h
WHERE NOT EXISTS (
  SELECT 1
  FROM hostel_room_types rt
  WHERE rt.hostel_id = h.hostel_id
);

UPDATE hostels h
JOIN (
  SELECT
    hostel_id,
    MIN(price_per_month) AS minimum_price,
    SUM(total_rooms) AS total_rooms,
    SUM(available_rooms) AS available_rooms
  FROM hostel_room_types
  GROUP BY hostel_id
) rt ON rt.hostel_id = h.hostel_id
SET
  h.price_per_month = rt.minimum_price,
  h.total_rooms = rt.total_rooms,
  h.available_rooms = rt.available_rooms;
