package potato.backend.global.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .env 파일을 로드하고 Infisical SDK로 secrets를 가져와 Spring Environment에 추가하는 Initializer
 * 
 * ApplicationContext 초기화 전에 실행되어:
 * 1. .env 파일의 환경 변수를 Spring Boot Environment에 추가
 * 2. Infisical SDK로 secrets를 가져와서 추가
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 1. .env 파일 로드
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // .env 파일의 모든 항목을 Map으로 변환
            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                dotenvMap.put(entry.getKey(), entry.getValue());
            });

            if (!dotenvMap.isEmpty()) {
                // Spring Environment에 PropertySource로 추가
                ConfigurableEnvironment environment = applicationContext.getEnvironment();
                MapPropertySource dotenvPropertySource = new MapPropertySource("dotenv", dotenvMap);
                environment.getPropertySources().addFirst(dotenvPropertySource);
                
                System.out.println("✓ Loaded " + dotenvMap.size() + " properties from .env file");
                
                // 2. Infisical이 활성화되어 있으면 secrets 가져오기
                String infisicalEnabled = dotenv.get("INFISICAL_ENABLED", "true");
                if ("true".equalsIgnoreCase(infisicalEnabled)) {
                    loadInfisicalSecrets(dotenv, environment);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadInfisicalSecrets(Dotenv dotenv, ConfigurableEnvironment environment) {
        try {
            String projectId = dotenv.get("INFISICAL_PROJECT_ID");
            String clientId = dotenv.get("INFISICAL_CLIENT_ID");
            String clientSecret = dotenv.get("INFISICAL_CLIENT_SECRET");
            String envName = dotenv.get("INFISICAL_ENVIRONMENT", "dev");
            String secretsPath = dotenv.get("INFISICAL_SECRETS_PATH", "/");
            String apiUrl = dotenv.get("INFISICAL_API_URL");

            if (clientId == null || clientSecret == null) {
                System.out.println("⚠ Infisical credentials not found in .env file");
                return;
            }

            System.out.println("=== Loading secrets from Infisical ===");
            System.out.println("Project ID: " + projectId);
            System.out.println("Environment: " + envName);
            System.out.println("Secrets Path: " + secretsPath);

            // Create SDK with custom site URL if provided
            InfisicalSdk sdk;
            if (apiUrl != null && !apiUrl.isEmpty()) {
                System.out.println("✓ Using custom Infisical API URL: " + apiUrl);
                sdk = new InfisicalSdk(
                        new SdkConfig.Builder()
                                .withSiteUrl(apiUrl)
                                .build()
                );
            } else {
                System.out.println("Using default Infisical API URL");
                sdk = new InfisicalSdk(
                        new SdkConfig.Builder().build()
                );
            }

            // Authenticate with Universal Auth
            sdk.Auth().UniversalAuthLogin(clientId, clientSecret);
            System.out.println("✓ Successfully authenticated with Infisical");

            // Fetch secrets
            List<Secret> secrets = sdk.Secrets().ListSecrets(
                    projectId,
                    envName,
                    secretsPath,
                    true,   // expandSecretReferences
                    false,  // recursive
                    false,  // includeImports
                    false   // setSecretsOnSystemProperties
            );

            System.out.println("✓ Successfully fetched " + secrets.size() + " secrets from Infisical");

            // Add secrets to Spring environment
            Map<String, Object> infisicalProperties = new HashMap<>();
            for (Secret secret : secrets) {
                String key = secret.getSecretKey();
                String value = secret.getSecretValue();
                infisicalProperties.put(key, value);
                System.out.println("  - " + key);
            }

            environment.getPropertySources().addFirst(
                    new MapPropertySource("infisicalProperties", infisicalProperties)
            );

            System.out.println("✓ Added Infisical secrets to Spring Environment");
            System.out.println("===========================================\n");

        } catch (Exception e) {
            System.err.println("❌ Failed to load secrets from Infisical: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
