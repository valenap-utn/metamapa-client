package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginResp;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Builder
@Controller
public class OAuth2Controller {
  private final BackendAPI api;

  @GetMapping("/oauth2/success")
  public String oauth2Success(@AuthenticationPrincipal OAuth2User principal, OAuth2AuthenticationToken auth, HttpSession session, HttpServletResponse response) {
    if (principal == null || auth == null) {
      return "redirect:/iniciar-sesion?error=oauth_failed";
    }

    String provider = auth.getAuthorizedClientRegistrationId(); //google, facebook, etc
    Object emailAttr = principal.getAttributes().get("email");
    String email = emailAttr == null ? "" : String.valueOf(emailAttr);

    if(email.isBlank()){
      return "redirect:/iniciar-sesion?error=oauth_no_email";
    }
    Object usernameAttr = principal.getAttributes().get("username");
    String username = usernameAttr == null ? "" : String.valueOf(usernameAttr);

    if (username.isBlank()){
      return "redirect:/iniciar-sesion?error=oauth_no_username";
    }

    // Pedimos al backend (o mock) el rol efectivo para este email+provider
    LoginResp r = api.socialLogin(provider,email);
    if(r == null || !r.ok()){
      return "redirect:/iniciar-sesion?error=" + ((r != null && r.error()!=null) ? r.error() : "oauth_backend_fail");
    }

    String role = r.rol().toUpperCase();
    session.setAttribute("AUTH_EMAIL", email);
    session.setAttribute("AUTH_ROLE", role);

    return role.startsWith("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }
}
