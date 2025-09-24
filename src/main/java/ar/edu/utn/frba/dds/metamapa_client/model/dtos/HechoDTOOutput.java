package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
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
}
