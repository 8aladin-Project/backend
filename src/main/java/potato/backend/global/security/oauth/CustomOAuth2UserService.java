package potato.backend.global.security.oauth;

import java.util.Objects;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Member member = findOrCreateMember(oAuth2User);

        return new CustomOAuth2User(
                oAuth2User,
                userRequest
                        .getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName(),
                member);
    }

    private Member findOrCreateMember(OAuth2User oAuth2User) {
        String oauthId = oAuth2User.getName();
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

        Member member = memberRepository.findByOauthId(oauthId).orElse(null);
        if (member == null) {
            return memberRepository.save(Member.create(oauthId, email, name));
        } else {
            if (!Objects.equals(member.getEmail(), email)) {
                member.updateEmail(email);
            }
            if (!Objects.equals(member.getName(), name)) {
                member.updateName(name);
            }
        }

        return member;
    }
}
