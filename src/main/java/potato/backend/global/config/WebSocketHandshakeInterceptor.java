package potato.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.global.security.oauth.UserInfo;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 인터셉터
 * WebSocket 연결 시점에 memberId를 쿼리 파라미터로 받아 사용자 정보를 Principal로 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final MemberRepository memberRepository;

    /**
     * 핸드셰이크 전에 실행
     * 쿼리 파라미터에서 memberId를 받아 Member를 조회하고 UserInfo를 Principal로 설정합니다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                     WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String path = request.getURI().getPath();
        
        // SockJS의 info 엔드포인트는 허용 (쿼리 파라미터 없이도 통과)
        if (path != null && path.endsWith("/info")) {
            log.debug("SockJS info 엔드포인트 요청 허용: {}", path);
            return true;
        }
        
        // 쿼리 파라미터에서 memberId 추출
        String queryString = request.getURI().getQuery();
        Long memberId = null;
        
        if (queryString != null && queryString.contains("memberId=")) {
            String[] params = queryString.split("&");
            for (String param : params) {
                if (param.startsWith("memberId=")) {
                    String memberIdStr = param.substring(9); // "memberId=" 제거
                    memberIdStr = java.net.URLDecoder.decode(memberIdStr, java.nio.charset.StandardCharsets.UTF_8);
                    try {
                        memberId = Long.parseLong(memberIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("WebSocket 핸드셰이크 실패: 유효하지 않은 memberId 형식: {}", memberIdStr);
                        response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
                        return false;
                    }
                    break;
                }
            }
        }
        
        // memberId가 없으면 인증 실패
        if (memberId == null) {
            log.warn("WebSocket 핸드셰이크 실패: memberId 파라미터가 없습니다. path={}, query={}", path, queryString);
            response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
            return false;
        }

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElse(null);
        
        if (member == null) {
            log.warn("WebSocket 핸드셰이크 실패: 존재하지 않는 회원입니다. memberId={}", memberId);
            response.setStatusCode(org.springframework.http.HttpStatus.NOT_FOUND);
            return false;
        }

        // UserInfo 생성 및 WebSocket 세션에 저장 (Principal)
        UserInfo userInfo = UserInfo.from(member);
        attributes.put("principal", userInfo);
        
        log.info("WebSocket 핸드셰이크 성공: memberId={}", memberId);
        
        return true;
    }

    /**
     * 핸드셰이크 후에 실행
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 핸드셰이크 후 오류 발생", exception);
        }
    }
}

