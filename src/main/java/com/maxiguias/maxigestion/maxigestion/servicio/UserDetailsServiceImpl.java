package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Obtener el nombre del rol real desde la BD
        String nombreRol = usuario.getPerfil().getRol().getNombreRol();

        System.out.println("Usuario autenticado: " + usuario.getNombreUsuario() + " con rol: " + nombreRol);

        // Asignar ese rol como autoridad sin anteponer "ROLE_"
        return new User(
                usuario.getNombreUsuario(),
                usuario.getContrasena(),
                Collections.singletonList(new SimpleGrantedAuthority(nombreRol)));
    }
}
