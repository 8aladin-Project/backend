package potato.backend.domain.product.dto;


import potato.backend.domain.product.domain.Image;

import java.util.List;

public class ProductUpdateRequest {
    private String title;
    private String content;
    private String mainImageUrl;
    private List<String> imageUrls;
    private String status;


}
