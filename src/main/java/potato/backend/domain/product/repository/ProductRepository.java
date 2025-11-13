package potato.backend.domain.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import potato.backend.domain.product.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 상품 ID로 조회 (상품아이디)
     * SQL: SELECT * FROM product WHERE 상품아이디 = ?
     */
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 모든 상품 조회 (페이징)
     * SQL: SELECT * FROM product ORDER BY created_at DESC
     */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    Page<Product> findAllProducts(Pageable pageable);
    
    /**
     * 상품 삭제 (상품아이디)
     * SQL: DELETE FROM product WHERE 상품아이디 = ?
     */
    @Modifying
    @Query("DELETE FROM Product p WHERE p.id = :productId")
    void deleteByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 상품 존재 여부 확인 (상품아이디)
     * SQL: SELECT COUNT(*) > 0 FROM product WHERE 상품아이디 = ?
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.id = :productId")
    boolean existsByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 상품 개수 조회
     * SQL: SELECT COUNT(*) FROM product
     */
    @Query("SELECT COUNT(p) FROM Product p")
    long countAllProducts();
    
    /**
     * 사용자별 상품 조회 (사용자아이디)
     * SQL: SELECT * FROM product WHERE 사용자아이디 = ? ORDER BY created_at DESC
     */
    @Query("SELECT p FROM Product p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    Page<Product> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);
    
    /**
     * 상태별 상품 조회 (상태)
     * SQL: SELECT * FROM product WHERE 상태 = ? ORDER BY created_at DESC
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Product> findByStatus(@Param("status") potato.backend.domain.product.domain.Status status, Pageable pageable);
    
    /**
     * 가격 범위별 상품 조회 (가격)
     * SQL: SELECT * FROM product WHERE 가격 BETWEEN ? AND ? ORDER BY 가격 ASC
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    Page<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, Pageable pageable);
    
    /**
     * 제목으로 상품 검색 (제목)
     * SQL: SELECT * FROM product WHERE 제목 LIKE ? ORDER BY created_at DESC
     */
    @Query("SELECT p FROM Product p WHERE p.title LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Product> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}
