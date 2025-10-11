package ar.edu.utn.frba.dds.metamapa_client.services;

import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.CredencialesUserDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import ar.edu.utn.frba.dds.metamapa_client.exceptions.FalloEnLaAutenticacion;
import ar.edu.utn.frba.dds.metamapa_client.exceptions.ServicioDesconectado;
import ar.edu.utn.frba.dds.metamapa_client.exceptions.UsuarioNoEncontrado;
import ar.edu.utn.frba.dds.metamapa_client.services.internal.WebApiCallerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


public class ConexionServicioUser implements IConexionServicioUser {
  private final WebApiCallerService webApiCallerService;
//  private final WebClient webClient;
  private final WebClient.Builder webClientBuilder;

  @Value("${api.servicioUsuarios.url}")
  private String baseUrl;

  private WebClient webClient;

  ConexionServicioUser(WebApiCallerService webApiCallerService, WebClient.Builder webClientBuilder) {
    this.webApiCallerService = webApiCallerService;
    this.webClientBuilder = webClientBuilder;
  }

  // El PostConstruct es para armar el baseUrl,
  // que para el momento del Constructor todavía no fue Inyectado
  @PostConstruct
  void init(){
    this.webClient = webClientBuilder.baseUrl(baseUrl).build();
  }

  public AuthResponseDTO getTokens(String username, String password) {
    try {
      CredencialesUserDTO credenciales = new CredencialesUserDTO(username, password);
      return this.webClient.post().uri(uriBuilder ->
                      uriBuilder.path("/api/auth").build()).bodyValue(credenciales)
              .retrieve().bodyToMono(AuthResponseDTO.class).block();
    }catch (WebClientResponseException e) { //// errores HTTP
      //log.error(e.getMessage());
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new UsuarioNoEncontrado("Las credenciales de usuario no se encuentran en el sistema");
      } else if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new FalloEnLaAutenticacion("El email o la password no son válidos");
      }
      throw new RuntimeException("Error en el servicio de autenticación: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new ServicioDesconectado("Error de conexión con el servicio de autenticación: " + e.getMessage());
    }
  }

  public RolesPermisosDTO getRolesPermisos(String tokenAcceso) {
    try {
      return webApiCallerService.getWithAuth(
              baseUrl + "/api/auth/user/roles-permisos",
              tokenAcceso,
              RolesPermisosDTO.class
      );
    } catch (Exception e) {
      throw new RuntimeException("Error al obtener roles y permisos: " + e.getMessage(), e);
    }
  }

//  public UsuarioDTO crearUsuario(UsuarioDTO alumnoDTO) {
//    UsuarioDTO response = webApiCallerService.post(baseUrl + "/api/users", alumnoDTO, UsuarioDTO.class);
//    if (response == null) {
//      throw new RuntimeException("Error al crear alumno en el servicio externo");
//    }
//    return response;
//  }

  // Para usuario actual (lee el accessToken de sesión y maneja el refresh)
  public UsuarioDTO getMe() {
    return webApiCallerService.get(baseUrl + "/api/auth/me", UsuarioDTO.class);
  } // a chequear,,,

  // REGISTRO
  public UsuarioDTO crearUsuario(UsuarioDTO dto) {
    return this.webClient.post()
        .uri("/api/users")
        .bodyValue(dto)
        .retrieve()
        .bodyToMono(UsuarioDTO.class)
        .block();
  }
}
