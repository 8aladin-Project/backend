package potato.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.exception.MemberNotFoundException;
import potato.backend.domain.user.repository.MemberRepository;

/**
 * 회원 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

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
}

