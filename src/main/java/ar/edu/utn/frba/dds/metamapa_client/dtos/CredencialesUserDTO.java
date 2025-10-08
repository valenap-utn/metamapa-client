package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredencialesUserDTO {
//  private String nombre;
  private String email;
  private String password;
}
