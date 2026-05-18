CREATE TABLE otp_verifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  identifier VARCHAR(180) NOT NULL,
  otp_code VARCHAR(255) NOT NULL,
  expiry_time DATETIME NOT NULL,
  is_verified TINYINT(1) NOT NULL DEFAULT 0,
  attempts INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_otp_identifier (identifier)
);
