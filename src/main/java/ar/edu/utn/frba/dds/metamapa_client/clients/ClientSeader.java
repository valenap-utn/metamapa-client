package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.Categoria;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ContenidoMultimedia;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.CriterioDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.FuenteDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.OrigenDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEdicionDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEliminacionDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ClientSeader implements IFuenteDinamica, IFuenteEstatica, IServicioAgregador {
  private final Map<UUID, ColeccionDTOOutput> coleccion = new HashMap<>();
  private final Map<Long, HechoDTOOutput> hechos = new HashMap<>();
  private final Map<Long, SolicitudEliminacionDTO> solicitudesEliminacion = new HashMap<>();
  private final Map<Long, SolicitudEdicionDTO> solicitudesEdicion = new HashMap<>();
  private final AtomicLong idHecho = new AtomicLong(1);
  private final AtomicLong idSolicitudEliminacion = new AtomicLong(1);
  private final AtomicLong idSolicitudEdicion = new AtomicLong(1);
  private final Long usuarioAdmin = 1L;
  private final Long usuarioContribuyente = 2L;
  private final Long usuarioContribuyente2 = 3L;

  public ClientSeader() {
    OrigenDTO origenDinamica = new OrigenDTO();
    origenDinamica.setUrl("http://localhost:4000");
    origenDinamica.setTipo("DINAMICA");
    OrigenDTO origenProxy = new OrigenDTO();
    origenProxy.setUrl("http://localhost:6000");
    origenProxy.setTipo("PROXY");

    FuenteDTO fuenteColeccionDinamica = new FuenteDTO();
      fuenteColeccionDinamica.setTipoOrigen("DINAMICA");
      fuenteColeccionDinamica.setUrl("http://localhost:4000");
    FuenteDTO fuenteColeccionProxy = new FuenteDTO();
    fuenteColeccionProxy.setTipoOrigen("PROXY");
    fuenteColeccionProxy.setUrl("http://localhost:6000");
    FuenteDTO fuenteColeccionEstatica = new FuenteDTO();
    fuenteColeccionEstatica.setTipoOrigen("ESTATICA");
    fuenteColeccionEstatica.setUrl("http://localhost:5000");

    ColeccionDTOOutput coleccion = new ColeccionDTOOutput();

    ColeccionDTOOutput coleccion2 = new ColeccionDTOOutput();
    coleccion.setId(UUID.randomUUID());
    coleccion.setTitulo("Desastres Naturales");
    coleccion.setDescripcion("Se muestran los desastres Naturales ocurridos en Buenos Aires, Argentina");
    coleccion.setAlgoritmoDeConsenso("TODOSCONSENSUADOS");
    coleccion.setFuentes(List.of(fuenteColeccionEstatica, fuenteColeccionDinamica));

    CriterioDTO criterioFechaCarga = new CriterioDTO();
    criterioFechaCarga.setFechaCargaInicial(LocalDate.of(2020, 1, 12).atStartOfDay());
    criterioFechaCarga.setFechaCargaFinal(LocalDateTime.now());
    coleccion.setCriterios(List.of(criterioFechaCarga));

    coleccion2.setId(UUID.randomUUID());
    coleccion2.setTitulo("Terremotos");
    coleccion2.setDescripcion("Tiene los terremotos que ocurren cerca de la cordillera de los andes");
    coleccion2.setAlgoritmoDeConsenso("MAYORIASIMPLE");
    coleccion2.setFuentes(List.of(fuenteColeccionDinamica, fuenteColeccionProxy));

    ContenidoMultimedia contenidoMultimedia = new ContenidoMultimedia();
    //Usuario contribuyente = Usuario.of(new Contribuyente(), "Carlos", "Romualdo");
    //Usuario contribuyente2 = Usuario.of(new Contribuyente(), "Josefina", "Mariel");

    this.coleccion.put(coleccion.getId(), coleccion);
    this.coleccion.put(coleccion2.getId(), coleccion2);
    // Creación de hechos



    HechoDTOOutput hecho = HechoDTOOutput.builder()
            .titulo("Inundacion Bahia Blanca")
            .categoria(new Categoria("Inundacion"))
            .fechaCarga(LocalDateTime.now())
            .fechaAcontecimiento(LocalDateTime.of(2025, 3, 7, 0, 0))
            .contenidoMultimedia(contenidoMultimedia)
            .origen(origenDinamica)
            .idUsuario(usuarioAdmin)
            .id(this.idHecho.getAndIncrement())
            .build();

    HechoDTOOutput hecho2 = HechoDTOOutput.builder()
            .contenidoMultimedia(contenidoMultimedia)
            .fechaCarga(LocalDateTime.now())
            .titulo("Bahia Blanca se inunda")
            .categoria(new Categoria("Inundacion"))
            .fechaAcontecimiento(LocalDateTime.of(2025, 3, 7, 0 , 0))
            .idUsuario(usuarioContribuyente)
            .origen(origenDinamica)
            .id(this.idHecho.getAndIncrement())
            .build();

    HechoDTOOutput hecho3 = HechoDTOOutput.builder().origen(origenProxy)
            .contenidoMultimedia(contenidoMultimedia)
            .fechaCarga(LocalDateTime.now())
            .titulo("Bahia Blanca se inunda")
            .categoria(new Categoria("Inundacion"))
            .fechaAcontecimiento(LocalDateTime.of(2025, 3, 7, 0 , 0))
            .idUsuario(usuarioContribuyente)
            .id(this.idHecho.getAndIncrement())
            .build();

    this.hechos.put(hecho.getId(), hecho);
    this.hechos.put(hecho2.getId(), hecho2);
    this.hechos.put(hecho3.getId(), hecho3);
    //Usuario contribuyente = Usuario.of(new Contribuyente(), "Carlos", "Romualdo");
    //Usuario administrador = Usuario.of(new Administrador(), "Josefina", "Mariel");

  }

  @Override
  public HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl) {
    HechoDTOOutput hechoParseado = this.toHechoDTOOutput(hecho);
    Long id = this.idHecho.getAndIncrement();
    hechoParseado.setId(id);
    this.hechos.put(id, hechoParseado);
    return hechoParseado;
  }

  private HechoDTOOutput toHechoDTOOutput(HechoDTOInput hecho) {
    OrigenDTO origenDTO = new OrigenDTO();
    origenDTO.setTipo("DINAMICA");
    origenDTO.setUrl("http://localhost:4000");
    return HechoDTOOutput.builder()
            .idUsuario(hecho.getIdUsuario())
            .fechaAcontecimiento(hecho.getFechaAcontecimiento())
            .fechaCarga(hecho.getFechaCarga())
            .ubicacion(hecho.getUbicacion())
            .contenidoMultimedia(hecho.getContenidoMultimedia())
            .categoria(hecho.getCategoria())
            .titulo(hecho.getTitulo())
            .descripcion(hecho.getDescripcion())
            .origen(origenDTO).build();
  }

  @Override
  public HechoDTOOutput actualizarHecho(HechoDTOInput hecho, String baseUrl) {
    HechoDTOOutput hechoParseado = this.toHechoDTOOutput(hecho);
    HechoDTOOutput hechoEncontrado = this.hechos.get(hecho.getId());
    hechoParseado.setId(hechoEncontrado.getId());
    this.hechos.put(hecho.getId(), hechoParseado);
    return hechoParseado;
  }

  @Override
  public HechoDTOOutput revisarHecho(Long idHecho, String baseUrl) {
    return this.hechos.get(idHecho);
  }

  @Override
  public SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl) {
    Long idSolicitudEdicion = this.idSolicitudEdicion.getAndIncrement();
    solicitudEdicion.setId(idSolicitudEdicion);
    this.solicitudesEdicion.put(solicitudEdicion.getId(), solicitudEdicion);
    return solicitudEdicion;
  }

  @Override
  public SolicitudEdicionDTO procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO) {
    SolicitudEdicionDTO solicitudEdicionDTO = this.solicitudesEdicion.get(idSolicitud);
    solicitudEdicionDTO.setEstado(revisionDTO.getEstado());
    solicitudEdicionDTO.setJustificacion(revisionDTO.getComentario());
    return solicitudEdicionDTO;
  }

  @Override
  public String subirHechosCSV(MultipartFile archivo, Long idUsuario, String baseURL) {
    OrigenDTO origenDTO = new OrigenDTO();
    origenDTO.setTipo("ESTATICA");
    origenDTO.setUrl("http://localhost:5000");
    for (int i=0; i < 5; i++){
      HechoDTOOutput hecho = HechoDTOOutput.builder()
              .titulo("Inundacion Bahia Blanca " + i + " prueba")
              .categoria(new Categoria("Inundacion"))
              .fechaCarga(LocalDateTime.now())
              .fechaAcontecimiento(LocalDateTime.of(2025, 3, 7, 0, 0))
              .origen(origenDTO)
              .idUsuario(usuarioAdmin)
              .build();
      hecho.setId(this.idHecho.getAndIncrement());
      this.hechos.put(hecho.getId(), hecho);
    }
    return "Se han subido los hechos";
  }


  @Override
  public List<HechoDTOOutput> findAllHechos(FiltroDTO filtro) {
    return this.hechos.values().stream().toList();
  }

  @Override
  public List<HechoDTOOutput> findHechosByColeccionId(UUID coleccionId, FiltroDTO filtro) {
    List<HechoDTOOutput> hechos = new ArrayList<>(this.hechos.values().stream().toList());
    Collections.shuffle(hechos);
    return hechos.subList(0, 3);
  }

  @Override
  public List<SolicitudEliminacionDTO> findAllSolicitudes() {
    return new ArrayList<>(this.solicitudesEliminacion.values());
  }

  @Override
  public SolicitudEliminacionDTO crearSolicitud(SolicitudEliminacionDTO solicitudEliminacionDTO) {
    Long id = this.idSolicitudEliminacion.getAndIncrement();
    solicitudEliminacionDTO.setId(id);
    this.solicitudesEliminacion.put(id, solicitudEliminacionDTO);
    return solicitudEliminacionDTO;
  }

  @Override
  public SolicitudEliminacionDTO cancelarSolicitud(Long idSolicitud) {
    SolicitudEliminacionDTO solicitudEliminacionDTO = this.solicitudesEliminacion.get(idSolicitud);
    solicitudEliminacionDTO.setEstado("CANCELADO");
    return solicitudEliminacionDTO;
  }

  @Override
  public SolicitudEliminacionDTO aceptarSolicitud(Long idSolicitud) {
    SolicitudEliminacionDTO solicitudEliminacionDTO = this.solicitudesEliminacion.get(idSolicitud);
    solicitudEliminacionDTO.setEstado("ACEPTAR");
    HechoDTOOutput hecho = this.hechos.get(solicitudEliminacionDTO.getId());
    hecho.setEliminado(true);
    return solicitudEliminacionDTO;
  }

  @Override
  public ColeccionDTOOutput modificarColeccion(ColeccionDTOInput coleccionDTOInput, UUID coleccionId) {
    ColeccionDTOOutput coleccionDTOOutput = this.toColeccionDTOOutput(coleccionDTOInput);
    coleccionDTOOutput.setId(coleccionId);
    this.coleccion.put(coleccionId, this.toColeccionDTOOutput(coleccionDTOInput));
    return coleccionDTOOutput;
  }

  @Override
  public ColeccionDTOOutput eliminarColeccion(UUID idColeccion) {
    return this.coleccion.remove(idColeccion);
  }

  @Override
  public ColeccionDTOOutput crearColeccion(ColeccionDTOInput coleccion) {
    ColeccionDTOOutput coleccionDTOOutput = this.toColeccionDTOOutput(coleccion);
    UUID id = UUID.randomUUID();
    coleccionDTOOutput.setId(id);
    this.coleccion.put(id, coleccionDTOOutput);
    return coleccionDTOOutput;
  }

  private ColeccionDTOOutput toColeccionDTOOutput(ColeccionDTOInput coleccionDTOInput) {
    ColeccionDTOOutput coleccionDTOOutput = new ColeccionDTOOutput();
    coleccionDTOOutput.setAlgoritmoDeConsenso(coleccionDTOInput.getAlgoritmo());
    coleccionDTOOutput.setFuentes(coleccionDTOInput.getFuentes());
    coleccionDTOOutput.setTitulo(coleccionDTOInput.getTitulo());
    coleccionDTOOutput.setDescripcion(coleccionDTOInput.getDescripcion());
    coleccionDTOOutput.setCriterios(coleccionDTOInput.getCriterios());
    return coleccionDTOOutput;
  }


}
