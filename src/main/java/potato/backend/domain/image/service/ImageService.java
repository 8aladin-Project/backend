package potato.backend.domain.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.dto.ImageResponse;
import potato.backend.domain.image.exception.ImageNotFoundException;
import potato.backend.domain.image.repository.ImageRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.storage.service.S3Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    
    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;
    
    /**
     * 이미지 업로드 및 저장
     */
    @Transactional
    public List<ImageResponse> uploadImages(Long productId, List<MultipartFile> files) {
        log.info("이미지 업로드 시작 - productId: {}, 파일 개수: {}", productId, files.size());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        
        // S3에 파일 업로드
        log.debug("S3에 {} 개의 파일 업로드 시작", files.size());
        List<String> imageUrls = s3Service.uploadFiles(files);
        log.debug("S3 업로드 완료 - URLs: {}", imageUrls);
        
        // DB에 이미지 정보 저장
        List<Image> images = imageUrls.stream()
                .map(url -> {
                    Image image = Image.create(product, url);
                    return imageRepository.save(image);
                })
                .toList();
        
        log.info("이미지 업로드 완료 - productId: {}, 저장된 이미지 개수: {}", productId, images.size());
        return ImageResponse.fromList(images);
    }
    
    /**
     * 단일 이미지 업로드
     */
    @Transactional
    public ImageResponse uploadImage(Long productId, MultipartFile file) {
        log.info("단일 이미지 업로드 시작 - productId: {}, 파일명: {}", productId, file.getOriginalFilename());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        
        log.debug("S3에 파일 업로드 시작 - 파일명: {}", file.getOriginalFilename());
        String imageUrl = s3Service.uploadFile(file);
        log.debug("S3 업로드 완료 - imageUrl: {}", imageUrl);
        
        Image image = Image.create(product, imageUrl);
        Image savedImage = imageRepository.save(image);
        
        log.info("단일 이미지 업로드 완료 - productId: {}, imageId: {}, imageUrl: {}", 
                productId, savedImage.getId(), savedImage.getImageUrl());
        return ImageResponse.from(savedImage);
    }
    
    /**
     * 상품의 모든 이미지 조회
     */
    public List<ImageResponse> getImagesByProduct(Long productId) {
        log.info("상품 이미지 조회 시작 - productId: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        
        List<Image> images = imageRepository.findByProducts(product);
        log.info("상품 이미지 조회 완료 - productId: {}, 이미지 개수: {}", productId, images.size());
        return ImageResponse.fromList(images);
    }
    
    /**
     * 이미지 삭제
     */
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("이미지 삭제 시작 - imageId: {}", imageId);
        
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));
        
        String imageUrl = image.getImageUrl();
        log.debug("S3에서 파일 삭제 시작 - imageUrl: {}", imageUrl);
        
        // S3에서 파일 삭제
        s3Service.deleteFile(imageUrl);
        log.debug("S3 파일 삭제 완료 - imageUrl: {}", imageUrl);
        
        // DB에서 이미지 정보 삭제
        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - imageId: {}, imageUrl: {}", imageId, imageUrl);
    }
    
    /**
     * 상품의 모든 이미지 삭제
     */
    @Transactional
    public void deleteAllImagesByProduct(Long productId) {
        log.info("상품의 모든 이미지 삭제 시작 - productId: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        
        List<Image> images = imageRepository.findByProducts(product);
        
        if (!images.isEmpty()) {
            log.debug("삭제할 이미지 개수: {}", images.size());
            
            // S3에서 파일들 삭제
            List<String> imageUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
            log.debug("S3에서 {} 개의 파일 삭제 시작", imageUrls.size());
            s3Service.deleteFiles(imageUrls);
            log.debug("S3 파일 삭제 완료");
            
            // DB에서 이미지 정보 삭제
            imageRepository.deleteByProducts(product);
            log.info("상품의 모든 이미지 삭제 완료 - productId: {}, 삭제된 이미지 개수: {}", 
                    productId, images.size());
        } else {
            log.info("삭제할 이미지가 없음 - productId: {}", productId);
        }
    }
    
    /**
     * 이미지 정보 조회
     */
    public ImageResponse getImage(Long imageId) {
        log.info("이미지 정보 조회 시작 - imageId: {}", imageId);
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));
        log.info("이미지 정보 조회 완료 - imageId: {}, imageUrl: {}", imageId, image.getImageUrl());
        return ImageResponse.from(image);
    }
}

