package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColeccionDTOInput {
  private String titulo;
  private String descripcion;
  private Long usuario;
  private List<FuenteDTO> fuentes = new ArrayList<>();
  private List<CriterioDTO> criterios = new ArrayList<>();
  private String algoritmo;
}
