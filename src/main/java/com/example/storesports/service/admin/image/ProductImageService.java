package com.example.storesports.service.admin.image;

import com.example.storesports.core.admin.image.payload.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    List<Long> saveProductImage(List<MultipartFile> images);
    ProductImageResponse getPictureById(Long id);



}
