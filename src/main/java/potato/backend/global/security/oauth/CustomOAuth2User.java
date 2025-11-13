package potato.backend.global.security.oauth;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;
import lombok.ToString;

import potato.backend.domain.user.domain.Member;

@Getter
@ToString
public class CustomOAuth2User extends DefaultOAuth2User {

    private final UserInfo userInfo;

    public CustomOAuth2User(OAuth2User oAuth2User, String nameAttributeKey, Member member) {
        super(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), nameAttributeKey);
        this.userInfo = UserInfo.from(member);
    }
}
