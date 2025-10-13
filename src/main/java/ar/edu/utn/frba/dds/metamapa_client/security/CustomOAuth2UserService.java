package ar.edu.utn.frba.dds.metamapa_client.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService { //normaliza email, name, picture (para poder manejar oauth con Google|Github|etc + facil)
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User user = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId().toLowerCase();
    Map<String, Object> attributes = user.getAttributes();

    Map<String, Object> normalizedAttributes = switch (provider) {
      case "google" -> Map.of(
          "email", attributes.get("email"),
          "name", attributes.getOrDefault("name", ""),
          "picture", attributes.getOrDefault("picture", "")
      );
      case "github" -> Map.of(
          "email", attributes.get("email"),
          "name", attributes.getOrDefault("name", attributes.getOrDefault("login", "")),
          "picture", attributes.getOrDefault("avatar_url", null)
      );
      default -> attributes;
    };
    return new DefaultOAuth2User(user.getAuthorities(), normalizedAttributes, "email");
  }
}
