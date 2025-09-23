--Eliminar FK de usuarios
ALTER TABLE usuarios DROP FOREIGN KEY fk_perfil_usuario;

--Eliminar FK de formularios_x_perfiles
ALTER TABLE formularios_x_perfiles DROP FOREIGN KEY formularios_x_perfiles_ibfk_1;
ALTER TABLE formularios_x_perfiles DROP FOREIGN KEY formularios_x_perfiles_ibfk_3;

--Agregar autoincrement a perfiles
ALTER TABLE perfiles MODIFY COLUMN id_perfil int NOT NULL AUTO_INCREMENT;

--Volver a agregar las FK
ALTER TABLE usuarios ADD CONSTRAINT fk_perfil_usuario;
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);

ALTER TABLE formularios_x_perfiles ADD CONSTRAINT formularios_x_perfiles_ibfk_1
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);

ALTER TABLE formularios_x_perfiles ADD CONSTRAINT formularios_x_perfiles_ibfk_3
	FOREIGN KEY (perfiles_id_perfil) REFERENCES perfiles
    (id_perfil);