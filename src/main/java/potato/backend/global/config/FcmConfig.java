package potato.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (serviceAccountJson == null || serviceAccountJson.isEmpty()) {
                    log.warn("Firebase 서비스 계정 정보가 없습니다. FCM 기능이 비활성화됩니다.");
                    return;
                }

                // 환경 변수에서 가져온 값의 앞뒤 따옴표 제거
                String jsonValue = serviceAccountJson.trim();
                
                // 앞뒤 따옴표 제거 (양쪽 모두 또는 한쪽만 있을 수 있음)
                if (jsonValue.length() >= 2 && jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
                    jsonValue = jsonValue.substring(1, jsonValue.length() - 1);
                } else if (jsonValue.length() >= 2 && jsonValue.startsWith("'") && jsonValue.endsWith("'")) {
                    jsonValue = jsonValue.substring(1, jsonValue.length() - 1);
                } else if (jsonValue.startsWith("\"")) {
                    jsonValue = jsonValue.substring(1);
                } else if (jsonValue.startsWith("'")) {
                    jsonValue = jsonValue.substring(1);
                } else if (jsonValue.endsWith("\"")) {
                    jsonValue = jsonValue.substring(0, jsonValue.length() - 1);
                } else if (jsonValue.endsWith("'")) {
                    jsonValue = jsonValue.substring(0, jsonValue.length() - 1);
                }

                ByteArrayInputStream stream = new ByteArrayInputStream(
                    jsonValue.getBytes(StandardCharsets.UTF_8)
                );
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화 완료 (project: aladin-f19fc)");
            } else {
                log.info("Firebase가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 실패", e);
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase가 초기화되지 않았습니다. FirebaseMessaging Bean을 생성할 수 없습니다.");
            throw new IllegalStateException("Firebase가 초기화되지 않았습니다. FIREBASE_SERVICE_ACCOUNT_JSON 환경 변수를 확인하세요.");
        }
        
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        log.info("FirebaseMessaging Bean 등록 완료");
        return messaging;
    }
}

