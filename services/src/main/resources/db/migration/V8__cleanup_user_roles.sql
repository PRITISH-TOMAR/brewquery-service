-- ── MIGRATE role FROM LOOKUP TABLE TO INLINE COLUMN ──────────────────────────
-- Step 1: add the new column (nullable to allow backfill)
ALTER TABLE user_details ADD COLUMN role VARCHAR(20);

-- Step 2: backfill from the existing FK join
UPDATE user_details ud
SET    role = ur.role_name
FROM   user_roles ur
WHERE  ur.role_id = ud.role_id;

-- Step 3: lock it down — NOT NULL, default to USER, valid values only
ALTER TABLE user_details
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN role SET DEFAULT 'USER',
    ADD  CONSTRAINT chk_ud_role CHECK (role IN ('USER', 'ADMIN', 'SUPERADMIN'));

-- Step 4: drop the FK and the old integer column
ALTER TABLE user_details
    DROP CONSTRAINT fk_user_details_role,
    DROP COLUMN role_id;

-- Step 5: drop the now-unused lookup table
DROP TABLE user_roles;
