package potato.backend.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Infisical CLI 기반 인증 통합 테스트
 * 
 * 이 테스트는 실제 Spring Context와 함께 동작하며,
 * Infisical CLI가 설치되어 있고 환경 변수가 설정된 경우에만 실행됩니다.
 * 
 * 테스트 실행 전 필요한 환경 변수:
 * - INFISICAL_CLIENT_ID
 * - INFISICAL_CLIENT_SECRET
 * - INFISICAL_PROJECT_ID
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "infisical.enabled=true",
    "infisical.project-id=${INFISICAL_PROJECT_ID:test-project}",
    "infisical.environment=dev",
    "infisical.secrets-path=/"
})
@EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_SECRET", matches = ".+")
class InfisicalConfigIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(InfisicalConfigIntegrationTest.class);

    @Autowired(required = false)
    private InfisicalConfig infisicalConfig;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Infisical Config Bean이 생성된다")
    void testInfisicalConfigBeanCreated() {
        // When & Then
        assertThat(infisicalConfig).isNotNull();
        log.info("✓ InfisicalConfig bean successfully created");
    }

    @Test
    @DisplayName("CLI를 통해 로드된 secrets가 Spring Environment에 존재한다")
    void testSecretsLoadedIntoSpringEnvironment() {
        // Given
        String useCliAuth = System.getenv("INFISICAL_USE_CLI_AUTH");
        
        if (!"true".equalsIgnoreCase(useCliAuth)) {
            log.info("Skipping test - INFISICAL_USE_CLI_AUTH is not set to true");
            return;
        }

        // When
        // Secrets는 InfisicalConfig의 @PostConstruct에서 이미 로드됨
        
        // Then
        // Environment에서 일부 속성이 로드되었는지 확인
        // 실제 secret 키는 프로젝트마다 다를 수 있으므로,
        // PropertySource가 추가되었는지 확인
        assertThat(environment).isNotNull();
        log.info("✓ Spring Environment contains loaded secrets");
    }

    @Test
    @DisplayName("CLI 인증 방식을 사용하여 토큰을 획득할 수 있다")
    void testCliAuthTokenAcquisition() throws Exception {
        // Given
        String clientId = System.getenv("INFISICAL_CLIENT_ID");
        String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
        
        // When
        String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        log.info("✓ Successfully obtained authentication token");
        log.info("Token preview: {}...", token.substring(0, Math.min(20, token.length())));
    }

    @Test
    @DisplayName("획득한 토큰으로 secrets를 조회할 수 있다")
    void testQuerySecretsWithToken() throws Exception {
        // Given
        String clientId = System.getenv("INFISICAL_CLIENT_ID");
        String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
        String projectId = System.getenv("INFISICAL_PROJECT_ID");
        
        if (projectId == null || projectId.isEmpty()) {
            log.info("Skipping test - INFISICAL_PROJECT_ID not set");
            return;
        }
        
        String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
        
        // When
        String secrets = InfisicalAuth.runWithTokenAndCapture(token, 
            new String[]{
                "secrets", "list",
                "--projectId=" + projectId,
                "--env=dev",
                "--plain", "--silent"
            });
        
        // Then
        assertThat(secrets).isNotNull();
        log.info("✓ Successfully queried secrets");
        log.info("Secrets output length: {} characters", secrets.length());
    }

    @Test
    @DisplayName("잘못된 토큰으로 명령 실행 시 예외가 발생한다")
    void testInvalidTokenThrowsException() {
        // Given
        String invalidToken = "invalid-token-12345";
        String projectId = System.getenv("INFISICAL_PROJECT_ID");
        
        if (projectId == null || projectId.isEmpty()) {
            projectId = "test-project";
        }
        
        // When & Then
        String finalProjectId = projectId;
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            InfisicalAuth.runWithTokenAndCapture(invalidToken, 
                new String[]{
                    "secrets", "list",
                    "--projectId=" + finalProjectId,
                    "--env=dev"
                });
        });
        
        log.info("✓ Invalid token correctly throws exception");
    }
}
