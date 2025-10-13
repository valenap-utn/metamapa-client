package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HechoDTOOutput {
  private Long id;
  private String titulo;
  private String descripcion;
  private Categoria categoria;
  private Ubicacion ubicacion;
  private LocalDateTime fechaAcontecimiento;
  private LocalDateTime fechaCarga;
  private Set<String> etiquetas;
  private boolean eliminado;
  private ContenidoMultimedia contenidoMultimedia;
  private Long idUsuario;
  private OrigenDTO origen;

  private String estado;
  private LocalDateTime fechaAprobacion;

  private LocalDateTime fechaDeBaja; //para saber cuando se eliminó
}
