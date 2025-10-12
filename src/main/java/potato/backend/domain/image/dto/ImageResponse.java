package potato.backend.domain.image.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import potato.backend.domain.image.domain.Image;

public record ImageResponse(
        Long id,
        String imageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt
) {
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getCreatedAt()
        );
    }
    
    public static List<ImageResponse> fromList(List<Image> images) {
        return images.stream()
                .map(ImageResponse::from)
                .toList();
    }
}
