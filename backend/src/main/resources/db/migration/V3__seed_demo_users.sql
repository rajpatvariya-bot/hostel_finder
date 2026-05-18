-- Demo users for login (password = Demo@123)
-- BCrypt hash generated for "Demo@123"
-- If you change password, update hash accordingly.

SET @pwd = '$2a$10$kG3rYtJxS9tqz.4X9p8mHOVx3yJ1vK8j3xgQv7HcJfPpRzH2xZ7mG';

INSERT INTO users (role, name, email, phone, password_hash, status)
VALUES ('ADMIN', 'Demo Admin', 'admin@demo.com', '9999990000', @pwd, 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

SET @admin_id = (SELECT id FROM users WHERE email='admin@demo.com' LIMIT 1);
INSERT INTO admins (user_id) VALUES (@admin_id) ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO users (role, name, email, phone, password_hash, status)
VALUES ('STUDENT', 'Demo Student', 'student@demo.com', '9999990002', @pwd, 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Ensure demo owner exists and has correct password for login too.
INSERT INTO users (role, name, email, phone, password_hash, status)
VALUES ('OWNER', 'Demo Owner', 'owner@demo.com', '9999990001', @pwd, 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), password_hash=VALUES(password_hash);

SET @owner_id = (SELECT id FROM users WHERE email='owner@demo.com' LIMIT 1);
INSERT INTO hostel_owners (user_id, owner_status, verified_at)
VALUES (@owner_id, 'VERIFIED', NOW())
ON DUPLICATE KEY UPDATE owner_status=VALUES(owner_status);

