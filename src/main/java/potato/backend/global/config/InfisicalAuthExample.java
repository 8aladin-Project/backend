package potato.backend.global.config;

import lombok.extern.slf4j.Slf4j;

/**
 * Infisical CLI 기반 인증 사용 예시
 * 
 * 이 클래스는 InfisicalAuth를 사용하는 다양한 예시를 제공합니다.
 * 
 * 사용 전 준비사항:
 * 1. Infisical CLI 설치
 *    - macOS: brew install infisical/get-cli/infisical
 *    - Linux: https://infisical.com/docs/cli/overview
 * 
 * 2. 환경 변수 설정
 *    export INFISICAL_CLIENT_ID=your-client-id
 *    export INFISICAL_CLIENT_SECRET=your-client-secret
 * 
 * 3. (선택) .env 파일 생성
 *    INFISICAL_CLIENT_ID=your-client-id
 *    INFISICAL_CLIENT_SECRET=your-client-secret
 */
@Slf4j
public class InfisicalAuthExample {

    /**
     * 예시 1: 기본 로그인 및 토큰 획득
     */
    public static void example1_BasicLogin() {
        try {
            log.info("=== Example 1: Basic Login ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            
            if (clientId == null || clientSecret == null) {
                log.error("Please set INFISICAL_CLIENT_ID and INFISICAL_CLIENT_SECRET");
                return;
            }
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            log.info("Successfully obtained token: {}", token.substring(0, 20) + "...");
            
        } catch (Exception e) {
            log.error("Failed to login: {}", e.getMessage(), e);
        }
    }

    /**
     * 예시 2: 특정 Secret 조회
     */
    public static void example2_GetSpecificSecret() {
        try {
            log.info("=== Example 2: Get Specific Secret ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            
            // 특정 secret 조회
            String secretName = "DATABASE_URL";
            String secretValue = InfisicalAuth.runWithTokenAndCapture(token, 
                new String[]{"secrets", "get", secretName, "--plain", "--silent"});
            
            log.info("Secret {}: {}", secretName, secretValue);
            
        } catch (Exception e) {
            log.error("Failed to get secret: {}", e.getMessage(), e);
        }
    }

    /**
     * 예시 3: 모든 Secrets 목록 조회
     */
    public static void example3_ListAllSecrets() {
        try {
            log.info("=== Example 3: List All Secrets ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            String projectId = System.getenv("INFISICAL_PROJECT_ID");
            
            if (projectId == null) {
                log.error("Please set INFISICAL_PROJECT_ID");
                return;
            }
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            
            // 모든 secrets 목록 조회
            String secrets = InfisicalAuth.runWithTokenAndCapture(token, 
                new String[]{
                    "secrets", "list",
                    "--projectId=" + projectId,
                    "--env=dev",
                    "--plain", "--silent"
                });
            
            log.info("All secrets:\n{}", secrets);
            
        } catch (Exception e) {
            log.error("Failed to list secrets: {}", e.getMessage(), e);
        }
    }

    /**
     * 예시 4: 특정 환경(Environment)의 Secrets 조회
     */
    public static void example4_GetSecretsForEnvironment() {
        try {
            log.info("=== Example 4: Get Secrets for Specific Environment ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            String projectId = System.getenv("INFISICAL_PROJECT_ID");
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            
            // Production 환경의 secrets 조회
            String prodSecrets = InfisicalAuth.runWithTokenAndCapture(token, 
                new String[]{
                    "secrets", "list",
                    "--projectId=" + projectId,
                    "--env=prod",
                    "--path=/",
                    "--plain", "--silent"
                });
            
            log.info("Production secrets:\n{}", prodSecrets);
            
        } catch (Exception e) {
            log.error("Failed to get environment secrets: {}", e.getMessage(), e);
        }
    }

    /**
     * 예시 5: 표준 출력으로 명령 실행 (inheritIO 사용)
     */
    public static void example5_RunCommandWithInheritIO() {
        try {
            log.info("=== Example 5: Run Command with InheritIO ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            String projectId = System.getenv("INFISICAL_PROJECT_ID");
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            
            // 표준 출력으로 직접 출력
            InfisicalAuth.runWithToken(token, 
                new String[]{
                    "secrets", "list",
                    "--projectId=" + projectId,
                    "--env=dev"
                });
            
            log.info("Command executed successfully");
            
        } catch (Exception e) {
            log.error("Failed to run command: {}", e.getMessage(), e);
        }
    }

    /**
     * 예시 6: Secret 값으로 환경 변수 설정
     */
    public static void example6_SetEnvironmentVariableFromSecret() {
        try {
            log.info("=== Example 6: Set Environment Variable from Secret ===");
            
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            
            String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
            
            // Secret 값 조회
            String apiKey = InfisicalAuth.runWithTokenAndCapture(token, 
                new String[]{"secrets", "get", "API_KEY", "--plain", "--silent"});
            
            // 환경 변수로 설정 (현재 프로세스 내에서만 유효)
            System.setProperty("app.api.key", apiKey);
            
            log.info("API Key set as system property");
            log.info("Retrieved value: {}", System.getProperty("app.api.key"));
            
        } catch (Exception e) {
            log.error("Failed to set environment variable: {}", e.getMessage(), e);
        }
    }

    /**
     * 모든 예시 실행
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("Infisical CLI Authentication Examples");
        log.info("========================================\n");
        
        example1_BasicLogin();
        System.out.println();
        
        example2_GetSpecificSecret();
        System.out.println();
        
        example3_ListAllSecrets();
        System.out.println();
        
        example4_GetSecretsForEnvironment();
        System.out.println();
        
        example5_RunCommandWithInheritIO();
        System.out.println();
        
        example6_SetEnvironmentVariableFromSecret();
        
        log.info("\n========================================");
        log.info("All examples completed!");
        log.info("========================================");
    }
}
