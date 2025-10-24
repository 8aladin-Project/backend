package potato.backend.domain.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Category Repository 테스트")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 생성 및 저장 테스트")
    void createAndSaveCategory() {
        // given
        Category category = Category.create("전자기기");

        // when
        Category savedCategory = categoryRepository.save(category);

        // then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("전자기기");
    }

    @Test
    @DisplayName("카테고리 ID로 조회 테스트")
    void findById() {
        // given
        Category category = Category.create("의류");
        Category savedCategory = categoryRepository.save(category);

        // when
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        // then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getCategoryName()).isEqualTo("의류");
    }

    @Test
    @DisplayName("카테고리 이름으로 여러 개 조회 테스트")
    void findByNameIn() {
        // given
        Category category1 = Category.create("전자기기");
        Category category2 = Category.create("의류");
        Category category3 = Category.create("가구");
        Category category4 = Category.create("도서");
        categoryRepository.saveAll(List.of(category1, category2, category3, category4));

        // when
        List<Category> foundCategories = categoryRepository.findByNameIn(List.of("전자기기", "의류", "도서"));

        // then
        assertThat(foundCategories).hasSize(3);
        assertThat(foundCategories).extracting(Category::getCategoryName)
                .containsExactlyInAnyOrder("전자기기", "의류", "도서");
    }

    @Test
    @DisplayName("모든 카테고리 조회 테스트")
    void findAll() {
        // given
        Category category1 = Category.create("스포츠");
        Category category2 = Category.create("음악");
        Category category3 = Category.create("게임");
        categoryRepository.saveAll(List.of(category1, category2, category3));

        // when
        List<Category> allCategories = categoryRepository.findAll();

        // then
        assertThat(allCategories).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    void deleteCategory() {
        // given
        Category category = Category.create("삭제될 카테고리");
        Category savedCategory = categoryRepository.save(category);
        Long categoryId = savedCategory.getId();

        // when
        categoryRepository.deleteById(categoryId);

        // then
        Optional<Category> deletedCategory = categoryRepository.findById(categoryId);
        assertThat(deletedCategory).isEmpty();
    }

    @Test
    @DisplayName("카테고리 존재 여부 확인 테스트")
    void existsById() {
        // given
        Category category = Category.create("존재 확인 카테고리");
        Category savedCategory = categoryRepository.save(category);

        // when & then
        assertThat(categoryRepository.existsById(savedCategory.getId())).isTrue();
        assertThat(categoryRepository.existsById(99999L)).isFalse();
    }

    @Test
    @DisplayName("카테고리 개수 조회 테스트")
    void countCategories() {
        // given
        Category category1 = Category.create("카테고리1");
        Category category2 = Category.create("카테고리2");
        categoryRepository.saveAll(List.of(category1, category2));

        // when
        long count = categoryRepository.count();

        // then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("여러 카테고리 일괄 저장 테스트")
    void saveAllCategories() {
        // given
        List<Category> categories = List.of(
                Category.create("카테고리A"),
                Category.create("카테고리B"),
                Category.create("카테고리C")
        );

        // when
        List<Category> savedCategories = categoryRepository.saveAll(categories);

        // then
        assertThat(savedCategories).hasSize(3);
        assertThat(savedCategories).allMatch(category -> category.getId() != null);
    }
}

