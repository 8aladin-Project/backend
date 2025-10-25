package potato.backend.domain.storage.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.image.exception.ImageUploadException;
import potato.backend.domain.image.exception.InvalidImageException;
import potato.backend.global.util.FileNameUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3 파일 업로드/삭제 서비스
 * 서버를 통해 직접 파일을 업로드/삭제하는 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class S3Service {
    
    private final S3Client s3Client;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static}")
    private String region;
    
    @Value("${cloud.aws.s3.endpoint:#{null}}")
    private String endpoint;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * 단일 파일 업로드
     * 
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = FileNameUtils.uniqueName(originalFilename);
        String key = "images/" + uniqueFileName;
        
        try {
        // debug logging: request details
        log.debug("Preparing to upload file to S3 - bucket: {}, key: {}, size: {}, contentType: {}", 
            bucketName, key, file.getSize(), file.getContentType());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(
                    putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            
            String fileUrl = generateFileUrl(key);
            log.info("File uploaded successfully - key: {}, url: {}", key, fileUrl);
            return fileUrl;
            
        } catch (S3Exception e) {
            String errorMsg = null;
            try {
                if (e.awsErrorDetails() != null) {
                    errorMsg = e.awsErrorDetails().errorMessage();
                }
            } catch (Exception ignore) {
            }
            if (errorMsg == null) {
                errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            }
            log.error("S3 upload failed for file: {} - awsErrorDetails: {}, statusCode: {}, exception: ",
                    originalFilename,
                    e.awsErrorDetails(),
                    e.statusCode(),
                    e);
            throw new ImageUploadException("S3 업로드 실패: " + errorMsg, e);
        } catch (IOException e) {
            log.error("Failed to read file: {}", originalFilename, e);
            throw new ImageUploadException("파일 읽기 실패: " + originalFilename, e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", originalFilename, e);
            throw new ImageUploadException("파일 업로드 중 예상치 못한 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 디버그용: S3에 연결하여 버킷 리스트를 반환합니다.
     */
    public String listBucketsForDebug() {
        try {
            var response = s3Client.listBuckets();
            var names = response.buckets().stream()
                    .map(b -> b.name())
                    .toList();
            log.info("S3 buckets: {}", names);
            return "Buckets: " + names.toString();
        } catch (S3Exception e) {
            String msg = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("listBuckets failed: {}", msg, e);
            return "listBuckets failed: " + (msg != null ? msg : e.toString());
        } catch (Exception e) {
            log.error("Unexpected error listing buckets", e);
            return "Unexpected error: " + e.toString();
        }
    }
    
    /**
     * 여러 파일 업로드
     * 
     * @param files 업로드할 파일 목록
     * @return 업로드된 파일들의 URL 목록
     */
    public List<String> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidImageException("업로드할 파일이 없습니다");
        }
        
        log.info("Starting batch upload for {} files", files.size());
        return files.stream()
                .map(this::uploadFile)
                .toList();
    }
    
    /**
     * 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully - key: {}, url: {}", key, fileUrl);
            
        } catch (S3Exception e) {
            String err = null;
            try {
                if (e.awsErrorDetails() != null) err = e.awsErrorDetails().errorMessage();
            } catch (Exception ignore) {
            }
            if (err == null) err = e.getMessage() != null ? e.getMessage() : e.toString();
            log.error("S3 delete failed for url: {} - {}", fileUrl, err, e);
            throw new ImageUploadException("S3 삭제 실패: " + err, e);
        }
    }
    
    /**
     * 여러 파일 삭제
     * 
     * @param fileUrls 삭제할 파일들의 URL 목록
     */
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            log.warn("No files to delete");
            return;
        }
        
        log.info("Starting batch deletion for {} files", fileUrls.size());
        fileUrls.forEach(this::deleteFile);
    }
    
    /**
     * 파일 존재 여부 확인
     * 
     * @param fileUrl 확인할 파일의 URL
     * @return 파일 존재 여부
     */
    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            log.debug("File does not exist: {}", fileUrl);
            return false;
        } catch (S3Exception e) {
            log.error("Failed to check file existence: {}", fileUrl, e);
            return false;
        }
    }
    
    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("파일이 비어있습니다");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageException(
                    String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB, 현재 크기: %.2fMB", 
                            MAX_FILE_SIZE / 1024 / 1024,
                            file.getSize() / 1024.0 / 1024.0)
            );
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageException(
                    "지원하지 않는 파일 형식입니다. 지원 형식: " + String.join(", ", ALLOWED_CONTENT_TYPES)
            );
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidImageException("파일명이 유효하지 않습니다");
        }
    }
    
    /**
     * S3 URL 생성
     */
    private String generateFileUrl(String key) {
        // MinIO나 로컬 endpoint 사용 시
        if (endpoint != null && !endpoint.isEmpty()) {
            String trimmedEndpoint = endpoint.endsWith("/") 
                    ? endpoint.substring(0, endpoint.length() - 1) 
                    : endpoint;
            return String.format("%s/%s/%s", trimmedEndpoint, bucketName, key);
        }
        
        // AWS S3 사용 시
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
    
    /**
     * URL에서 S3 key 추출
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new InvalidImageException("파일 URL이 비어있습니다");
        }
        
        // S3 버킷명 이후의 key 추출
        if (fileUrl.contains(bucketName + "/")) {
            String[] parts = fileUrl.split(bucketName + "/");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        
        // path-style URL인 경우 (MinIO 등)
        if (fileUrl.contains("/" + bucketName + "/")) {
            int bucketIndex = fileUrl.indexOf("/" + bucketName + "/");
            return fileUrl.substring(bucketIndex + bucketName.length() + 2);
        }
        
        throw new InvalidImageException("잘못된 S3 URL 형식입니다: " + fileUrl);
    }
}
