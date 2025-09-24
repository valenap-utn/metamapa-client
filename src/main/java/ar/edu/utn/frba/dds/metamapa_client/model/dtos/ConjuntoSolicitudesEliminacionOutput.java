package ar.edu.utn.frba.dds.metamapa_client.model.dtos;

import java.util.List;
import lombok.Data;

@Data
public class ConjuntoSolicitudesEliminacionOutput {
  private List<SolicitudEliminacionDTO> solicitudes;
}
