-- Ensure every OWNER user has a matching hostel_owners row

INSERT INTO hostel_owners (user_id, owner_status, verified_at)
SELECT u.id, 'VERIFIED', NOW()
FROM users u
LEFT JOIN hostel_owners ho ON ho.user_id = u.id
WHERE u.role = 'OWNER'
  AND ho.user_id IS NULL;
