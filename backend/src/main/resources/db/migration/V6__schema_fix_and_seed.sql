-- Fix schema and seed facilities
ALTER TABLE hostels ADD COLUMN image_url VARCHAR(500) NULL AFTER description;

INSERT INTO facilities (name) VALUES ('Gym') ON DUPLICATE KEY UPDATE name=name;

SET @hotel_boys = (SELECT hostel_id FROM hostels WHERE hostel_name='Shree Boys Hostel' LIMIT 1);
SET @hotel_girls = (SELECT hostel_id FROM hostels WHERE hostel_name='Galaxy Girls PG' LIMIT 1);
SET @hotel_coed = (SELECT hostel_id FROM hostels WHERE hostel_name='Central Co-ed Hostel' LIMIT 1);

SET @fac_wifi = (SELECT facility_id FROM facilities WHERE name='WiFi' LIMIT 1);
SET @fac_ac = (SELECT facility_id FROM facilities WHERE name='AC' LIMIT 1);
SET @fac_laundry = (SELECT facility_id FROM facilities WHERE name='Laundry' LIMIT 1);
SET @fac_gym = (SELECT facility_id FROM facilities WHERE name='Gym' LIMIT 1);

INSERT IGNORE INTO hostel_facilities (hostel_id, facility_id) VALUES 
(@hotel_boys, @fac_wifi), (@hotel_boys, @fac_laundry),
(@hotel_girls, @fac_wifi), (@hotel_girls, @fac_ac), (@hotel_girls, @fac_laundry),
(@hotel_coed, @fac_wifi), (@hotel_coed, @fac_gym);
