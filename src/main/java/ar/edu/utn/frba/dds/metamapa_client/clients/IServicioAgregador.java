package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEliminacionDTO;
import java.util.List;
import java.util.UUID;

public interface IServicioAgregador {
  List<HechoDTOOutput> findAllHechos(FiltroDTO filtro);

  List<HechoDTOOutput> findHechosByColeccionId(UUID coleccionId, FiltroDTO filtro);

  List<SolicitudEliminacionDTO> findAllSolicitudes();

  SolicitudEliminacionDTO crearSolicitud(SolicitudEliminacionDTO solicitudEliminacionDTO);

  SolicitudEliminacionDTO cancelarSolicitud(Long idSolicitud);

  SolicitudEliminacionDTO aceptarSolicitud(Long idSolicitud);

  ColeccionDTOOutput modificarColeccion(ColeccionDTOInput coleccionDTOInput, UUID coleccionId);

  ColeccionDTOOutput eliminarColeccion(UUID idColeccion);

  ColeccionDTOOutput crearColeccion(ColeccionDTOInput coleccion);
}
