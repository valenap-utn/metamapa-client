package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RolesPermisosDTO {
//  private String Rol;
//  private List<String> Permisos;

  @JsonProperty("rol")
  private String rol;
  @JsonProperty("permisos")
  private List<String> permisos;

}
