package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Data;

@Data
public class AuthUserDTO {
  private String tokenAcceso;
  private String tokenRefresh;
}
