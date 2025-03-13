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
}
