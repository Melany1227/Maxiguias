package com.maxiguias.maxigestion.maxigestion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(String destinatario, String nuevaContrasena) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Recuperación de Contraseña - Maxigestion");
            mensaje.setText(construirMensajeRecuperacion(nuevaContrasena));

            mailSender.send(mensaje);
            logger.info("✅ Correo enviado exitosamente a: " + destinatario);

        } catch (MailAuthenticationException e) {
            logger.severe("❌ Error de Autenticación SMTP: " + e.getMessage());
            logger.severe("Verifica: usuario, contraseña y credenciales de aplicación de Gmail");
            throw new RuntimeException("Error de autenticación SMTP. Verifica credenciales de Gmail.", e);
        } catch (MailSendException e) {
            logger.severe("❌ Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo de recuperación: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("❌ Error inesperado: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar correo de recuperación: " + e.getMessage(), e);
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