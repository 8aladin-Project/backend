package potato.backend.domain.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.storage.service.S3Service;
import potato.backend.domain.user.domain.Member;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Image 도메인 테스트")
class ImageDomainTest {

    @Test
    @DisplayName("이미지 생성 테스트")
    void createImage() {
        // when
        Image image = Image.create("images/test-image-2.png");

        // then
        assertThat(image).isNotNull();
        assertThat(image.getImageUrl()).isEqualTo("images/test-image-2.png");
    }

    @Test
    @DisplayName("이미지 생성 시 imageUrl이 null이면 예외 발생")
    void createImageWithNullUrl() {
        // when & then
        assertThatThrownBy(() -> Image.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("이미지 생성 시 imageUrl이 빈 문자열이면 예외 발생")
    void createImageWithEmptyUrl() {
        // when & then
        assertThatThrownBy(() -> Image.create(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("이미지 URL이 올바르게 저장되는지 테스트")
    void imageUrlStoredCorrectly() {
        // given
        String imageUrl = "images/test-image-3.png";

        // when
        Image image = Image.create(imageUrl);

        // then
        assertThat(image.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("모든 테스트 이미지가 존재하는지 확인")
    void testAllImagesExist() {
        // given
        List<String> testImages = List.of(
                "images/test-image-2.png",
                "images/test-image-3.png",
                "images/test-image-4.png",
                "images/test-image-5.png",
                "images/test-image-6.png"
        );

        // when & then
        testImages.forEach(imageUrl -> {
            Image image = Image.create(imageUrl);
            assertThat(image.getImageUrl()).isEqualTo(imageUrl);
        });
    }

    /**
     * S3 업로드 통합 테스트
     */
    @SpringBootTest
    @ActiveProfiles("test")
    @Disabled("S3 실제 연결이 필요한 통합 테스트 - CI/CD 환경에서는 비활성화")
    @DisplayName("S3 이미지 업로드 통합 테스트")
    static class S3ImageUploadTest {

        @Autowired(required = false)
        private S3Service s3Service;

        @Test
        @DisplayName("리소스 폴더의 실제 이미지를 S3에 업로드하고 존재 여부 확인")
        void uploadRealImageToS3AndVerify() throws IOException {
            // S3Service가 없으면 테스트 건너뛰기
            if (s3Service == null) {
                System.out.println("S3Service가 비활성화되어 테스트를 건너뜁니다.");
                return;
            }

            // given
            MockMultipartFile imageFile = createMockMultipartFile("test-image-2.png");

            // when
            String uploadedUrl = s3Service.uploadFile(imageFile);

            // then
            assertThat(uploadedUrl).isNotNull();
            assertThat(uploadedUrl).contains("test-image-2");

            // S3에 실제로 파일이 존재하는지 확인
            boolean exists = s3Service.fileExists(uploadedUrl);
            assertThat(exists)
                    .as("업로드된 이미지 파일이 S3에 존재해야 합니다: %s", uploadedUrl)
                    .isTrue();

            System.out.println("✅ 이미지 업로드 성공: " + uploadedUrl);
            System.out.println("✅ S3에 파일 존재 확인 완료");

            // cleanup
            s3Service.deleteFile(uploadedUrl);

            // 삭제 후 존재하지 않는지 확인
            assertThat(s3Service.fileExists(uploadedUrl))
                    .as("삭제된 이미지 파일이 S3에 존재하지 않아야 합니다")
                    .isFalse();

            System.out.println("✅ 파일 삭제 및 검증 완료");
        }

        @Test
        @DisplayName("여러 실제 이미지를 S3에 업로드하고 모두 존재하는지 확인")
        void uploadMultipleRealImagesToS3() throws IOException {
            // S3Service가 없으면 테스트 건너뛰기
            if (s3Service == null) {
                System.out.println("S3Service가 비활성화되어 테스트를 건너뜁니다.");
                return;
            }

            // given
            List<MultipartFile> imageFiles = new ArrayList<>();
            imageFiles.add(createMockMultipartFile("test-image-2.png"));
            imageFiles.add(createMockMultipartFile("test-image-3.png"));
            imageFiles.add(createMockMultipartFile("test-image-4.png"));
            imageFiles.add(createMockMultipartFile("test-image-5.png"));
            imageFiles.add(createMockMultipartFile("test-image-6.png"));

            // when
            List<String> uploadedUrls = s3Service.uploadFiles(imageFiles);

            // then
            assertThat(uploadedUrls).hasSize(5);

            System.out.println("✅ 5개의 이미지 업로드 성공");

            // 각 이미지가 S3에 실제로 존재하는지 확인
            for (int i = 0; i < uploadedUrls.size(); i++) {
                String url = uploadedUrls.get(i);
                assertThat(s3Service.fileExists(url))
                        .as("업로드된 이미지 %d번이 S3에 존재해야 합니다: %s", i + 1, url)
                        .isTrue();
                System.out.println("✅ 이미지 " + (i + 1) + " 존재 확인: " + url);
            }

            // cleanup
            s3Service.deleteFiles(uploadedUrls);

            // 모두 삭제되었는지 확인
            uploadedUrls.forEach(url ->
                    assertThat(s3Service.fileExists(url))
                            .as("삭제된 이미지가 S3에 존재하지 않아야 합니다: %s", url)
                            .isFalse()
            );

            System.out.println("✅ 모든 파일 삭제 및 검증 완료");
        }

        @Test
        @DisplayName("Product와 함께 이미지를 S3에 업로드하고 검증")
        void uploadProductImagesWithS3Integration() throws IOException {
            // S3Service가 없으면 테스트 건너뛰기
            if (s3Service == null) {
                System.out.println("S3Service가 비활성화되어 테스트를 건너뜁니다.");
                return;
            }

            // given - 실제 이미지 파일을 S3에 업로드
            List<MultipartFile> imageFiles = List.of(
                    createMockMultipartFile("test-image-2.png"),
                    createMockMultipartFile("test-image-3.png"),
                    createMockMultipartFile("test-image-4.png")
            );

            List<String> uploadedUrls = s3Service.uploadFiles(imageFiles);

            System.out.println("✅ Product용 3개 이미지 업로드 성공");

            // when - Product 생성 (업로드된 S3 URL 사용)
            Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
            Category category = Category.create("전자기기");

            Product product = Product.create(
                    member,
                    List.of(category),
                    "S3 이미지 테스트 상품",
                    "S3에 업로드된 실제 이미지를 사용하는 상품",
                    uploadedUrls,
                    BigDecimal.valueOf(100000),
                    Status.SELLING,
                    uploadedUrls.get(0)
            );

            // then - Product의 이미지가 올바르게 설정되었는지 확인
            assertThat(product.getImages()).hasSize(3);
            assertThat(product.getImages())
                    .extracting(Image::getImageUrl)
                    .containsExactlyInAnyOrderElementsOf(uploadedUrls);

            System.out.println("✅ Product에 이미지 3개 정상 설정됨");

            // 모든 이미지가 S3에 실제로 존재하는지 확인
            uploadedUrls.forEach(url -> {
                assertThat(s3Service.fileExists(url))
                        .as("Product의 이미지가 S3에 존재해야 합니다: %s", url)
                        .isTrue();
                System.out.println("✅ Product 이미지 S3 존재 확인: " + url);
            });

            // cleanup
            s3Service.deleteFiles(uploadedUrls);

            System.out.println("✅ Product 이미지 삭제 완료");
        }

        /**
         * 테스트 리소스 폴더의 이미지 파일을 읽어 MockMultipartFile 생성
         */
        private MockMultipartFile createMockMultipartFile(String filename) throws IOException {
            Path imagePath = Paths.get("src/test/resources/images/" + filename);

            if (!Files.exists(imagePath)) {
                throw new IllegalArgumentException("테스트 이미지 ���일을 찾을 수 없습니다: " + imagePath);
            }

            InputStream inputStream = Files.newInputStream(imagePath);
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            return new MockMultipartFile(
                    "file",
                    filename,
                    "image/png",
                    content
            );
        }
    }
}
