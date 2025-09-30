package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthUserDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import org.springframework.stereotype.Component;

@Component
public class ConexionServicioUser {
  public AuthUserDTO getTokens(String username, String password) {
    //TODO
    return null;
  }

  public RolesPermisosDTO getRolesPermisos(String tokenAcceso) {
    return null;
  }
}
