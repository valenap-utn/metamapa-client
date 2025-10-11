package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEdicionDTO;

public interface IFuenteDinamica {
  HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl);

  HechoDTOOutput actualizarHecho(HechoDTOInput hecho, String baseUrl);

  HechoDTOOutput revisarHecho(Long idHecho, String baseUrl);

  SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl);

  SolicitudEdicionDTO  procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO);
}
