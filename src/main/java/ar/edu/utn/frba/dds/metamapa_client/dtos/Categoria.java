package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Categoria {
  private String nombre;
  public Categoria(String nombre) {
    this.nombre = nombre;
  }
}
