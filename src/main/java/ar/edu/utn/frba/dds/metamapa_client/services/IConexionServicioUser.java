package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;

public interface IConexionServicioUser {
  AuthResponseDTO getTokens(String username, String password);
  RolesPermisosDTO getRolesPermisos(String tokenAcceso);
  UsuarioDTO crearUsuario(UsuarioDTO dto);
  UsuarioDTO getMe();
}
