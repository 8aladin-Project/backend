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
     * 상품 ID로 조회 (JPQL)
     */
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 모든 상품 조회 (페이징, JPQL)
     */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    Page<Product> findAllProducts(Pageable pageable);
    
    /**
     * 상품 삭제 (JPQL)
     */
    @Modifying
    @Query("DELETE FROM Product p WHERE p.id = :productId")
    void deleteByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 상품 존재 여부 확인 (JPQL)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.id = :productId")
    boolean existsByIdWithQuery(@Param("productId") Long productId);
    
    /**
     * 상품 개수 조회 (JPQL)
     */
    @Query("SELECT COUNT(p) FROM Product p")
    long countAllProducts();
}
