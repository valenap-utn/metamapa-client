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

    // Si ya tenemos USER_ID en sesión
    if (session != null) {
      Object idAttr = session.getAttribute("USER_ID");
      if (idAttr instanceof Number num) {
        Long id = num.longValue();
        UsuarioDTO u = clientSeader.obtenerUsuarioPorId(id);
        if (u != null) {
          return u;
        }
      }
    }

    // Si tenemos AUTH_EMAIL (OAuth2SuccessHandler)
    if (session != null) {
      String authEmail = (String) session.getAttribute("AUTH_EMAIL");
      if (authEmail != null && !authEmail.isBlank()) {
        UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(authEmail);
        if (u != null) {
          // cacheamos el id para futuros usos
          if (u.getId() != null) {
            session.setAttribute("USER_ID", u.getId());
          }
          return u;
        }
      }
    }

    // Si seguimos usando accessToken propio para login clásico
    if (session != null) {
      String accessToken = (String) session.getAttribute("accessToken");
      if (accessToken != null && !accessToken.isBlank()) {
        try {
          Long id = JwtUtil.getId(accessToken);
          if (id != null) {
            UsuarioDTO u = clientSeader.obtenerUsuarioPorId(id);
            if (u != null) {
              session.setAttribute("USER_ID", u.getId());
              return u;
            }
          }
          String email = JwtUtil.validarToken(accessToken);
          if (email != null && !email.isBlank()) {
            UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(email);
            if (u != null) {
              if (u.getId() != null) {
                session.setAttribute("USER_ID", u.getId());
              }
              return u;
            }
          }
        } catch (Exception ignored) {
          // si el token no es válido, seguimos probando otros caminos
        }
      }
    }

    // 4) Fallback con Authentication (sirve para login con formulario sin tokens)
    if (authentication != null) {
      Object principal = authentication.getPrincipal();

      // 4.a) Si es OAuth2User y por algún motivo no usamos AUTH_EMAIL
      if (principal instanceof OAuth2User oauth2User) {
        String email = (String) oauth2User.getAttributes().get("email");
        if (email == null || email.isBlank()) {
          Object login = oauth2User.getAttributes().get("login");
          email = (login != null) ? login.toString() : authentication.getName();
        }
        if (email != null && !email.isBlank()) {
          UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(email);
          if (u != null) {
            if (u.getId() != null && session != null) {
              session.setAttribute("USER_ID", u.getId());
            }
            return u;
          }
        }
      }

      // 4.b) Login normal: username suele ser el email
      String username = authentication.getName();
      if (username != null && !username.isBlank()) {
        UsuarioDTO u = clientSeader.obtenerUsuarioPorEmail(username);
        if (u != null) {
          if (u.getId() != null && session != null) {
            session.setAttribute("USER_ID", u.getId());
          }
          return u;
        }
      }
    }

    // Si no pudimos determinar el usuario
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
