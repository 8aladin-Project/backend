package potato.backend.domain.user.domain;

import java.math.BigDecimal;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import potato.backend.domain.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String hashedPassword;

    @Column(unique = true)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String mobileNumber;

    @Builder.Default
    private Boolean activated = true;

    @Builder.Default
    private BigDecimal ratingScore = BigDecimal.ZERO;


    public static Member create(String name, String email, String hashedPassword, String mobileNumber){
        return Member.builder()
            .name(name)
            .email(email)
            .hashedPassword(hashedPassword)
            .role(Role.USER)
            .mobileNumber(mobileNumber)
            .activated(true)
            .ratingScore(BigDecimal.ZERO)
            .build();
    }

    // OAuth 회원가입용 메서드
    public static Member create(String oauthId, String email, String name) {
        return Member.builder()
            .oauthId(oauthId)
            .email(email)
            .name(name)
            .role(Role.USER)
            .mobileNumber("") // OAuth는 전화번호가 없을 수 있음
            .activated(true)
            .ratingScore(BigDecimal.ZERO)
            .build();
    }

    // OAuth 사용자 정보 업데이트 메서드들
    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateName(String name) {
        this.name = name;
    }
}