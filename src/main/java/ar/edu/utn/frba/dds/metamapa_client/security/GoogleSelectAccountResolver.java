package ar.edu.utn.frba.dds.metamapa_client.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.HashMap;
import java.util.Map;

public class GoogleSelectAccountResolver implements OAuth2AuthorizationRequestResolver {
  private final OAuth2AuthorizationRequestResolver delegate;

  public GoogleSelectAccountResolver(ClientRegistrationRepository clientRegistrationRepository) {
    this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authorizationRequest = this.delegate.resolve(request);
    return customizeIfGoogle(authorizationRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authorizationRequest = this.delegate.resolve(request, clientRegistrationId);
    return customizeIfGoogle(authorizationRequest);
  }

  private OAuth2AuthorizationRequest customizeIfGoogle(OAuth2AuthorizationRequest request) {
    if(request == null) return null;

    String registrationId = (String) request.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);
    if(!registrationId.equalsIgnoreCase("google")) return request;

    //Forzamos a mostrar el selector de cuentas (siempre)
    Map<String, Object> extraParams = new HashMap<>(request.getAdditionalParameters());
    extraParams.put("prompt", "select_account");

    return OAuth2AuthorizationRequest.from(request)
        .additionalParameters(extraParams)
        .build();
  }

}
