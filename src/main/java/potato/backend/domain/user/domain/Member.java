package potato.backend.domain.user.domain;

import java.math.BigDecimal;

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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    private Role role;

    @Column(nullable = false)
    private String mobileNumber;

    @Builder.Default
    private Boolean activated = true;

    @Builder.Default
    private BigDecimal ratingScore = BigDecimal.ZERO;


    public static Member create(String name, String email, String hashedPassword, String role, String mobileNumber){
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
}