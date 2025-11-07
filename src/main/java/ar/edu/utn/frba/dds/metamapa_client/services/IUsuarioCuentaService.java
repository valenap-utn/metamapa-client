package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;

public interface IUsuarioCuentaService {
  UsuarioDTO ensureFromOAuth(String email, String nombre, String provider, String roleHint);

  UsuarioDTO obtenerUsuarioActual(HttpSession session, Authentication authentication);

  UsuarioDTO findById(Long id);

  UsuarioDTO findByEmail(String email);

}
