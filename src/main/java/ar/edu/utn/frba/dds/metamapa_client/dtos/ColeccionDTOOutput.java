package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ColeccionDTOOutput {
  private UUID id;
  private String titulo;
  private String descripcion;
  private List<FuenteDTO> fuentes;
  private List<CriterioDTO> criterios = new ArrayList<>();
  private String algoritmoDeConsenso;
}
