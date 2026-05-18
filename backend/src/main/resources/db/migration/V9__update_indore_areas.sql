-- Update Indore areas to the expanded project list

SET @city_id = (SELECT city_id FROM cities WHERE name = 'Indore' LIMIT 1);
SET @area_bhanwarkuan = (SELECT area_id FROM areas WHERE city_id = @city_id AND name = 'Bhanwarkuan' LIMIT 1);
SET @area_bhawarkua = (SELECT area_id FROM areas WHERE city_id = @city_id AND name = 'Bhawarkua' LIMIT 1);
SET @area_old_palasia = (SELECT area_id FROM areas WHERE city_id = @city_id AND name = 'Old Palasia' LIMIT 1);
SET @area_palasia = (SELECT area_id FROM areas WHERE city_id = @city_id AND name = 'Palasia' LIMIT 1);

UPDATE hostels
SET area_id = @area_bhanwarkuan
WHERE @area_bhanwarkuan IS NOT NULL
  AND @area_bhawarkua IS NOT NULL
  AND area_id = @area_bhawarkua;

DELETE FROM areas
WHERE @area_bhanwarkuan IS NOT NULL
  AND area_id = @area_bhawarkua;

UPDATE areas
SET name = 'Bhanwarkuan'
WHERE @area_bhanwarkuan IS NULL
  AND area_id = @area_bhawarkua;

UPDATE hostels
SET area_id = @area_old_palasia
WHERE @area_old_palasia IS NOT NULL
  AND @area_palasia IS NOT NULL
  AND area_id = @area_palasia;

DELETE FROM areas
WHERE @area_old_palasia IS NOT NULL
  AND area_id = @area_palasia;

UPDATE areas
SET name = 'Old Palasia'
WHERE @area_old_palasia IS NULL
  AND area_id = @area_palasia;

INSERT INTO areas (city_id, name) VALUES
(@city_id, 'Bhanwarkuan'),
(@city_id, 'Vishnupuri'),
(@city_id, 'Indrapuri'),
(@city_id, 'Rau'),
(@city_id, 'Khandwa Naka'),
(@city_id, 'Geeta Bhawan'),
(@city_id, 'Navlakha'),
(@city_id, 'Bengali Square'),
(@city_id, 'Pipliyahana'),
(@city_id, 'Silicon City'),
(@city_id, 'Vijay Nagar'),
(@city_id, 'Scheme 54'),
(@city_id, 'Scheme 78'),
(@city_id, 'New Palasia'),
(@city_id, 'Old Palasia'),
(@city_id, 'Saket Nagar'),
(@city_id, 'Nipania'),
(@city_id, 'Super Corridor'),
(@city_id, 'Musakhedi'),
(@city_id, 'Pardeshipura'),
(@city_id, 'Malwa Mill'),
(@city_id, 'Azad Nagar'),
(@city_id, 'Sudama Nagar'),
(@city_id, 'Gumasta Nagar'),
(@city_id, 'Chhawani'),
(@city_id, 'Tukoganj'),
(@city_id, 'Race Course Road'),
(@city_id, 'MG Road'),
(@city_id, 'Rajwada'),
(@city_id, 'Siyaganj'),
(@city_id, 'YN Road')
ON DUPLICATE KEY UPDATE name = VALUES(name);

DELETE FROM areas
WHERE city_id = @city_id
  AND name = 'Rajendra Nagar'
  AND NOT EXISTS (
    SELECT 1
    FROM hostels h
    WHERE h.area_id = areas.area_id
  );
