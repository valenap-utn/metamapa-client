package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import lombok.Data;

@Data
public class ContenidoMultimedia {
  private String nombre;
  private String path;
  private Boolean esVideo = false;
}
