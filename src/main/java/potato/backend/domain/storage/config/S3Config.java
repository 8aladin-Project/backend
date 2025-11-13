package potato.backend.domain.storage.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "cloud.aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class S3Config {
    @Value("${cloud.aws.s3.endpoint:#{null}}")
    private String endpoint;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.path-style:false}")
    private boolean pathStyle;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        log.info("=== S3Client 초기화 시작 ===");
        log.info("Region: {}", region);
        log.info("Path Style: {}", pathStyle);
        log.info("Endpoint: {}", endpoint != null ? endpoint : "AWS Default (S3)");
        log.info("Bucket: {}", bucketName);
        log.info("Access Key: {}***", accessKey != null && accessKey.length() > 4 
                ? accessKey.substring(0, 4) : "null");
        
        if (accessKey == null || accessKey.isEmpty() || accessKey.equals("your-access-key")) {
            log.error("AWS Access Key is not configured properly: '{}'", accessKey);
            throw new IllegalStateException("AWS_ACCESS_KEY 환경 변수가 Infisical에서 로드되지 않았습니다. Infisical dev 환경에 AWS_ACCESS_KEY를 설정해주세요.");
        }
        
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("your-secret-key")) {
            log.error("AWS Secret Key is not configured properly");
            throw new IllegalStateException("AWS_SECRET_KEY 환경 변수가 Infisical에서 로드되지 않았습니다. Infisical dev 환경에 AWS_SECRET_KEY를 설정해주세요.");
        }
        
        if (bucketName == null || bucketName.isEmpty() || bucketName.equals("your-bucket-name")) {
            log.error("S3 Bucket name is not configured properly: '{}'", bucketName);
            throw new IllegalStateException("AWS_S3_BUCKET 환경 변수가 Infisical에서 로드되지 않았습니다. Infisical dev 환경에 AWS_S3_BUCKET을 설정해주세요.");
        }
        
        // endpoint가 null이거나 비어있으면 AWS S3를 사용
        // endpoint가 설정되어 있으면 MinIO 등 로컬 S3를 사용
        if (endpoint != null && !endpoint.isEmpty()) {
            log.warn("⚠️  Custom endpoint 사용 중: {}", endpoint);
            log.warn("⚠️  로컬 S3(MinIO 등)를 사용하는 경우 해당 서비스가 실행 중인지 확인하세요!");
            log.warn("⚠️  502 에러가 발생하면 endpoint 주소를 확인하세요: {}", endpoint);
        }
        
        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build());
        
        // endpoint가 설정되어 있으면 (로컬 MinIO 등) override
        if (endpoint != null && !endpoint.isEmpty()) {
            try {
                URI endpointUri = URI.create(endpoint);
                builder.endpointOverride(endpointUri);
                log.info("✓ Custom S3 endpoint 설정 완료: {}", endpoint);
            } catch (Exception e) {
                log.error("❌ Invalid endpoint URI: {}", endpoint, e);
                throw new IllegalStateException("잘못된 S3 endpoint 형식: " + endpoint, e);
            }
        }
        
        log.info("✓ S3Client 초기화 완료 - bucket: {}", bucketName);
        log.info("=== S3Client 초기화 종료 ===");
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        log.info("Initializing S3Presigner");
        
        var builder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region));
        
        // endpoint가 설정되어 있으면 (로컬 MinIO 등) override
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        return builder.build();
    }

    @Bean
    public S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyle) // http://host/bucket/key 형태
                .build();
    }
}
