package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Data;

@Data
public class AuthResponseDTO {
  private String accessToken;
  private String refreshToken;
}
