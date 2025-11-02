package potato.backend.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InfisicalAuth 단위 테스트
 * 
 * 실제 Infisical CLI가 설치되어 있고 환경 변수가 설정된 경우에만 실행됩니다.
 */
class InfisicalAuthTest {

    @Test
    @DisplayName("CLI 없이 실행 시 예외 발생")
    void testCliNotInstalled() {
        // Given
        String clientId = "invalid-client-id";
        String clientSecret = "invalid-client-secret";
        
        // When & Then
        assertThatThrownBy(() -> InfisicalAuth.loginAndGetToken(clientId, clientSecret))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("잘못된 credentials로 로그인 시 예외 발생")
    @EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_ID", matches = ".+")
    void testInvalidCredentials() {
        // Given
        String clientId = "invalid-client-id";
        String clientSecret = "invalid-client-secret";
        
        // When & Then
        assertThatThrownBy(() -> InfisicalAuth.loginAndGetToken(clientId, clientSecret))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Infisical login failed");
    }

    @Test
    @DisplayName("유효한 credentials로 토큰 획득 성공")
    @EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_ID", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_SECRET", matches = ".+")
    void testSuccessfulLogin() throws Exception {
        // Given
        String clientId = System.getenv("INFISICAL_CLIENT_ID");
        String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
        
        // When
        String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰으로 명령 실행 및 출력 캡처")
    @EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_ID", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "INFISICAL_CLIENT_SECRET", matches = ".+")
    void testRunWithTokenAndCapture() throws Exception {
        // Given
        String clientId = System.getenv("INFISICAL_CLIENT_ID");
        String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
        String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
        
        // When
        String output = InfisicalAuth.runWithTokenAndCapture(token, 
            new String[]{"secrets", "list", "--plain", "--silent"});
        
        // Then
        assertThat(output).isNotNull();
        // 출력 형식은 Infisical CLI 버전에 따라 다를 수 있음
    }

    @Test
    @DisplayName("null credentials로 로그인 시도")
    void testNullCredentials() {
        // When & Then
        assertThatThrownBy(() -> InfisicalAuth.loginAndGetToken(null, null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("빈 문자열 credentials로 로그인 시도")
    void testEmptyCredentials() {
        // When & Then
        assertThatThrownBy(() -> InfisicalAuth.loginAndGetToken("", ""))
                .isInstanceOf(Exception.class);
    }
}
