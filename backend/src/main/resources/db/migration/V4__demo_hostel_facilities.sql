-- Link demo hostels to facilities for filter demo

-- hostel 1: WiFi(1), Laundry(3), Power Backup(6)
INSERT IGNORE INTO hostel_facilities (hostel_id, facility_id) VALUES
(1, 1), (1, 3), (1, 6);

-- hostel 2: WiFi(1), AC(2), Water Purifier(5)
INSERT IGNORE INTO hostel_facilities (hostel_id, facility_id) VALUES
(2, 1), (2, 2), (2, 5);

-- hostel 3: Parking(4)
INSERT IGNORE INTO hostel_facilities (hostel_id, facility_id) VALUES
(3, 4);

