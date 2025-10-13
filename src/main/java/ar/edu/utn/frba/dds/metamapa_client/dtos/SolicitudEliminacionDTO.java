package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudEliminacionDTO {
  private Long id;
  private Long idHecho;
  private Long idusuario;
  private String justificacion;
  private String estado;
  private LocalDateTime fechaSolicitud;
}
