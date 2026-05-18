-- Backfill gallery rows from existing single-image hostels and align ordering

INSERT INTO hostel_images (hostel_id, file_url, sort_order)
SELECT h.hostel_id, h.image_url, 0
FROM hostels h
WHERE h.image_url IS NOT NULL
  AND TRIM(h.image_url) <> ''
  AND NOT EXISTS (
    SELECT 1
    FROM hostel_images hi
    WHERE hi.hostel_id = h.hostel_id
  );

SET @next_order := -1;
SET @current_hostel := -1;

UPDATE hostel_images hi
JOIN (
  SELECT
    image_id,
    hostel_id,
    (@next_order := IF(@current_hostel = hostel_id, @next_order + 1, 0)) AS new_order,
    (@current_hostel := hostel_id) AS _hostel_tracker
  FROM hostel_images
  ORDER BY hostel_id, sort_order, image_id
) ordered ON ordered.image_id = hi.image_id
SET hi.sort_order = ordered.new_order;
