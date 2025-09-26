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
INSERT INTO perfiles (id_perfil, nombre_perfil, roles_id_rol) VALUES 
(5, 'ADMIN_GENERAL', 1)
ON DUPLICATE KEY UPDATE nombre_perfil = VALUES(nombre_perfil);

-- Insertar formularios del sistema
INSERT INTO formularios (id_formulario, nombre_formulario, url, padre) VALUES 
(1, 'Dashboard', '/', NULL),
(2, 'Usuarios', '/usuarios', NULL),
(3, 'Productos', '/productos', NULL),
(4, 'Órdenes', '/ordenes', NULL),
(5, 'Reportes', '/reportes', NULL)
ON DUPLICATE KEY UPDATE nombre_formulario = VALUES(nombre_formulario);

-- Asignar permisos completos al ADMINISTRADOR (perfil 1)
INSERT INTO formularios_x_perfiles (perfiles_id_perfil, formularios_id_formulario, crear, editar, visualizar, eliminar) 
SELECT 1, id_formulario, 'S', 'S', 'S', 'S' FROM formularios
ON DUPLICATE KEY UPDATE crear = 'S', editar = 'S', visualizar = 'S', eliminar = 'S';

