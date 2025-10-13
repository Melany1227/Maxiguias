package com.maxiguias.maxigestion.maxigestion.servicio;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    public String uploadImage(MultipartFile file, String customName) throws IOException {
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "maxigestion/productos",
            "resource_type", "image",
            "format", "jpg",
            "transformation", ObjectUtils.asMap(
                "width", 800,
                "height", 600,
                "crop", "fill",
                "quality", "auto"
            )
        );

        // Si se especifica un nombre personalizado, usarlo
        if (customName != null && !customName.isEmpty()) {
            uploadParams.put("public_id", customName);
            uploadParams.put("overwrite", true);
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        String publicId = uploadResult.get("public_id").toString();
        String version = uploadResult.get("version").toString();
        // Guardar public_id con versión: publicId#version
        return publicId + "#" + version;
    }

    public String getImageUrl(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }
        
        // Separar public_id y versión si están guardados juntos
        String actualPublicId = publicId;
        String version = null;
        
        if (publicId.contains("#")) {
            String[] parts = publicId.split("#");
            actualPublicId = parts[0];
            version = parts[1];
        }
        
        // Generar URL con versión específica
        if (version != null) {
            return cloudinary.url().secure(true).version(version).generate(actualPublicId);
        } else {
            return cloudinary.url().secure(true).generate(actualPublicId);
        }
    }

    public void deleteImage(String publicId) {
        try {
            // Extraer solo el public_id sin la versión para eliminar
            String actualPublicId = publicId.contains("#") ? publicId.split("#")[0] : publicId;
            cloudinary.uploader().destroy(actualPublicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Error eliminando imagen: " + e.getMessage());
        }
    }

}