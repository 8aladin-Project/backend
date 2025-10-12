package potato.backend.global.security.jwt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;

import lombok.extern.slf4j.Slf4j;

import potato.backend.domain.user.domain.Role;
import potato.backend.global.security.oauth.UserInfo;

import static potato.backend.global.constant.SecurityConstant.ACCESS_TOKEN_EXPIRATION_SECONDS;
import static potato.backend.global.constant.SecurityConstant.REFRESH_TOKEN_EXPIRATION_SECONDS;

@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_OAUTH_ID = "oauthId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_EMAIL = "email";

    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.private-key}")
    private String privateKeyString;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = generatePrivateKey(privateKeyString);
            this.publicKey = generatePublicKey(publicKeyString);
        } catch (Exception e) {
            throw new RuntimeException("키 초기화에 실패했습니다.", e);
        }
    }

    public String generateAccessToken(UserInfo userInfo) {
        Instant issuedAt = Instant.now();
        Instant expiredAt = issuedAt.plus(ACCESS_TOKEN_EXPIRATION_SECONDS, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userInfo.memberId().toString())
                .claim(CLAIM_OAUTH_ID, userInfo.oauthId())
                .claim(CLAIM_ROLE, userInfo.role().name())
                .claim(CLAIM_NAME, userInfo.name())
                .claim(CLAIM_EMAIL, userInfo.email())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiredAt))
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    public String generateRefreshToken(UserInfo userInfo) {
        Instant issuedAt = Instant.now();
        Instant expiredAt = issuedAt.plus(REFRESH_TOKEN_EXPIRATION_SECONDS, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userInfo.memberId().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiredAt))
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(publicKey).build().parse(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("[JwtUtil][validateToken] JWT 검증 실패: token={}, message={}", token, e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("[JwtUtil][validateToken] JWT 만료: token={}, message={}", token, e.getMessage());
        } catch (JwtException e) {
            log.warn("[JwtUtil][validateToken] JWT 파싱 실패: token={}, message={}", token, e.getMessage());
        } catch (Exception e) {
            log.warn("[JwtUtil][validateToken] JWT 처리 중 예외 발생: token={}, message={}", token, e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("[JwtUtil][getClaims] 만료된 JWT 클레임 추출: token={}, message={}", token, e.getMessage());
            return e.getClaims();
        } catch (Exception e) {
            log.warn("[JwtUtil][getClaims] JWT 처리 실패: token={}, message={}", token, e.getMessage());
            throw new RuntimeException("JWT 처리 실패", e);
        }
    }

    public Authentication getAuthentication(String accessToken) {
        try {
            Claims claims = getClaims(accessToken);

            Long memberId = Long.parseLong(claims.getSubject());
            String oauthId = claims.get(CLAIM_OAUTH_ID, String.class);
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            String name = claims.get(CLAIM_NAME, String.class);
            String email = claims.get(CLAIM_EMAIL, String.class);

            Collection<? extends GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority(role.getKey()));

            UserInfo principal = UserInfo.of(memberId, oauthId, role, name, email);

            return new UsernamePasswordAuthenticationToken(principal, null, authorities);
        } catch (Exception e) {
            log.error(
                    "[JwtUtil][getAuthentication] JWT 인증 정보 추출 실패: token={}, message={}", accessToken, e.getMessage());
            return null;
        }
    }

    private PrivateKey generatePrivateKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey generatePublicKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(keySpec);
    }
}
