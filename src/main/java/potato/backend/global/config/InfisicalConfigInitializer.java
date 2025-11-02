package potato.backend.global.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Infisical SDK ApplicationContext Initializer
 * 
 * ApplicationContextInitializer로 등록되어 DataSource 초기화보다 먼저 실행됩니다.
 */
@Slf4j
public class InfisicalConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private Dotenv dotenv;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // .env 파일 로드
        try {
            this.dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            log.info("✓ Successfully loaded .env file");
        } catch (Exception e) {
            log.debug("No .env file found, using system environment variables");
            this.dotenv = null;
        }

        try {
            log.info("=== Loading secrets from Infisical ===");
            
            String projectId = environment.getProperty("infisical.project-id");
            String infisicalEnv = environment.getProperty("infisical.environment", "dev");
            String secretsPath = environment.getProperty("infisical.secrets-path", "/");
            
            log.info("Project ID: {}", projectId);
            log.info("Environment: {}", infisicalEnv);
            log.info("Secrets Path: {}", secretsPath);

            // CLI 확인
            boolean useCliAuth = Boolean.parseBoolean(getEnvValue("INFISICAL_USE_CLI_AUTH", "true"));
            boolean cliAvailable = isInfisicalCliAvailable();

            if (useCliAuth && cliAvailable) {
                log.info("✓ Infisical CLI is available");
                log.info("Using CLI-based authentication");
                loadFromCli(projectId, infisicalEnv, secretsPath, environment);
            } else {
                if (useCliAuth && !cliAvailable) {
                    log.info("✗ Infisical CLI is not available");
                }
                log.info("Using SDK-based authentication");
                loadFromUniversalAuth(projectId, infisicalEnv, secretsPath, environment);
            }
        } catch (Exception e) {
            log.error("Failed to load secrets from Infisical: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Infisical", e);
        }
    }

    private boolean isInfisicalCliAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"infisical", "--version"});
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadFromCli(String projectId, String infisicalEnv, String secretsPath, ConfigurableEnvironment springEnvironment) {
        try {
            log.info("Using CLI-based authentication with export command...");
            
            String clientId = getEnvValue("INFISICAL_CLIENT_ID");
            String clientSecret = getEnvValue("INFISICAL_CLIENT_SECRET");
            String apiUrl = getEnvValue("INFISICAL_API_URL");
            
            // infisical export를 사용하여 secrets 가져오기
            List<String> commandList = new ArrayList<>();
            commandList.add("infisical");
            commandList.add("export");
            commandList.add("--format=dotenv");
            commandList.add("--projectId=" + projectId);
            commandList.add("--env=" + infisicalEnv);
            commandList.add("--path=" + secretsPath);
            
            if (apiUrl != null && !apiUrl.isEmpty()) {
                commandList.add("--domain=" + apiUrl);
                log.info("Using custom Infisical API URL: {}", apiUrl);
            }
            
            // Universal Auth credentials 설정
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.environment().put("INFISICAL_UNIVERSAL_AUTH_CLIENT_ID", clientId);
            pb.environment().put("INFISICAL_UNIVERSAL_AUTH_CLIENT_SECRET", clientSecret);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = br.lines().collect(Collectors.joining("\n")).trim();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    log.error("Infisical export failed with exit code: {}", exitCode);
                    log.error("Output: {}", output);
                    throw new RuntimeException("Infisical export failed with exit code " + exitCode);
                }
                
                log.info("✓ Retrieved secrets from Infisical CLI");
                
                // 출력 파싱 및 Spring Environment에 추가
                Map<String, Object> secretsMap = new HashMap<>();
                String[] lines = output.split("\n");
                int secretCount = 0;
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();
                        
                        // 따옴표 제거
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        secretsMap.put(key, value);
                        secretCount++;
                    }
                }
                
                springEnvironment.getPropertySources().addFirst(
                        new MapPropertySource("infisical-secrets", secretsMap)
                );
                
                log.info("✓ Successfully loaded {} secrets into Spring Environment", secretCount);
            }
        } catch (Exception e) {
            log.error("Failed to load from CLI: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void loadFromUniversalAuth(String projectId, String infisicalEnv, String secretsPath, ConfigurableEnvironment springEnvironment) throws InfisicalException {
        // INFISICAL_UNIVERSAL_AUTH_* 먼저 확인, 없으면 INFISICAL_* 확인 (호환성)
        String clientId = getEnvValue("INFISICAL_UNIVERSAL_AUTH_CLIENT_ID");
        if (clientId == null || clientId.isEmpty()) {
            clientId = getEnvValue("INFISICAL_CLIENT_ID");
        }
        
        String clientSecret = getEnvValue("INFISICAL_UNIVERSAL_AUTH_CLIENT_SECRET");
        if (clientSecret == null || clientSecret.isEmpty()) {
            clientSecret = getEnvValue("INFISICAL_CLIENT_SECRET");
        }
        
        String apiUrl = getEnvValue("INFISICAL_API_URL");

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            log.error("❌ Infisical credentials not found!");
            log.error("Please set INFISICAL_UNIVERSAL_AUTH_CLIENT_ID and INFISICAL_UNIVERSAL_AUTH_CLIENT_SECRET");
            log.error("Or INFISICAL_CLIENT_ID and INFISICAL_CLIENT_SECRET");
            return;
        }

        // Create SDK with custom API URL if provided
        InfisicalSdk sdk;
        if (apiUrl != null && !apiUrl.isEmpty()) {
            log.info("Using custom Infisical API URL: {}", apiUrl);
            sdk = new InfisicalSdk(new SdkConfig.Builder().withSiteUrl(apiUrl).build());
        } else {
            log.info("Using default Infisical API URL");
            sdk = new InfisicalSdk(new SdkConfig.Builder().build());
        }
        
        // Authenticate
        sdk.Auth().UniversalAuthLogin(clientId, clientSecret);
        log.info("✓ Successfully authenticated with Infisical");

        List<Secret> secrets = sdk.Secrets().ListSecrets(
                projectId,
                infisicalEnv,
                secretsPath,
                true,   // expandSecretReferences
                false,  // recursive
                false,  // includeImports
                false   // setSecretsOnSystemProperties
        );

        log.info("✓ Retrieved {} secrets from Infisical", secrets.size());

        Map<String, Object> secretsMap = new HashMap<>();
        for (Secret secret : secrets) {
            secretsMap.put(secret.getSecretKey(), secret.getSecretValue());
        }

        springEnvironment.getPropertySources().addFirst(
                new MapPropertySource("infisical-secrets", secretsMap)
        );

        log.info("✓ Successfully loaded {} secrets into Spring Environment", secrets.size());
    }

    private String getEnvValue(String key) {
        return getEnvValue(key, null);
    }

    private String getEnvValue(String key, String defaultValue) {
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null) {
                return value;
            }
        }
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
