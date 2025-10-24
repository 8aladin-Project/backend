package potato.backend.domain.image.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public record ImageRequest(
        @NotNull(message = "이미지 파일은 필수입니다")
        List<MultipartFile> images
) {
    public ImageRequest {
        if (images.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 이미지가 필요합니다");
        }
    }
}
