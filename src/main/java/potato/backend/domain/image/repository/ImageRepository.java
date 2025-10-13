package potato.backend.domain.image.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    /**
     * 상품에 속한 이미지 목록 조회 (상품아이디)
     * SQL: SELECT * FROM product_images WHERE 상품아이디 = ? ORDER BY created_at DESC
     */
    @Query("SELECT i FROM Image i WHERE i.product = :product ORDER BY i.createdAt DESC")
    List<Image> findByProduct(@Param("product") Product product);
    
    /**
     * 상품 ID로 이미지 목록 조회 (상품아이디)
     * SQL: SELECT * FROM product_images WHERE 상품아이디 = ? ORDER BY created_at DESC
     */
    @Query("SELECT i FROM Image i WHERE i.product.id = :productId ORDER BY i.createdAt DESC")
    List<Image> findByProductId(@Param("productId") Long productId);
    
    /**
     * 상품에 속한 모든 이미지 삭제 (상품아이디)
     * SQL: DELETE FROM product_images WHERE 상품아이디 = ?
     */
    @Modifying
    @Query("DELETE FROM Image i WHERE i.product = :product")
    void deleteByProduct(@Param("product") Product product);
    
    /**
     * 상품 ID로 모든 이미지 삭제 (상품아이디)
     * SQL: DELETE FROM product_images WHERE 상품아이디 = ?
     */
    @Modifying
    @Query("DELETE FROM Image i WHERE i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    /**
     * 이미지 URL로 존재 여부 확인 (상품 이미지 url)
     * SQL: SELECT COUNT(*) > 0 FROM product_images WHERE `상품 이미지 url` = ?
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Image i WHERE i.imageUrl = :imageUrl")
    boolean existsByImageUrl(@Param("imageUrl") String imageUrl);
    
    /**
     * 이미지 ID로 조회 (상품이미지아이디)
     * SQL: SELECT * FROM product_images WHERE 상품이미지아이디 = ?
     */
    @Query("SELECT i FROM Image i WHERE i.id = :imageId")
    Optional<Image> findByIdWithQuery(@Param("imageId") Long imageId);
    
    /**
     * 이미지 URL로 조회 (상품 이미지 url)
     * SQL: SELECT * FROM product_images WHERE `상품 이미지 url` = ?
     */
    @Query("SELECT i FROM Image i WHERE i.imageUrl = :imageUrl")
    Optional<Image> findByImageUrl(@Param("imageUrl") String imageUrl);
    
    /**
     * 상품의 이미지 개수 조회 (상품아이디)
     * SQL: SELECT COUNT(*) FROM product_images WHERE 상품아이디 = ?
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    /**
     * 모든 이미지 개수 조회
     * SQL: SELECT COUNT(*) FROM product_images
     */
    @Query("SELECT COUNT(i) FROM Image i")
    long countAllImages();
}
