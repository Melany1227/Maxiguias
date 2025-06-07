package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.PersonaInfo;
import com.maxiguias.maxigestion.maxigestion.modelo.TipoUsuario;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarUsuarios() {
        try {
            System.out.println("Intentando obtener usuarios con consulta nativa...");
            List<Object[]> resultados = usuarioRepository.obtenerUsuariosConDatos();
            System.out.println("Resultados obtenidos: " + resultados.size());
            return convertirObjectArrayAUsuarios(resultados);
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Usuario> convertirObjectArrayAUsuarios(List<Object[]> resultados) {
        List<Usuario> usuarios = new ArrayList<>();

        for (Object[] fila : resultados) {
            try {
                System.out.println("Procesando fila usuario: " + java.util.Arrays.toString(fila));

                Long documento = fila[0] != null ? Long.valueOf(fila[0].toString()) : null;
                String primerApellido = fila[1] != null ? fila[1].toString() : null;
                String segundoApellido = fila[2] != null ? fila[2].toString() : null;
                String usuario = fila[3] != null ? fila[3].toString() : null;
                String contrasena = fila[4] != null ? fila[4].toString() : null;
                Integer tipoUsuarioId = fila[5] != null ? Integer.valueOf(fila[5].toString()) : null;
                String nombreTU = fila[10] != null ? fila[10].toString() : null;

                TipoUsuario tipoUsuario = null;
                if (tipoUsuarioId != null) {
                    tipoUsuario = new TipoUsuario();
                    tipoUsuario.setId(tipoUsuarioId);
                    tipoUsuario.setNombre(nombreTU);
                }

                Integer perfilesIdPerfil = fila[6] != null ? Integer.valueOf(fila[6].toString()) : null;
                String nombre = fila[7] != null ? fila[7].toString() : null;
                String direccion = fila[8] != null ? fila[8].toString() : null;
                String telefono = fila[9] != null ? fila[9].toString() : null;

                PersonaInfo infoPersona = new PersonaInfo(nombre, direccion, telefono);
                Usuario usuarioObj = new Usuario(documento, primerApellido, segundoApellido,
                                               usuario, contrasena, tipoUsuario,
                                               perfilesIdPerfil, infoPersona);
                usuarios.add(usuarioObj);

                System.out.println("Usuario creado: " + documento + " - " + nombre);

            } catch (Exception e) {
                System.err.println("Error al convertir fila de usuario: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return usuarios;
    }
}