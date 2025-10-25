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
        log.info("Initializing S3Client with region: {}, pathStyle: {}, endpoint: {}", 
                region, pathStyle, endpoint != null ? endpoint : "AWS Default");
        
        if (accessKey == null || accessKey.isEmpty() || accessKey.equals("your-access-key")) {
            log.error("AWS Access Key is not configured properly");
            throw new IllegalStateException("AWS_ACCESS_KEY 환경 변수가 설정되지 않았습니다.");
        }
        
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("your-secret-key")) {
            log.error("AWS Secret Key is not configured properly");
            throw new IllegalStateException("AWS_SECRET_KEY 환경 변수가 설정되지 않았습니다.");
        }
        
        if (bucketName == null || bucketName.isEmpty() || bucketName.equals("your-bucket-name")) {
            log.error("S3 Bucket name is not configured properly");
            throw new IllegalStateException("AWS_S3_BUCKET 환경 변수가 설정되지 않았습니다.");
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
            builder.endpointOverride(URI.create(endpoint));
            log.info("Using custom S3 endpoint: {}", endpoint);
        }
        
        log.info("S3Client initialized successfully for bucket: {}", bucketName);
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
