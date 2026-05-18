-- Hostel Finder (College Project) - Initial Schema + seed data
-- MySQL 8+

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role ENUM('ADMIN','OWNER','STUDENT') NOT NULL,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  gender ENUM('MALE','FEMALE','OTHER') NULL,
  age INT NULL,
  address VARCHAR(255) NULL,
  status ENUM('ACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_role (role),
  KEY idx_users_status (status)
);

CREATE TABLE admins (
  user_id BIGINT PRIMARY KEY,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE hostel_owners (
  user_id BIGINT PRIMARY KEY,
  owner_status ENUM('PENDING_VERIFICATION','VERIFIED','REJECTED','BLOCKED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
  verified_at DATETIME NULL,
  rejection_reason VARCHAR(400) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_owners_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE owner_documents (
  document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  doc_type ENUM('AADHAR','PAN','DRIVING_LICENSE','OTHER') NOT NULL,
  file_url VARCHAR(600) NOT NULL,
  file_mime VARCHAR(100) NULL,
  file_size_bytes BIGINT NULL,
  admin_status ENUM('UPLOADED','APPROVED','REJECTED') NOT NULL DEFAULT 'UPLOADED',
  admin_note VARCHAR(400) NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_owner_docs_owner FOREIGN KEY (owner_user_id) REFERENCES hostel_owners(user_id) ON DELETE CASCADE,
  KEY idx_owner_docs_owner_status (owner_user_id, admin_status)
);

CREATE TABLE verification_reviews (
  review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  admin_user_id BIGINT NOT NULL,
  decision ENUM('APPROVE','REJECT') NOT NULL,
  note VARCHAR(400) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_owner FOREIGN KEY (owner_user_id) REFERENCES hostel_owners(user_id),
  CONSTRAINT fk_review_admin FOREIGN KEY (admin_user_id) REFERENCES admins(user_id)
);

CREATE TABLE cities (
  city_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  UNIQUE KEY uk_cities_name (name)
);

CREATE TABLE areas (
  area_id INT PRIMARY KEY AUTO_INCREMENT,
  city_id INT NOT NULL,
  name VARCHAR(80) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_areas_city FOREIGN KEY (city_id) REFERENCES cities(city_id) ON DELETE CASCADE,
  UNIQUE KEY uk_areas_city_name (city_id, name)
);

CREATE TABLE facilities (
  facility_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(60) NOT NULL,
  UNIQUE KEY uk_facilities_name (name)
);

CREATE TABLE hostels (
  hostel_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  city_id INT NOT NULL,
  area_id INT NOT NULL,
  hostel_name VARCHAR(140) NOT NULL,
  address_line VARCHAR(255) NOT NULL,
  price_per_month DECIMAL(10,2) NOT NULL,
  gender_type ENUM('BOYS','GIRLS','COED') NOT NULL,
  mess_available TINYINT(1) NOT NULL DEFAULT 0,
  description TEXT NULL,
  status ENUM('DRAFT','PUBLISHED','BLOCKED') NOT NULL DEFAULT 'DRAFT',
  total_rooms INT NOT NULL DEFAULT 0,
  available_rooms INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_hostels_owner FOREIGN KEY (owner_user_id) REFERENCES hostel_owners(user_id),
  CONSTRAINT fk_hostels_city FOREIGN KEY (city_id) REFERENCES cities(city_id),
  CONSTRAINT fk_hostels_area FOREIGN KEY (area_id) REFERENCES areas(area_id),
  KEY idx_hostels_city_area (city_id, area_id),
  KEY idx_hostels_gender (gender_type),
  KEY idx_hostels_status (status),
  KEY idx_hostels_price (price_per_month),
  KEY idx_hostels_available (available_rooms)
);

CREATE TABLE hostel_facilities (
  hostel_id BIGINT NOT NULL,
  facility_id INT NOT NULL,
  PRIMARY KEY (hostel_id, facility_id),
  CONSTRAINT fk_hf_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(hostel_id) ON DELETE CASCADE,
  CONSTRAINT fk_hf_facility FOREIGN KEY (facility_id) REFERENCES facilities(facility_id) ON DELETE CASCADE
);

CREATE TABLE hostel_images (
  image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hostel_id BIGINT NOT NULL,
  file_url VARCHAR(600) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_images_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(hostel_id) ON DELETE CASCADE
);

CREATE TABLE rooms (
  room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hostel_id BIGINT NOT NULL,
  room_number VARCHAR(30) NOT NULL,
  is_available TINYINT(1) NOT NULL DEFAULT 1,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rooms_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(hostel_id) ON DELETE CASCADE,
  UNIQUE KEY uk_rooms_hostel_room (hostel_id, room_number),
  KEY idx_rooms_avail (hostel_id, is_available, is_active)
);

CREATE TABLE inquiries (
  inquiry_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hostel_id BIGINT NOT NULL,
  student_user_id BIGINT NOT NULL,
  requested_room_count INT NOT NULL DEFAULT 1,
  message TEXT NULL,
  status ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  owner_note VARCHAR(400) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inquiries_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(hostel_id) ON DELETE CASCADE,
  CONSTRAINT fk_inquiries_student FOREIGN KEY (student_user_id) REFERENCES users(id) ON DELETE CASCADE,
  KEY idx_inquiries_hostel_status (hostel_id, status),
  KEY idx_inquiries_student_status (student_user_id, status)
);

CREATE TABLE inquiry_room_reservations (
  reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inquiry_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  reserved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_res_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiries(inquiry_id) ON DELETE CASCADE,
  CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
  UNIQUE KEY uk_res_room (room_id)
);

-- Seed: city + areas (Indore)
INSERT INTO cities (name) VALUES ('Indore');
SET @indore_id = LAST_INSERT_ID();

INSERT INTO areas (city_id, name) VALUES
(@indore_id, 'Bhanwarkuan'),
(@indore_id, 'Vishnupuri'),
(@indore_id, 'Indrapuri'),
(@indore_id, 'Rau'),
(@indore_id, 'Khandwa Naka'),
(@indore_id, 'Geeta Bhawan'),
(@indore_id, 'Navlakha'),
(@indore_id, 'Bengali Square'),
(@indore_id, 'Pipliyahana'),
(@indore_id, 'Silicon City'),
(@indore_id, 'Vijay Nagar'),
(@indore_id, 'Scheme 54'),
(@indore_id, 'Scheme 78'),
(@indore_id, 'New Palasia'),
(@indore_id, 'Old Palasia'),
(@indore_id, 'Saket Nagar'),
(@indore_id, 'Nipania'),
(@indore_id, 'Super Corridor'),
(@indore_id, 'Musakhedi'),
(@indore_id, 'Pardeshipura'),
(@indore_id, 'Malwa Mill'),
(@indore_id, 'Azad Nagar'),
(@indore_id, 'Sudama Nagar'),
(@indore_id, 'Gumasta Nagar'),
(@indore_id, 'Chhawani'),
(@indore_id, 'Tukoganj'),
(@indore_id, 'Race Course Road'),
(@indore_id, 'MG Road'),
(@indore_id, 'Rajwada'),
(@indore_id, 'Siyaganj'),
(@indore_id, 'YN Road');

-- Seed: facilities
INSERT INTO facilities (name) VALUES
('WiFi'), ('AC'), ('Laundry'), ('Parking'), ('Water Purifier'), ('Power Backup');

