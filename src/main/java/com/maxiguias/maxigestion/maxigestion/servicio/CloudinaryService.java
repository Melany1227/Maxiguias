package com.maxiguias.maxigestion.maxigestion.servicio;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    public String uploadImage(MultipartFile file, String customName) throws IOException {
        // Parámetros de subida
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "MAXYGUIAS/Imagenes-Productos", // carpeta en Cloudinary
                "resource_type", "image",
                "format", "jpg",
                // transformación ajustada: mantiene proporción, optimiza y reduce peso
                "transformation", new Transformation()
                        .width(800)
                        .height(800)
                        .crop("limit") // mantiene proporciones sin deformar
                        .quality("auto")
                        .fetchFormat("auto"));

        // Si se especifica un nombre personalizado
        if (customName != null && !customName.isEmpty()) {
            uploadParams.put("public_id", customName);
            uploadParams.put("overwrite", true);
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        String publicId = uploadResult.get("public_id").toString();
        String version = uploadResult.get("version").toString();

        // Guardar public_id con versión
        return publicId + "#" + version;
    }

    public String getImageUrl(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }

        String actualPublicId = publicId;
        String version = null;

        if (publicId.contains("#")) {
            String[] parts = publicId.split("#");
            actualPublicId = parts[0];
            version = parts[1];
        }

        // 👇 Genera la URL optimizada también para visualización
        if (version != null) {
            return cloudinary.url()
                    .secure(true)
                    .version(version)
                    .transformation(new Transformation()
                            .width(400)
                            .height(400)
                            .crop("fit")
                            .quality("auto")
                            .fetchFormat("auto"))
                    .generate(actualPublicId);
        } else {
            return cloudinary.url()
                    .secure(true)
                    .transformation(new Transformation()
                            .width(400)
                            .height(400)
                            .crop("fit")
                            .quality("auto")
                            .fetchFormat("auto"))
                    .generate(actualPublicId);
        }
    }

    public void deleteImage(String publicId) {
        try {
            String actualPublicId = publicId.contains("#") ? publicId.split("#")[0] : publicId;
            cloudinary.uploader().destroy(actualPublicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Error eliminando imagen: " + e.getMessage());
        }
    }
}
