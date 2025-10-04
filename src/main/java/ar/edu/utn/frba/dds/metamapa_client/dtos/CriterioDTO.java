package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CriterioDTO {
  private String tipo;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaCargaInicial;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaCargaFinal;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaAcontecimientoInicial;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaAcontecimientoFinal;
  private String titulo;
  private String descripcion;
  private String categoria;
  private Ubicacion ubicacion;
}
