package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxiguias.maxigestion.maxigestion.modelo.FormularioXPerfil;
import com.maxiguias.maxigestion.maxigestion.modelo.FormularioXPerfilId;

@Repository
public interface FormularioXPerfilRepository extends JpaRepository<FormularioXPerfil, FormularioXPerfilId> {

    @Query("SELECT fxp FROM FormularioXPerfil fxp WHERE fxp.perfil.id = :perfilId")
    List<FormularioXPerfil> findByPerfilId(@Param("perfilId") Long perfilId);

    @Query("SELECT fxp FROM FormularioXPerfil fxp WHERE fxp.perfil.id = :perfilId AND fxp.formulario.url = :url")
    Optional<FormularioXPerfil> findByPerfilIdAndFormularioUrl(@Param("perfilId") Long perfilId, @Param("url") String url);

    @Query("SELECT fxp FROM FormularioXPerfil fxp WHERE fxp.perfil.id = :perfilId AND fxp.formulario.nombreFormulario = :nombreFormulario")
    Optional<FormularioXPerfil> findByPerfilIdAndFormularioNombre(@Param("perfilId") Long perfilId, @Param("nombreFormulario") String nombreFormulario);
    
    @Query("SELECT fxp FROM FormularioXPerfil fxp WHERE fxp.perfil.id = :perfilId AND :url LIKE CONCAT(fxp.formulario.url, '%') ORDER BY LENGTH(fxp.formulario.url) DESC")
    List<FormularioXPerfil> findByPerfilIdAndUrlStartsWith(@Param("perfilId") Long perfilId, @Param("url") String url);
}