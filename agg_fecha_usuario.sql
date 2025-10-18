ALTER TABLE usuarios ADD COLUMN fecha_registro DATE;
UPDATE usuarios SET fecha_registro = '2025-03-16' WHERE documento = 10119372;


-- 1. Eliminar la foreign key actual hacia productos
ALTER TABLE detalle_ordenes DROP FOREIGN KEY fk_detalle_factura_producto;

-- 2. Eliminar la columna auto_incremental
ALTER TABLE detalle_ordenes DROP PRIMARY KEY,
                            DROP COLUMN id_detalle_factura;

-- 3. Renombrar id_producto -> id_terminado (o bien crear uno nuevo)
ALTER TABLE detalle_ordenes CHANGE COLUMN id_producto id_terminado INT NOT NULL;
SELECT * FROM  detalle_ordenes;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM detalle_ordenes;

-- 4. Crear la nueva clave primaria compuesta
ALTER TABLE detalle_ordenes ADD PRIMARY KEY (id_factura, id_terminado);

-- 5. Agregar la nueva foreign key hacia terminados
ALTER TABLE detalle_ordenes 
  ADD CONSTRAINT fk_detalle_ordenes_terminado 
  FOREIGN KEY (id_terminado) REFERENCES terminados (id_terminado);