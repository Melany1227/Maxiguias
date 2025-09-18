--Eliminar FK de perfiles
ALTER TABLE perfiles DROP FOREIGN KEY perfiles_ibfk_1;

--Agregar autoincrement a roles
ALTER TABLE roles MODIFY COLUMN id_rol int NOT NULL AUTO_INCREMENT;

--Volver a agregar la FK
ALTER TABLE perfiles ADD CONSTRAINT perfiles_ibfk_1
	FOREIGN KEY (roles_id_rol) REFERENCES roles
    (id_rol);