package com.example.storesports.service.admin.image.impl;
import com.example.storesports.core.admin.image.payload.ProductImageResponse;
import com.example.storesports.entity.ProductImage;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.exceptions.NotFoundException;
import com.example.storesports.repositories.ProductImageRepository;
import com.example.storesports.service.admin.image.ProductImageService;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public List<Long> saveProductImage(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("No images provided to upload");
        }

        List<Long> imageIds = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(image, "product_images");
                String imageUrl = (String) uploadResult.get("url");

                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                ProductImage savedImage = productImageRepository.save(productImage);
                imageIds.add(savedImage.getId());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image: " + image.getOriginalFilename(), e);
            }
        }

        if (imageIds.isEmpty()) {
            throw new IllegalStateException("No images were successfully uploaded");
        }
        return imageIds;
    }


    @Override
    public ProductImageResponse getPictureById(Long id) {
        ProductImage productImage = productImageRepository.findById(id).
                orElseThrow(() -> new NotFoundException("productImage is not found"));
        return modelMapper.map(productImage,ProductImageResponse.class);
    }

    @Override
    public void deleteById(Long id) {
        if (!productImageRepository.existsById(id)) {
            throw new ErrorException("ProductImage with id " + id + " is not found");
        }
        productImageRepository.deleteById(id);
    }

    @Override
    public void deleteByProductId(Long id) {
        if (!productImageRepository.existsByProductId(id)) {
            throw new ErrorException("No ProductImages found for Product with id " + id);
        }
        productImageRepository.deleteByProductId(id);
    }


}
