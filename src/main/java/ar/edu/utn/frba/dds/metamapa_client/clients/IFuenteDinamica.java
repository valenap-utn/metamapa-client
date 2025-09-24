package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEdicionDTO;

public interface IFuenteDinamica {
  public HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl);

  HechoDTOOutput actualizarHecho(HechoDTOInput hecho, String baseUrl);

  HechoDTOOutput revisarHecho(Long idHecho, String baseUrl);

  SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl);

  SolicitudEdicionDTO  procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO);
}
