package potato.backend.global.security.oauth;

import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.domain.Role;

public record UserInfo(Long memberId, String oauthId, Role role, String name, String email) {
    public static UserInfo from(Member member) {
        return new UserInfo(member.getId(), member.getOauthId(), member.getRole(), member.getName(), member.getEmail());
    }

    public static UserInfo of(Long memberId, String oauthId, Role role, String name, String email) {
        return new UserInfo(memberId, oauthId, role, name, email);
    }
}
