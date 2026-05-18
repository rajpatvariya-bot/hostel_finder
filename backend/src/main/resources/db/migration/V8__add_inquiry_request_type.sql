SET @request_type_column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inquiries'
    AND COLUMN_NAME = 'request_type'
);

SET @request_type_sql := IF(
  @request_type_column_exists = 0,
  'ALTER TABLE inquiries ADD COLUMN request_type VARCHAR(20) NOT NULL DEFAULT ''INQUIRY'' AFTER message',
  'SELECT 1'
);

PREPARE request_type_stmt FROM @request_type_sql;
EXECUTE request_type_stmt;
DEALLOCATE PREPARE request_type_stmt;
