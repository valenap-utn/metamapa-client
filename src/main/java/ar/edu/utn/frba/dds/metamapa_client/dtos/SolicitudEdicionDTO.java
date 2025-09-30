package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Data;

@Data
public class SolicitudEdicionDTO {
  private Long id;
  private Long idHecho;
  private Long idusuario;
  private String justificacion;
  private String estado;
}
