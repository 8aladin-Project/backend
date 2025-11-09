package potato.backend.global.config;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import potato.backend.global.security.filter.JwtFilter;
import potato.backend.global.security.jwt.JwtUtil;
import potato.backend.global.security.oauth.CustomAuthorizationRequestRepository;
import potato.backend.global.security.oauth.CustomOAuth2UserService;
import potato.backend.global.security.oauth.CustomSuccessHandler;

import static potato.backend.global.constant.UrlConstant.ALLOWED_CLIENT_URLS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;
    private final JwtUtil jwtUtil;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs.yaml",
                                "/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/v3/api-docs.yaml",
                                "/h2-console/**",  // H2 콘솔 허용
                                "/ws-chat/**",  // WebSocket 엔드포인트 허용
                                "/ws-chat",     // SockJS 엔드포인트 허용
                                "/api/v1/wishlists/**",  // 위시리스트 API 임시 허용
                                "/api/v1/chatrooms/**",  // 채팅 API 임시 허용
                                "/api/v1/chat/**",  // 채팅 API 임시 허용
                                "/api/v1/images/**",  // 이미지 업로드 API 허용
                                "/api/v1/products/**",  // 상품 API 허용
                                "/websocket-test.html",  // WebSocket 테스트 페이지 허용
                                "/fcm-token-test.html",  // FCM 토큰 테스트 페이지 허용
                                "/firebase-messaging-sw.js",  // FCM 테스트용 서비스 워커 허용
                                "/static/**"  // 정적 리소스 허용

                        ).permitAll()
                        .anyRequest().authenticated() // 개발용
                );

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 설정 - ClientRegistrationRepository가 있을 때만 활성화
        if (clientRegistrationRepository.isPresent()) {
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(auth -> auth
                            .authorizationRequestRepository(customAuthorizationRequestRepository))
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                    .successHandler(customSuccessHandler)
            );
        }

        // JWT 필터 추가
        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(ALLOWED_CLIENT_URLS);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
