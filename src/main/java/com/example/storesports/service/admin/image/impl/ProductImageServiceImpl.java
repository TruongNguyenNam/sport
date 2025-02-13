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

//    @Autowired
//    public ProductImageServiceImpl(ProductImageRepository productImageRepository, CloudinaryService cloudinaryService, ModelMapper modelMapper) {
//        this.productImageRepository = productImageRepository;
//        this.cloudinaryService = cloudinaryService;
//        this.modelMapper = modelMapper;
//    }
    @Override
    @Transactional
    public List<Long> saveProductImage(List<MultipartFile> images) {
        // Tạo một danh sách để lưu ID ảnh
        List<Long> imageIds = new ArrayList<>();

        for (MultipartFile image : images) {
            try {
                // Tải ảnh lên Cloudinary
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(image, "product_images");

                // Lấy URL ảnh từ Cloudinary
                String imageUrl = (String) uploadResult.get("url");

                // Tạo đối tượng ProductImage và lưu vào cơ sở dữ liệu
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);

                // Lưu vào CSDL
                ProductImage savedImage = productImageRepository.save(productImage);

                // Thêm ID của ảnh đã lưu vào danh sách
                imageIds.add(savedImage.getId());
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        // Trả về danh sách ID ảnh đã lưu
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
        Optional<ProductImage> productImage = productImageRepository.findById(id);
        if (productImage.isPresent()) {
            productImageRepository.deleteById(id);
        } else {
            throw new ErrorException("ProductImage with id " + id + " is not found");
        }
    }


}
