package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.clients.utils.JwtUtil;
import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class UsuarioCuentaMockService implements IUsuarioCuentaService {

  private final ClientSeader clientSeader;

  public UsuarioCuentaMockService(ClientSeader clientSeader) {
    this.clientSeader = clientSeader;
  }

  @Override
  public UsuarioDTO ensureFromOAuth(String email, String nombre, String provider, String roleHint) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email OAuth2 vacío");
    }

    UsuarioDTO existente = clientSeader.obtenerUsuarioPorEmail(email);
    if (existente != null) return existente;

    UsuarioDTO nuevo = new UsuarioDTO();
    nuevo.setEmail(email);
    nuevo.setNombre(nombre);
    nuevo.setRol(roleHint);
    return clientSeader.crearUsuario(nuevo);
  }

  @Override
  public UsuarioDTO obtenerUsuarioActual(HttpSession session, Authentication authentication) {

    // 1) Si guardaste USER_ID en sesión (después de login) => usalo
    Object idAttr = session.getAttribute("USER_ID");
    if (idAttr instanceof Long) {
      UsuarioDTO u = clientSeader.obtenerUsuarioPorId((Long) idAttr);
      if (u != null) return u;
    } else if (idAttr instanceof Integer) {
      UsuarioDTO u = clientSeader.obtenerUsuarioPorId(((Integer) idAttr).longValue());
      if (u != null) return u;
    }

    // 2) accessToken propio (login con formulario)
    Object tokenObj = session.getAttribute("accessToken");
    if (tokenObj instanceof String accessToken && !accessToken.isBlank()) {
      try {
        Long userId = JwtUtil.getId(accessToken);
        if (userId != null) {
          UsuarioDTO u = clientSeader.obtenerUsuarioPorId(userId);
          if (u != null) return u;
        }
        String email = JwtUtil.validarToken(accessToken);
        if (email != null && !email.isBlank()) {
          UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(email);
          if (u != null) return u;
        }
      } catch (Exception ignored) {}
    }

    // 3) Datos de sesión puestos por OAuth2SuccessHandler
    String email = (String) session.getAttribute("AUTH_EMAIL");
    String username = (String) session.getAttribute("AUTH_USERNAME");
    String role = (String) session.getAttribute("AUTH_ROLE");
    if (email != null && !email.isBlank()) {
      UsuarioDTO existente = clientSeader.obtenerUsuarioPorEmail(email);
      if (existente != null) return existente;

      UsuarioDTO dto = new UsuarioDTO();
      dto.setEmail(email);
      dto.setNombre(username);
      dto.setRol(role != null ? role : extraerRol(authentication));
      return dto;
    }

    // 4) Fallback: Authentication directo
    if (authentication != null) {
      Object principal = authentication.getPrincipal();

      if (principal instanceof OAuth2User oAuth2User) {
        String mail = (String) oAuth2User.getAttributes().get("email");
        if (mail == null || mail.isBlank()) {
          Object login = oAuth2User.getAttributes().get("login");
          mail = (login != null) ? login.toString() : authentication.getName();
        }
        if (mail != null && !mail.isBlank()) {
          UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(mail);
          if (u != null) return u;
        }
      } else {
        String usernameAuth = authentication.getName();
        if (usernameAuth != null && !usernameAuth.isBlank()) {
          UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(usernameAuth);
          if (u != null) return u;
        }
      }
    }

    return null;
  }

  @Override
  public UsuarioDTO findByEmail(String email) {
    return clientSeader.obtenerUsuarioPorEmail(email);
  }

  @Override
  public UsuarioDTO findById(Long id) {
    return clientSeader.obtenerUsuarioPorId(id);
  }

  private String extraerRol(Authentication authentication) {
    if (authentication == null) return null;
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(a -> a.startsWith("ROLE_"))
        .map(a -> a.substring("ROLE_".length()))
        .findFirst()
        .orElse(null);
  }
}
