package ar.edu.utn.frba.dds.metamapa_client.provider;

import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import ar.edu.utn.frba.dds.metamapa_client.exceptions.FalloEnLaAutenticacion;
import ar.edu.utn.frba.dds.metamapa_client.services.ConexionServicioUser;
import ar.edu.utn.frba.dds.metamapa_client.services.IConexionServicioUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthProviderCreado implements AuthenticationProvider {
  private final IConexionServicioUser conexionServicioUser;

  public AuthProviderCreado(IConexionServicioUser conexionServicioUser) {
    this.conexionServicioUser = conexionServicioUser;
  }
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String password = authentication.getCredentials().toString();
    String email = authentication.getName();
    try {
      AuthResponseDTO tokensDeAcceso = this.conexionServicioUser.getTokens(email, password);
      if(tokensDeAcceso == null) {
        throw new FalloEnLaAutenticacion("No se pudo recuperar el token");
      }
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
      HttpServletRequest request = attributes.getRequest();

      request.getSession().setAttribute("accessToken", tokensDeAcceso.getAccessToken());
      request.getSession().setAttribute("refreshToken", tokensDeAcceso.getRefreshToken());
      request.getSession().setAttribute("email", email);
      //request.getSession().setAttribute("idUsuario", tokensDeAcceso.getTokenAcceso());

      RolesPermisosDTO rolesPermisos = conexionServicioUser.getRolesPermisos(tokensDeAcceso.getAccessToken());

      request.getSession().setAttribute("rol", rolesPermisos.getRol());
      request.getSession().setAttribute("permisos", rolesPermisos.getPermisos());
      List<GrantedAuthority> authorities = new ArrayList<>();
      rolesPermisos.getPermisos().forEach(permiso -> {
        authorities.add(new SimpleGrantedAuthority(permiso));
      });
      authorities.add(new SimpleGrantedAuthority("ROLE_" + rolesPermisos.getRol()));

      return new UsernamePasswordAuthenticationToken(email, password, authorities);
    } catch (Exception e) {
      throw new FalloEnLaAutenticacion("Hubo un error en la autenticación: " + e.getMessage());
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }
}
