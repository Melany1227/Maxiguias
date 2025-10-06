ALTER TABLE usuarios ADD COLUMN fecha_registro DATE;
UPDATE usuarios SET fecha_registro = '2025-03-16' WHERE documento = 10119372;