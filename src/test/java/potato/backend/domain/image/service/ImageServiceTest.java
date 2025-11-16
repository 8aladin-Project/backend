package potato.backend.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.dto.ImageResponse;
import potato.backend.domain.image.repository.ImageRepository;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.storage.service.S3Service;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService 단위 테스트")
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private S3Service s3Service;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepository, productRepository);
        ReflectionTestUtils.setField(imageService, "s3Service", s3Service);
    }

    @Test
    @DisplayName("단일 이미지 업로드 시 S3와 DB에 저장된다")
    void uploadImage_success() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-photo.jpg",
                "image/jpeg",
                "dummy-jpeg-content".getBytes()
        );

        when(s3Service.uploadFile(file)).thenReturn("https://cdn.test/images/test-photo.jpg");
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image image = invocation.getArgument(0);
            ReflectionTestUtils.setField(image, "id", 1L);
            return image;
        });

        // when
        ImageResponse response = imageService.uploadImage(file);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.imageUrl()).isEqualTo("https://cdn.test/images/test-photo.jpg");
        verify(s3Service).uploadFile(file);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    @DisplayName("여러 이미지를 업로드하면 각각 저장된다")
    void uploadImages_success() {
        // given
        MockMultipartFile first = new MockMultipartFile(
                "images",
                "first.png",
                "image/png",
                "first-image".getBytes()
        );
        MockMultipartFile second = new MockMultipartFile(
                "images",
                "second.png",
                "image/png",
                "second-image".getBytes()
        );

        when(s3Service.uploadFiles(anyList())).thenReturn(List.of(
                "https://cdn.test/images/first.png",
                "https://cdn.test/images/second.png"
        ));

        AtomicLong sequence = new AtomicLong(10L);
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image image = invocation.getArgument(0);
            ReflectionTestUtils.setField(image, "id", sequence.incrementAndGet());
            return image;
        });

        // when
        List<ImageResponse> responses = imageService.uploadImages(List.of(first, second));

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ImageResponse::imageUrl)
                .containsExactly(
                        "https://cdn.test/images/first.png",
                        "https://cdn.test/images/second.png"
                );
        verify(s3Service).uploadFiles(anyList());
        verify(imageRepository, times(2)).save(any(Image.class));
    }
}
