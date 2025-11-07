package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import ar.edu.utn.frba.dds.metamapa_client.services.internal.WebApiCallerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementación ejemplo para entorno real.
 * Acá en vez de ClientSeader hablás con tu API de usuarios.
 */
@Service
@Profile("prod")
public class UsuarioCuentaService implements IUsuarioCuentaService {

  private final WebApiCallerService webApiCallerService;

  public UsuarioCuentaService(WebApiCallerService webApiCallerService) {
    this.webApiCallerService = webApiCallerService;
  }

  @Override
  public UsuarioDTO ensureFromOAuth(String email, String nombre, String provider, String roleHint) {
    // Esquema típico:
    // 1. Intentar buscar usuario por email en el servicio real.
    // 2. Si no existe, crearlo llamando al backend.
    // 3. Devolver el usuario definitivo con su ID.

    // TODO: adaptarlo a tus endpoints reales
    // Ejemplo imaginario:
    // UsuarioDTO existente = webApiCallerService.get(baseUrl + "/api/users/by-email?email=" + email, UsuarioDTO.class);
    // if (existente != null) return existente;
    // UsuarioDTO nuevo = new UsuarioDTO(...);
    // return webApiCallerService.post(baseUrl + "/api/users", nuevo, UsuarioDTO.class);

    throw new UnsupportedOperationException("Implementar con el API real de usuarios");
  }

  @Override
  public UsuarioDTO obtenerUsuarioActual(HttpSession session, Authentication authentication) {
    // En un diseño real, lo más limpio:
    // - leer accessToken de sesión (guardado cuando te logueás contra tu backend)
    // - llamar /api/auth/me con WebApiCallerService
    try {
      return webApiCallerService.get("/api/auth/me", UsuarioDTO.class);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public UsuarioDTO findByEmail(String email) {
    // TODO: endpoint real de búsqueda por email
    throw new UnsupportedOperationException("findByEmail no implementado aún en remoto");
  }

  @Override
  public UsuarioDTO findById(Long id) {
    // TODO: endpoint real /api/users/{id}
    throw new UnsupportedOperationException("findById no implementado aún en remoto");
  }
}
