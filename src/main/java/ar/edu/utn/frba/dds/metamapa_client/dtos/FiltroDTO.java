package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class FiltroDTO {
  String categoria;
  LocalDateTime fecha_reporte_desde;
  LocalDateTime fecha_reporte_hasta;
  LocalDateTime fecha_acontecimiento_desde;
  LocalDateTime fecha_acontecimiento_hasta;
  Float latitud;
  Float longitud;
  Boolean curada = false;
  Boolean entiemporeal = false;
}
