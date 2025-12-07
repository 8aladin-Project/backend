package potato.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.dto.SignUpRequest;
import potato.backend.domain.user.dto.SignUpResponse;
import potato.backend.domain.user.exception.MemberNotFoundException;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.global.exception.CustomException;
import potato.backend.global.exception.ErrorCode;

/**
 * 회원 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * FCM 토큰 업데이트
     * @param memberId 회원 ID
     * @param fcmToken FCM 토큰
     */
    @Transactional
    public void updateFcmToken(Long memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        member.updateFcmToken(fcmToken);
        log.info("FCM 토큰 업데이트 완료: memberId={}", memberId);
    }

    /**
     * FCM 토큰 제거
     * @param memberId 회원 ID
     */
    @Transactional
    public void clearFcmToken(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.updateFcmToken(null);
        log.info("유효하지 않은 FCM 토큰 제거: memberId={}", memberId);
    }

    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     * @return 회원가입 응답
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        log.info("회원가입 요청: email={}, name={}", request.getEmail(), request.getName());

        // 이메일 중복 체크
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 길이 체크 (BCrypt는 72바이트 제한)
        byte[] passwordBytes = request.getPassword().getBytes();
        if (passwordBytes.length > 72) {
            log.warn("비밀번호 길이 초과: {} bytes (최대 72바이트)", passwordBytes.length);
            throw new CustomException(ErrorCode.PASSWORD_TOO_LONG);
        }

        // 비밀번호 암호화
        String hashedPassword;
        try {
            hashedPassword = passwordEncoder.encode(request.getPassword());
        } catch (IllegalArgumentException e) {
            // BCryptPasswordEncoder가 72바이트 초과 시 IllegalArgumentException 발생
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("72") || errorMessage.contains("cannot be more than"))) {
                log.warn("BCryptPasswordEncoder 비밀번호 길이 초과: {}", errorMessage);
                throw new CustomException(ErrorCode.PASSWORD_TOO_LONG);
            }
            // 다른 IllegalArgumentException은 그대로 전파
            log.error("비밀번호 암호화 중 예외 발생: {}", errorMessage, e);
            throw new CustomException(ErrorCode.INVALID_ARGUMENT, "비밀번호 암호화에 실패했습니다: " + errorMessage);
        }

        // 회원 생성
        Member member = Member.create(
                request.getName(),
                request.getEmail(),
                hashedPassword,
                request.getMobileNumber()
        );

        Member savedMember = memberRepository.save(member);
        log.info("회원가입 완료: memberId={}, email={}", savedMember.getId(), savedMember.getEmail());

        return SignUpResponse.of(
                savedMember.getId(),
                savedMember.getName(),
                savedMember.getEmail()
        );
    }
}

