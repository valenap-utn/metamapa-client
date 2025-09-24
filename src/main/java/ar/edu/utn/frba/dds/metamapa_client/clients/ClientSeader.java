package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEdicionDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEliminacionDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ClientSeader implements IFuenteDinamica, IFuenteEstatica, IServicioAgregador {
  @Override
  public HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl) {
    return null;
  }

  @Override
  public HechoDTOOutput actualizarHecho(HechoDTOInput hecho, String baseUrl) {
    return null;
  }

  @Override
  public HechoDTOOutput revisarHecho(Long idHecho, String baseUrl) {
    return null;
  }

  @Override
  public SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl) {
    return null;
  }

  @Override
  public SolicitudEdicionDTO procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO) {
    return null;
  }

  @Override
  public String subirHechosCSV(MultipartFile archivo, Long idUsuario, String baseURL) {
    return "";
  }


  @Override
  public List<HechoDTOOutput> findAllHechos(FiltroDTO filtro) {
    return List.of();
  }

  @Override
  public List<HechoDTOOutput> findHechosByColeccionId(UUID coleccionId, FiltroDTO filtro) {
    return List.of();
  }

  @Override
  public List<SolicitudEliminacionDTO> findAllSolicitudes() {
    return List.of();
  }

  @Override
  public SolicitudEliminacionDTO crearSolicitud(SolicitudEliminacionDTO solicitudEliminacionDTO) {
    return null;
  }

  @Override
  public SolicitudEliminacionDTO cancelarSolicitud(Long idSolicitud) {
    return null;
  }

  @Override
  public SolicitudEliminacionDTO aceptarSolicitud(Long idSolicitud) {
    return null;
  }

  @Override
  public ColeccionDTOOutput modificarColeccion(ColeccionDTOInput coleccionDTOInput, UUID coleccionId) {
    return null;
  }

  @Override
  public ColeccionDTOOutput eliminarColeccion(UUID idColeccion) {
    return null;
  }

  @Override
  public ColeccionDTOOutput crearColeccion(ColeccionDTOInput coleccion) {
    return null;
  }


}
