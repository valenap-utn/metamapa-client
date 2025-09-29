package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ubicacion {
  private Float latitud;
  private Float longitud;

  public Ubicacion(float latitud, float longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }
}
