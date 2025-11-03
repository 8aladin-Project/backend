package potato.backend.global.security.oauth;

import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.domain.Role;

import java.security.Principal;

public record UserInfo(Long memberId, String oauthId, Role role, String name, String email) implements Principal {
    public static UserInfo from(Member member) {
        return new UserInfo(member.getId(), member.getOauthId(), member.getRole(), member.getName(), member.getEmail());
    }

    public static UserInfo of(Long memberId, String oauthId, Role role, String name, String email) {
        return new UserInfo(memberId, oauthId, role, name, email);
    }

    // Principal 인터페이스 구현
    // Principal.getName()은 실제 사용자 이름을 반환 (name 필드 사용)
    @Override
    public String getName() {
        return name != null ? name : String.valueOf(memberId);
    }
}
