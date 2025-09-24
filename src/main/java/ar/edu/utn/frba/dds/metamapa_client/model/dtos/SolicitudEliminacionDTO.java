package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import lombok.Data;

@Data
public class SolicitudEliminacionDTO {
  private Long id;
  private Long idHecho;
  private Long idusuario;
  private String justificacion;
  private String estado;
}
