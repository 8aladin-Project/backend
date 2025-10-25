package potato.backend.domain.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import potato.backend.domain.category.domain.Category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Category 도메인 테스트")
class CategoryDomainTest {

    @Test
    @DisplayName("카테고리 생성 테스트")
    void createCategory() {
        // given
        String categoryName = "전자기기";

        // when
        Category category = Category.create(categoryName);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("카테고리 이름이 null이면 예외 발생")
    void createCategoryWithNullName() {
        // when & then
        assertThatThrownBy(() -> Category.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("다양한 카테고리 이름으로 생성 테스트")
    void createVariousCategories() {
        // given
        String[] categoryNames = {"전자기기", "의류", "가구", "도서", "스포츠", "음악"};

        // when & then
        for (String name : categoryNames) {
            Category category = Category.create(name);
            assertThat(category.getCategoryName()).isEqualTo(name);
        }
    }

    @Test
    @DisplayName("카테고리 이름이 정확히 저장되는지 테스트")
    void categoryNameStoredCorrectly() {
        // given
        String expectedName = "전자제품 및 악세서리";

        // when
        Category category = Category.create(expectedName);

        // then
        assertThat(category.getCategoryName())
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(expectedName);
    }

    @Test
    @DisplayName("공백이 포함된 카테고리 이름 생성 테스트")
    void createCategoryWithSpaces() {
        // given
        String categoryName = "스포츠 및 레저";

        // when
        Category category = Category.create(categoryName);

        // then
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("특수문자가 포함된 카테고리 이름 생성 테스트")
    void createCategoryWithSpecialCharacters() {
        // given
        String categoryName = "가전/전자";

        // when
        Category category = Category.create(categoryName);

        // then
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("영문 카테고리 이름 생성 테스트")
    void createCategoryWithEnglishName() {
        // given
        String categoryName = "Electronics";

        // when
        Category category = Category.create(categoryName);

        // then
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("긴 카테고리 이름 생성 테스트")
    void createCategoryWithLongName() {
        // given
        String categoryName = "전자기기 및 컴퓨터 액세서리 그리고 주변기기 제품";

        // when
        Category category = Category.create(categoryName);

        // then
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
        assertThat(category.getCategoryName().length()).isGreaterThan(10);
    }
}

