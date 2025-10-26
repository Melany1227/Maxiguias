-- Eliminar FK de usuarios
ALTER TABLE usuarios DROP FOREIGN KEY fk_perfil_usuario;

-- Eliminar FK de formularios_x_perfiles
ALTER TABLE formularios_x_perfiles DROP FOREIGN KEY formularios_x_perfiles_ibfk_1;
ALTER TABLE formularios_x_perfiles DROP FOREIGN KEY formularios_x_perfiles_ibfk_3;

-- Agregar autoincrement a perfiles
ALTER TABLE perfiles MODIFY COLUMN id_perfil int NOT NULL AUTO_INCREMENT;

-- Volver a agregar las FK
ALTER TABLE usuarios ADD CONSTRAINT fk_perfil_usuario;
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);

ALTER TABLE formularios_x_perfiles ADD CONSTRAINT formularios_x_perfiles_ibfk_1
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);

ALTER TABLE formularios_x_perfiles ADD CONSTRAINT formularios_x_perfiles_ibfk_3
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);

-- Eliminar FK de perfiles
ALTER TABLE perfiles DROP FOREIGN KEY perfiles_ibfk_1;

-- Agregar autoincrement a roles
ALTER TABLE roles MODIFY COLUMN id_rol int NOT NULL AUTO_INCREMENT;

-- Volver a agregar la FK
ALTER TABLE perfiles ADD CONSTRAINT perfiles_ibfk_1
	FOREIGN KEY (roles_id_rol) REFERENCES roles
    (id_rol);    

RENAME TABLE facturas TO ordenes;

RENAME TABLE detalle_facturas TO detalle_ordenes;

CREATE TABLE departamento (
    id_departamento INT PRIMARY KEY,
    nombre_departamento VARCHAR(100) NOT NULL
);

ALTER TABLE ciudades
ADD COLUMN id_departamento INT;

ALTER TABLE ciudades
ADD CONSTRAINT fk_ciudades_departamento
FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento);

-- Insertar departamentos
INSERT INTO departamento (id_departamento, nombre_departamento)
VALUES (1, 'Antioquia'),
       (2, 'Cundinamarca');

SET SQL_SAFE_UPDATES = 0;

-- Insertar ciudades y asignarles departamento
UPDATE ciudades SET id_departamento = 1 WHERE nombre_ciudad IN ('Medellín', 'Envigado');
UPDATE ciudades SET id_departamento = 2 WHERE nombre_ciudad = 'Bogotá';


ALTER TABLE terminados
ADD COLUMN ganancia_x_mayor DECIMAL(5,2) NULL AFTER precio_x_mayor,
ADD COLUMN ganancia_x_encargo DECIMAL(5,2) NULL AFTER precio_x_encargo;
ALTER TABLE productos MODIFY COLUMN cantidad_disponible INT NULL;

ALTER TABLE usuarios ADD COLUMN id_ciudad INT;

UPDATE usuarios SET id_ciudad = 1  WHERE id_ciudad IS NULL;

ALTER TABLE usuarios MODIFY COLUMN id_ciudad INT NOT NULL;

ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_ciudades FOREIGN KEY (id_ciudad) REFERENCES ciudades(id_ciudad);

ALTER TABLE ordenes DROP COLUMN lugar_venta;



-- Insertar perfiles básicos
INSERT INTO `maxigestion_db`.`roles` (`nombre_rol`) VALUES ('ADMINISTRADOR');
INSERT INTO `maxigestion_db`.`roles` (`nombre_rol`) VALUES ('JURIDICO');
INSERT INTO `maxigestion_db`.`perfiles` (`nombre_perfil`, `roles_id_rol`) VALUES ('ADMIN_GENERAL', (SELECT id_rol FROM roles WHERE nombre_rol = "ADMINISTRADOR"));
INSERT INTO `maxigestion_db`.`perfiles` (`nombre_perfil`, `roles_id_rol`) VALUES ('DEVELOPER', (SELECT id_rol FROM roles WHERE nombre_rol = "ADMINISTRADOR"));
INSERT INTO `maxigestion_db`.`perfiles` (`nombre_perfil`, `roles_id_rol`) VALUES ('ALMACEN', (SELECT id_rol FROM roles WHERE nombre_rol = "JURIDICO"));


-- Insertar formularios del sistema
INSERT INTO formularios (id_formulario, nombre_formulario, url, padre) VALUES 
(1, 'Dashboard', '/', NULL),
(2, 'Usuarios', '/usuarios', NULL),
(3, 'Productos', '/productos', NULL),
(4, 'Órdenes', '/ordenes', NULL),
(5, 'Reportes', '/reportes', NULL)
ON DUPLICATE KEY UPDATE nombre_formulario = VALUES(nombre_formulario);

-- Asignar permisos completos al ADMINISTRADOR (perfil 1)
INSERT INTO formularios (id_formulario, nombre_formulario, url, padre) VALUES
(1, 'Dashboard', '/', NULL),
(2, 'Usuarios', '/usuarios', NULL),
(3, 'Productos', '/productos', NULL),
(4, 'Órdenes', '/ordenes', NULL),
(5, 'Reportes', '/reportes', NULL),
(6, 'Crear_Usuarios', '/usuarios/crear', NULL),
(7, 'Crear_Ordenes', '/ordenes/nueva', NULL),
(8, 'Buscar_Usuario_Ordenes', '/ordenes/buscar-usuarios', NULL),
(9, 'Crear_Ordenes', '/ordenes/guardar', NULL),
(10, 'Ver_Ordenes', '/ordenes/verMas', NULL);

INSERT INTO formularios_x_perfiles (perfiles_id_perfil, formularios_id_formulario, crear, editar, visualizar, eliminar) VALUES
(1, 1, 'S', 'S', 'S', 'S'),
(1, 2, 'S', 'S', 'S', 'S'),
(1, 3, 'S', 'S', 'S', 'S'),
(1, 4, 'S', 'S', 'S', 'S'),
(1, 5, 'S', 'S', 'S', 'S'),
(1, 6, 'S', 'S', 'S', 'S'),
(1, 7, 'S', 'S', 'S', 'S'),
(1, 8, 'S', 'S', 'S', 'S'),
(1, 9, 'S', 'S', 'S', 'S'),
(1, 10, 'S', 'S', 'S', 'S');

ALTER TABLE usuarios ADD COLUMN fecha_registro DATE;
UPDATE usuarios SET fecha_registro = '2025-03-16' WHERE documento = 10119372;

ALTER TABLE ordenes ADD COLUMN estado VARCHAR(20) DEFAULT 'PENDIENTE' NOT NULL;
ALTER TABLE ordenes RENAME COLUMN fecha_venta TO fecha_entrega;
ALTER TABLE ordenes MODIFY fecha_entrega DATETIME;
ALTER TABLE ordenes ADD COLUMN fecha_orden DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Cambios en detalle_ordenes para usar clave primaria compuesta y referenciar terminados

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


-- Cambio en el tipo de dato de fecha_registro en usuarios
ALTER TABLE usuarios MODIFY COLUMN fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP;

