-- Drop old constraint
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS chk_storage_type;

-- Add new constraint with uppercase values
ALTER TABLE attachments 
ADD CONSTRAINT chk_storage_type 
CHECK (storage_type IN ('S3', 'LOCAL'));