-- Script para agregar el campo CORREO a la tabla USUARIOS

-- 1. Agregar columna CORREO a tabla USUARIOS
ALTER TABLE usuarios ADD COLUMN correo VARCHAR(100) NULL;

-- 2. Crear índice único para el correo 
CREATE UNIQUE INDEX idx_usuarios_correo ON USUARIOS(CORREO);