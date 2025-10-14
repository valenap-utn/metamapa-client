package ar.edu.utn.frba.dds.metamapa_client.security;

import ar.edu.utn.frba.dds.metamapa_client.config.RoleMappingProperties;
import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final RoleMappingProperties roleProperties;
  private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    if(!(authentication instanceof OAuth2AuthenticationToken token)){
      response.sendRedirect("/iniciar-sesion?error=oauth_failed");
      return;
    }

    OAuth2User principal = (OAuth2User) token.getPrincipal();
    Map<String, Object> attributes = principal.getAttributes();
    String provider = token.getAuthorizedClientRegistrationId(); //google, facebook, github, etc

    String email = str(attributes.get("email"));
    if(email.isBlank()){
      response.sendRedirect("/iniciar-sesion?error=oauth_no_email");
      return;
    }
    String username = firstNonBlank(str(attributes.get("name")), localPart(email));
//    if(username.isBlank()){
//      response.sendRedirect("/iniciar-sesion?error=oauth_no_username");
//    }

    //Rol por email o dominio
    String lower = email.toLowerCase();
    boolean byEmail = roleProperties.getAdminEmails().stream()
        .map(String::toLowerCase).anyMatch(lower::equals);
    boolean byDomain = roleProperties.getAdminDomains().stream()
        .map(String::toLowerCase).anyMatch(dom -> lower.endsWith("@" + dom));
    String role = (byEmail || byDomain) ? "ADMINISTRADOR" : "CONTRIBUYENTE";
    /*
    AuthResponseDTO respDTO = this.conexionUserService.verSiCuentaEstaCreada(provider, username);
    if(respDTO == null){
      respDTO = this.conexionUserService.crearCuentaAuth(provider, username, email, role);
    }
    //Se guardan los roles, el email y eso
    */


    //Authorities finales
    List<GrantedAuthority> newAuths = new ArrayList<>();
    newAuths.add(new SimpleGrantedAuthority("ROLE_"+role));

    //opcional
    //por si quisieramos meter un: @PreAuthorize("hasAuthority('SCOPE_user:email')")
    newAuths.addAll(token.getAuthorities().stream()
        .filter(a -> a.getAuthority().startsWith("SCOPE_"))
        .toList());

    //Inyectamos authorities y recreamos el token con las mismas y lo guardamos en el SecurityContext
    OAuth2AuthenticationToken enriched = new OAuth2AuthenticationToken(principal, newAuths, provider);
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(enriched);
    SecurityContextHolder.setContext(securityContext);

    request.getSession(true); //si no existe la sesión => la crea
    contextRepo.saveContext(securityContext, request, response);


    HttpSession newSession = request.getSession();
    newSession.setAttribute("AUTH_PROVIDER", provider);
    newSession.setAttribute("AUTH_EMAIL", email);
    newSession.setAttribute("AUTH_USERNAME", username);
    newSession.setAttribute("AUTH_ROLE", role);

    response.sendRedirect(role.equals("ADMINISTRADOR") ? "/admin" : "/main-gral");
  }

  //Helpers
  private static String str(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }
  private static String firstNonBlank(String... values) {
    for(var v : values) {
      if(v != null && !v.isBlank()) return v;
    }
    return "";
  }
  private static String localPart(String email) {
    int at = email.indexOf('@');
    return at > 0 ? email.substring(0, at) : email;
  }
}
