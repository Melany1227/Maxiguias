package com.maxiguias.maxigestion.maxigestion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(String destinatario, String nuevaContrasena) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Recuperación de Contraseña - Maxigestion");
            mensaje.setText(construirMensajeRecuperacion(nuevaContrasena));
            
            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado exitosamente a: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo de recuperación: " + e.getMessage());
        }
    }

    private String construirMensajeRecuperacion(String nuevaContrasena) {
        return "Estimado usuario,\n\n" +
               "Hemos recibido una solicitud para restablecer tu contraseña en Maxigestion.\n\n" +
               "Tu contraseña temporal es: " + nuevaContrasena + "\n\n" +
               "IMPORTANTE: Serás redirigido automáticamente al formulario de cambio de contraseña.\n" +
               "Allí debes:\n" +
               "1. Ingresar la contraseña temporal mostrada arriba\n" +
               "2. Crear tu nueva contraseña\n" +
               "3. Confirmar la nueva contraseña\n\n" +
               "Esta contraseña temporal solo es válida para cambiarla por una nueva.\n\n" +
               "Si no solicitaste este cambio, contacta inmediatamente al administrador.\n\n" +
               "Saludos,\n" +
               "Equipo Maxigestion";
    }
}