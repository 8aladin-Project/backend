package potato.backend.domain.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import potato.backend.domain.wishlist.domain.Wishlist;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.product.domain.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 특정 회원의 위시리스트 목록 조회 (최신순)
    List<Wishlist> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 특정 회원의 위시리스트 목록 조회
    @Query("SELECT w FROM Wishlist w " +
           "JOIN FETCH w.product p " +
           "JOIN FETCH p.member " +
           "LEFT JOIN FETCH p.categories " +
           "WHERE w.member.id = :memberId " +
           "ORDER BY w.createdAt DESC")
    List<Wishlist> findByMemberIdWithDetails(@Param("memberId") Long memberId);

    // 특정 회원과 상품으로 위시리스트 조회 (중복 체크용)
    Optional<Wishlist> findByMemberAndProduct(Member member, Product product);

    // 특정 회원과 상품으로 위시리스트 조회 (회원 ID, 상품 ID로)
    Optional<Wishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 회원의 위시리스트 개수 조회
    Long countByMemberId(Long memberId);

    // 특정 상품이 위시리스트에 추가된 횟수 조회
    Long countByProductId(Long productId);
}
