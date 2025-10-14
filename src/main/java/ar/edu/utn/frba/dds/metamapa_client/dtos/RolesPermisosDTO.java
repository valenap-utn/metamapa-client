package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class RolesPermisosDTO {

  @JsonProperty("rol")
  private String rol;
  @JsonProperty("permisos")
  private List<String> permisos;

}
