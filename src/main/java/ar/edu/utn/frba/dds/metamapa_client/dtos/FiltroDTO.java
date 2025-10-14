package ar.edu.utn.frba.dds.metamapa_client.dtos;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class FiltroDTO {
  String categoria;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  LocalDateTime fecha_reporte_desde;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  LocalDateTime fecha_reporte_hasta;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  LocalDateTime fecha_acontecimiento_desde;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  LocalDateTime fecha_acontecimiento_hasta;
  Float latitud;
  Float longitud;
  Boolean curada = false;
  Boolean entiemporeal = false;

  // para georef API
  private String provincia;
  private String municipio;
  private String departamento;

}
