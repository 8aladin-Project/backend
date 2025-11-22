package potato.backend.domain.wishlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import potato.backend.domain.wishlist.domain.Wishlist;
import potato.backend.domain.wishlist.repository.WishlistRepository;
import potato.backend.domain.wishlist.dto.WishlistListResponse;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * 위시리스트에 상품 추가
     * @param memberId 회원 ID
     * @param productId 상품 ID
     */
    @Transactional
    public void addToWishlist(Long memberId, Long productId) {
        Member member = getMember(memberId);
        Product product = getProduct(productId);
        
        // 중복 체크
        if (wishlistRepository.findByMemberIdAndProductId(memberId, productId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "이미 위시리스트에 추가된 상품입니다");
        }
        
        Wishlist wishlist = Wishlist.create(member, product);
        wishlistRepository.save(wishlist);
        log.info("위시리스트에 상품 추가: memberId={}, productId={}", memberId, productId);
    }

    /**
     * 위시리스트에서 상품 제거
     * @param memberId 회원 ID
     * @param productId 상품 ID
     */
    @Transactional
    public void removeFromWishlist(Long memberId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "위시리스트에서 해당 상품을 찾을 수 없습니다"));
        
        wishlistRepository.delete(wishlist);
        log.info("위시리스트에서 상품 제거: memberId={}, productId={}", memberId, productId);
    }

    // 위시리스트에 상품이 있는지 확인 (사용자가 상품 페이지에 들어갔을 때 표시하기 위해 필요)
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long memberId, Long productId) {
        return wishlistRepository.findByMemberIdAndProductId(memberId, productId).isPresent();
    }

    /**
     * 회원의 위시리스트 목록 조회
     * @param memberId 회원 ID
     * @return 위시리스트 목록 (최신순)
     */
    public List<WishlistListResponse> getWishlistList(Long memberId) {
        getMember(memberId); // 회원 존재 여부 확인
        List<Wishlist> wishlists = wishlistRepository.findByMemberIdWithDetails(memberId);
        return wishlists.stream()
                .map(WishlistListResponse::fromEntity)
                .collect(Collectors.toList());
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
