package potato.backend.domain.user.service;

import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.dto.ProductListResponse;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.dto.SignUpRequest;
import potato.backend.domain.user.dto.SignUpResponse;
import potato.backend.domain.user.exception.MemberNotFoundException;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.global.exception.CustomException;
import potato.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

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
    private final ProductRepository productRepository;


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
        byte[] passwordBytes = request.getPassword().getBytes(StandardCharsets.UTF_8);
        if (passwordBytes.length > 72) {
            log.warn("비밀번호 길이 초과: {} bytes (최대 72바이트)", passwordBytes.length);
            throw new CustomException(ErrorCode.PASSWORD_TOO_LONG);
        }

        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(request.getPassword());

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

     /**
     * 판매내역 조회
     * 내가 등록한 상품 목록을 반환합니다.
     * @param memberId 회원 ID
     * @return 판매내역 (상품 목록)
     */
    public List<ProductListResponse> getSalesHistory(Long memberId) {
        log.info("판매내역 조회 시작 - memberId: {}", memberId);
        
        // 회원 존재 여부 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        List<Product> products = productRepository.findByMemberIdWithDetails(memberId);
        List<ProductListResponse> salesHistory = products.stream()
                .map(ProductListResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("판매내역 조회 완료 - memberId: {}, 상품 개수: {}", memberId, salesHistory.size());
        return salesHistory;
    }

    /**
     * 구매내역 조회
     * 내가 구매한 상품 목록을 반환합니다.
     * (Product.buyer == 내 ID인 상품 목록)
     * @param memberId 회원 ID
     * @return 구매내역 (상품 목록)
     */
    public List<ProductListResponse> getPurchaseHistory(Long memberId) {
        log.info("구매내역 조회 시작 - memberId: {}", memberId);
        
        // 회원 존재 여부 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        List<Product> products = productRepository.findByBuyerIdWithDetails(memberId);
        List<ProductListResponse> purchaseHistory = products.stream()
                .map(ProductListResponse::fromEntity)
                .collect(Collectors.toList());
        
        log.info("구매내역 조회 완료 - memberId: {}, 상품 개수: {}", memberId, purchaseHistory.size());
        return purchaseHistory;
    }
}