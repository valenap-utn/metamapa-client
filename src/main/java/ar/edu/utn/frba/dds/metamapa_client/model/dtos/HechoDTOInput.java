package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HechoDTOInput {
  private String titulo;
  private Long id;
  private String descripcion;
  private Categoria categoria;
  private Ubicacion ubicacion;
  private LocalDateTime fechaAcontecimiento;
  private LocalDateTime fechaCarga;
  private ContenidoMultimedia contenidoMultimedia;
  private String contenido;
  private Long idUsuario;
}
