-- Demo data for public search page (PUBLISHED hostels)
-- Uses Indore city and seeded areas from V1.

-- Create a verified owner user quickly (for demo). Password hash will be set later via app.
INSERT INTO users (role, name, email, phone, password_hash, status)
VALUES ('OWNER', 'Demo Owner', 'owner@demo.com', '9999990001', '$2a$10$demo.demo.demo.demo.demo.demo.demo.demo.demo', 'ACTIVE');
SET @owner_user_id = LAST_INSERT_ID();

INSERT INTO hostel_owners (user_id, owner_status, verified_at)
VALUES (@owner_user_id, 'VERIFIED', NOW());

SET @city_id = (SELECT city_id FROM cities WHERE name='Indore' LIMIT 1);
SET @area_bhanwarkuan = (SELECT area_id FROM areas WHERE city_id=@city_id AND name='Bhanwarkuan' LIMIT 1);
SET @area_vijay = (SELECT area_id FROM areas WHERE city_id=@city_id AND name='Vijay Nagar' LIMIT 1);
SET @area_old_palasia = (SELECT area_id FROM areas WHERE city_id=@city_id AND name='Old Palasia' LIMIT 1);

INSERT INTO hostels (owner_user_id, city_id, area_id, hostel_name, address_line, price_per_month, gender_type, mess_available, status, total_rooms, available_rooms)
VALUES
(@owner_user_id, @city_id, @area_bhanwarkuan, 'Shree Boys Hostel', 'Near DAVV, Bhanwarkuan', 6500.00, 'BOYS', 1, 'PUBLISHED', 20, 6),
(@owner_user_id, @city_id, @area_vijay, 'Galaxy Girls PG', 'Scheme 54, Vijay Nagar', 8000.00, 'GIRLS', 1, 'PUBLISHED', 15, 2),
(@owner_user_id, @city_id, @area_old_palasia, 'Central Co-ed Hostel', 'Palasia Square', 9000.00, 'COED', 0, 'PUBLISHED', 12, 0);

