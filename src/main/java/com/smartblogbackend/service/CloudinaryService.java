package com.smartblogbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${CLOUDINARY_URL}") String cloudinaryUrl
    ) {
        this.cloudinary = new Cloudinary(cloudinaryUrl); // ✅ Use Cloudinary URL for configuration
    }

    public String uploadImage(byte[] imageBytes) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString(); // ✅ Returns Cloudinary Image URL
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        // Example: https://res.cloudinary.com/cloudname/image/upload/v12345/folder/id.jpg
        try {
            // Find "upload/"
            String uploadMarker = "/upload/";
            int startIndex = imageUrl.indexOf(uploadMarker);
            if (startIndex == -1) return null;
            
            startIndex += uploadMarker.length();
            
            // Skip version if present (v12345678/)
            if (imageUrl.charAt(startIndex) == 'v') {
                int nextSlash = imageUrl.indexOf('/', startIndex);
                if (nextSlash != -1) {
                    startIndex = nextSlash + 1;
                }
            }
            
            // End before the extension
            int endIndex = imageUrl.lastIndexOf('.');
            if (endIndex == -1 || endIndex <= startIndex) return null;
            
            return imageUrl.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }
}
