package potato.backend.domain.image.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.image.dto.ImageResponse;
import potato.backend.domain.image.service.ImageService;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 관리 API")
public class ImageController {
    
    private final ImageService imageService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "상품에 대한 이미지를 업로드합니다.")
    public ResponseEntity<List<ImageResponse>> uploadImages(
            @Parameter(description = "상품 ID") @RequestParam Long productId,
            @Parameter(description = "업로드할 이미지 파일들") @RequestParam("images") List<MultipartFile> images
    ) {
        log.info("이미지 다중 업로드 요청 - 이미지 개수: {}", images.size());
        List<ImageResponse> responses = imageService.uploadImages( images);
        log.info("이미지 다중 업로드 완료 - 업로드된 이미지 개수: {}", responses.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @PostMapping(value = "/upload/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "단일 이미지 업로드", description = "상품에 대한 단일 이미지를 업로드합니다.")
    public ResponseEntity<ImageResponse> uploadImage(
            @Parameter(description = "업로드할 이미지 파일") @RequestParam("image") MultipartFile image
    ) {
        log.info("이미지 단일 업로드 요청 - 파일명: {}, 파일 크기: {} bytes",
                image.getOriginalFilename(), image.getSize());
        ImageResponse response = imageService.uploadImage(image);
        log.info("이미지 단일 업로드 완료 - imageId: {}, imageUrl: {}",
                response.id(), response.imageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "상품 이미지 조회", description = "특정 상품의 모든 이미지를 조회합니다.")
    public ResponseEntity<List<ImageResponse>> getImagesByProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId
    ) {
        log.info("상품 이미지 조회 요청 - productId: {}", productId);
        List<ImageResponse> responses = imageService.getImagesByProduct(productId);
        log.info("상품 이미지 조회 완료 - productId: {}, 이미지 개수: {}", productId, responses.size());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{imageId}")
    @Operation(summary = "이미지 정보 조회", description = "특정 이미지의 정보를 조회합니다.")
    public ResponseEntity<ImageResponse> getImage(
            @Parameter(description = "이미지 ID") @PathVariable Long imageId
    ) {
        log.info("이미지 정보 조회 요청 - imageId: {}", imageId);
        ImageResponse response = imageService.getImage(imageId);
        log.info("이미지 정보 조회 완료 - imageId: {}, imageUrl: {}", imageId, response.imageUrl());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{imageId}")
    @Operation(summary = "이미지 삭제", description = "특정 이미지를 삭제합니다.")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "이미지 ID") @PathVariable Long imageId
    ) {
        log.info("이미지 삭제 요청 - imageId: {}", imageId);
        imageService.deleteImage(imageId);
        log.info("이미지 삭제 완료 - imageId: {}", imageId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/product/{productId}")
    @Operation(summary = "상품의 모든 이미지 삭제", description = "특정 상품의 모든 이미지를 삭제합니다.")
    public ResponseEntity<Void> deleteAllImagesByProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId
    ) {
        log.info("상품의 모든 이미지 삭제 요청 - productId: {}", productId);
        imageService.deleteAllImagesByProduct(productId);
        log.info("상품의 모든 이미지 삭제 완료 - productId: {}", productId);
        return ResponseEntity.noContent().build();
    }
}
