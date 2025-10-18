package potato.backend.domain.category.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String categoryName;

    public static Category create(String categoryName){
        Objects.requireNonNull(categoryName);

        return Category.builder()
            .categoryName(categoryName)
            .build();
    }
}
