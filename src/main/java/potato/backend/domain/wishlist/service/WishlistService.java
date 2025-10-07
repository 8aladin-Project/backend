package potato.backend.domain.wishlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import potato.backend.domain.wishlist.domain.Wishlist;
import potato.backend.domain.wishlist.repository.WishlistRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.repository.ProductRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * 위시리스트 토글 (하트 버튼 클릭 시)
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return true: 추가됨, false: 제거됨
     */
    @Transactional
    public boolean addOrRemoveFromWishlist(Long memberId, Long productId) {
        // 회원 존재 여부 확인
        Member member = getMember(memberId);
        
        // 상품 존재 여부 확인
        Product product = getProduct(productId);
        
        // 이미 위시리스트에 있는지 확인
        Optional<Wishlist> existingWishlist = wishlistRepository.findByMemberIdAndProductId(memberId, productId);
        
        if (existingWishlist.isPresent()) {
            // 이미 있으면 제거
            wishlistRepository.delete(existingWishlist.get());
            log.info("위시리스트 제거: memberId={}, productId={}", memberId, productId);
            return false; // 제거됨
        } else {
            // 없으면 추가
            Wishlist wishlist = Wishlist.create(member, product);
            wishlistRepository.save(wishlist);
            log.info("위시리스트 추가: memberId={}, productId={}", memberId, productId);
            return true; // 추가됨
        }
    }

    // 위시리스트에 상품이 있는지 확인 (사용자가 상품 페이지에 들어갔을 때 표시하기 위해 필요)
    public boolean isInWishlist(Long memberId, Long productId) {
        return wishlistRepository.findByMemberIdAndProductId(memberId, productId).isPresent();
    }

    // 회원의 위시리스트 개수 조회
    public Long getWishlistCount(Long memberId) {
        getMember(memberId); // 회원 존재 여부 확인
        return wishlistRepository.countByMemberId(memberId);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "회원을 찾을 수 없습니다: " + memberId));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "상품을 찾을 수 없습니다: " + productId));
    }
}
