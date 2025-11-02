package potato.backend.global.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Infisical SDK Configuration with .env file support
 * 
 * .env 파일에서 Infisical credentials를 읽어서 SDK로 secrets를 가져옵니다.
 * 
 * 사용 방법:
 * 1. .env.example을 .env로 복사: cp .env.example .env
 * 2. .env 파일에 실제 값 입력:
 *    INFISICAL_CLIENT_ID=your-client-id
 *    INFISICAL_CLIENT_SECRET=your-client-secret
 * 3. 애플리케이션 실행: ./gradlew bootRun
 * 
 * Machine Identity 생성 방법:
 * 1. https://app.infisical.com 로그인
 * 2. 프로젝트 선택 > Settings > Access Control > Machine Identities
 * 3. "Create Identity" 클릭 > Universal Auth 선택
 * 4. Client ID와 Client Secret를 .env 파일에 저장
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "infisical.sdk.enabled", havingValue = "true", matchIfMissing = false)
public class InfisicalConfig {

    @Value("${infisical.project-id}")
    private String projectId;

    @Value("${infisical.environment:dev}")
    private String environment;

    @Value("${infisical.secrets-path:/}")
    private String secretsPath;

    private final ConfigurableEnvironment springEnvironment;
    private Dotenv dotenv;

    public InfisicalConfig(ConfigurableEnvironment springEnvironment) {
        this.springEnvironment = springEnvironment;
        
        // .env 파일 로드 (파일이 없으면 무시)
        try {
            this.dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            log.info("✓ Successfully loaded .env file");
        } catch (Exception e) {
            log.debug("No .env file found, using system environment variables");
            this.dotenv = null;
        }
    }

    @PostConstruct
    public void loadSecretsFromInfisical() {
        try {
            log.info("=== Loading secrets from Infisical ===");
            log.info("Project ID: {}", projectId);
            log.info("Environment: {}", environment);
            log.info("Secrets Path: {}", secretsPath);

            // 1. .env 파일 또는 환경 변수에서 credentials 가져오기
            String clientId = getEnvValue("INFISICAL_CLIENT_ID");
            String clientSecret = getEnvValue("INFISICAL_CLIENT_SECRET");

            if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
                log.error("❌ Infisical credentials not found!");
                log.error("");
                log.error("Please set credentials in .env file:");
                log.error("  1. Copy example file: cp .env.example .env");
                log.error("  2. Edit .env file and set:");
                log.error("     INFISICAL_CLIENT_ID=your-client-id");
                log.error("     INFISICAL_CLIENT_SECRET=your-client-secret");
                log.error("  3. Run application: ./gradlew bootRun");
                log.error("");
                log.error("Get credentials from: https://app.infisical.com");
                log.error("");
                throw new IllegalStateException("Infisical credentials not configured");
            }

            // 2. Custom API URL 확인 (개인 호스팅 서버용)
            String apiUrl = getEnvValue("INFISICAL_API_URL");
            if (apiUrl != null && !apiUrl.isEmpty()) {
                log.info("Using custom Infisical API URL: {}", apiUrl);
            }

            // 3. 환경에 따라 CLI/SDK 방식 자동 선택
            // dev 환경: CLI 사용 (개발 편의성)
            // prod 환경: SDK 사용 (안정성)
            // 명시적으로 INFISICAL_USE_CLI_AUTH 설정 시 해당 값 우선
            boolean useCliAuth = shouldUseCliAuth(environment);
            
            if (useCliAuth) {
                log.info("🔧 Using CLI-based authentication for {} environment", environment);
                loadFromCliAuth(clientId, clientSecret);
            } else {
                log.info("🔒 Using SDK-based authentication for {} environment", environment);
                loadFromUniversalAuth(clientId, clientSecret, apiUrl);
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (InfisicalException e) {
            log.error("❌ Infisical API error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch secrets from Infisical", e);
        } catch (Exception e) {
            log.error("❌ Failed to load secrets from Infisical: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Infisical configuration", e);
        }
    }

    /**
     * .env 파일 또는 시스템 환경 변수에서 값 가져오기
     * 우선순위: .env 파일 > 시스템 환경 변수
     */
    private String getEnvValue(String key) {
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return System.getenv(key);
    }

    /**
     * 환경에 따라 CLI/SDK 방식 사용 여부 결정
     * 
     * 우선순위:
     * 1. INFISICAL_USE_CLI_AUTH 환경 변수가 명시적으로 설정된 경우 해당 값 사용
     * 2. prod/staging 환경: CLI 사용 (ProcessBuilder 기반, 직접 제어)
     * 3. dev/test 환경: SDK 사용 (안정성, 개발 편의성)
     * 
     * @param environment 현재 활성 환경 (dev, test, staging, prod 등)
     * @return true면 CLI 사용, false면 SDK 사용
     */
    private boolean shouldUseCliAuth(String environment) {
        // 1. 명시적으로 설정된 경우 해당 값 우선
        String explicitSetting = getEnvValue("INFISICAL_USE_CLI_AUTH");
        if (explicitSetting != null && !explicitSetting.isEmpty()) {
            boolean useCli = "true".equalsIgnoreCase(explicitSetting);
            log.info("INFISICAL_USE_CLI_AUTH explicitly set to: {}", useCli);
            return useCli;
        }
        
        // 2. 환경에 따라 자동 선택
        // prod/staging 환경에서 CLI 사용
        boolean useCli = "prod".equalsIgnoreCase(environment) || 
                        "staging".equalsIgnoreCase(environment);
        
        log.info("Auto-selecting authentication method for '{}' environment: {}", 
                environment, useCli ? "CLI" : "SDK");
        
        return useCli;
    }

    /**
     * CLI 기반 인증을 사용하여 secrets 로드
     */
    private void loadFromCliAuth(String clientId, String clientSecret) throws Exception {
        log.info("Using CLI-based authentication with ProcessBuilder...");
        
        // 1. 토큰 획득
        String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
        log.info("✓ Successfully obtained authentication token");
        
        // 2. secrets 가져오기
        String[] command = new String[]{
            "secrets", "list",
            "--projectId=" + projectId,
            "--env=" + environment,
            "--path=" + secretsPath,
            "--plain",
            "--silent"
        };
        
        String output = InfisicalAuth.runWithTokenAndCapture(token, command);
        log.info("✓ Retrieved secrets from Infisical CLI");
        
        // 3. 출력 파싱 및 Spring Environment에 추가
        parseAndLoadSecrets(output);
    }

    /**
     * CLI 출력을 파싱하여 Spring Environment에 추가
     */
    private void parseAndLoadSecrets(String output) {
        Map<String, Object> infisicalProperties = new HashMap<>();
        
        // CLI 출력 형식 파싱 (KEY=VALUE 형식을 가정)
        String[] lines = output.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            int equalsIndex = line.indexOf('=');
            if (equalsIndex > 0) {
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();
                infisicalProperties.put(key, value);
                log.debug("Loaded secret: {}", key);
            }
        }
        
        if (!infisicalProperties.isEmpty()) {
            MapPropertySource infisicalPropertySource = new MapPropertySource(
                    "infisical-cli", infisicalProperties);
            springEnvironment.getPropertySources().addFirst(infisicalPropertySource);
            log.info("✅ Loaded {} secrets from Infisical CLI to Spring Environment", 
                    infisicalProperties.size());
        }
    }

        /**
     * Universal Auth (Machine Identity)를 사용하여 secrets 로드
     */
    private void loadFromUniversalAuth(String clientId, String clientSecret, String apiUrl) throws InfisicalException {
        // Create SDK with custom site URL if provided
        InfisicalSdk sdk;
        if (apiUrl != null && !apiUrl.isEmpty()) {
            log.info("Using custom Infisical API URL: {}", apiUrl);
            sdk = new InfisicalSdk(
                    new SdkConfig.Builder()
                            .withSiteUrl(apiUrl)
                            .build()
            );
        } else {
            log.info("Using default Infisical API URL");
            sdk = new InfisicalSdk(
                    new SdkConfig.Builder().build()
            );
        }

        // Authenticate with Universal Auth
        sdk.Auth().UniversalAuthLogin(clientId, clientSecret);
        log.info("✓ Successfully authenticated with Infisical");

        // Fetch secrets
        List<Secret> secrets = sdk.Secrets().ListSecrets(
                projectId,
                environment,
                secretsPath,
                true,   // expandSecretReferences
                false,  // recursive
                false,  // includeImports
                false   // setSecretsOnSystemProperties
        );

        log.info("✓ Retrieved {} secrets from Infisical SDK", secrets.size());

        Map<String, Object> infisicalProperties = new HashMap<>();
        for (Secret secret : secrets) {
            String key = secret.getSecretKey();
            String value = secret.getSecretValue();
            infisicalProperties.put(key, value);
            log.debug("Loaded secret: {}", key);
        }

        if (!infisicalProperties.isEmpty()) {
            MapPropertySource infisicalPropertySource = new MapPropertySource(
                    "infisical-sdk", infisicalProperties);
            springEnvironment.getPropertySources().addFirst(infisicalPropertySource);
            log.info("✅ Infisical secrets added to Spring Environment");
        }
    }
}
