-- Rename sql_modes_available to modes_available and allow NULL
ALTER TABLE datasets RENAME COLUMN sql_modes_available TO modes_available;
ALTER TABLE datasets ALTER COLUMN modes_available DROP NOT NULL;
ALTER TABLE datasets ALTER COLUMN modes_available SET DEFAULT NULL;
