package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FiltroDTO {
  String categoria;
  LocalDate fecha_reporte_desde;
  LocalDate fecha_reporte_hasta;
  LocalDate fecha_acontecimiento_desde;
  LocalDate fecha_acontecimiento_hasta;
  Float latitud;
  Float longitud;
  Boolean curada;
  Boolean entiemporeal;
}
