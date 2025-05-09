package me.mmtr.koala.utils;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public class Oauth2Util {

    public static OAuth2AuthenticationToken getOAuth2AuthenticationToken(String username, String email, String picture) {
        Map<String, Object> attributes = Map.of(
                "name", username,
                "email", email,
                "picture", picture);
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        return new OAuth2AuthenticationToken(
                oauth2User,
                oauth2User.getAuthorities(),
                "google"
        );
    }
}
