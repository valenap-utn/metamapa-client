package ar.edu.utn.frba.dds.metamapa_client.dtos;

import java.util.List;
import lombok.Data;

@Data
public class ConjuntoSolicitudesEdicionOutput {
  private List<SolicitudEdicionDTO> solicitudes;
}
