package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.clients.utils.JwtUtil;
import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.Categoria;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ContenidoMultimedia;
import ar.edu.utn.frba.dds.metamapa_client.dtos.CriterioDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.FuenteDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.OrigenDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEdicionDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEliminacionDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.Ubicacion;
import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.usuarios.Permiso;
import ar.edu.utn.frba.dds.metamapa_client.services.IConexionServicioUser;
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
public class ClientSeader implements IFuenteDinamica, IFuenteEstatica, IServicioAgregador, IConexionServicioUser {
  private final Map<UUID, ColeccionDTOOutput> coleccion = new HashMap<>();
  private final Map<Long, HechoDTOOutput> hechos = new HashMap<>();
  private final Map<Long, SolicitudEliminacionDTO> solicitudesEliminacion = new HashMap<>();
  private final Map<Long, SolicitudEdicionDTO> solicitudesEdicion = new HashMap<>();
  private final Map<String, UsuarioDTO> usuarioDTO = new HashMap<>();
  private final Map<Long, UsuarioDTO> usuariosById = new HashMap<>();
  private final AtomicLong idHecho = new AtomicLong(1);
  private final AtomicLong idSolicitudEliminacion = new AtomicLong(1);
  private final AtomicLong idSolicitudEdicion = new AtomicLong(1);
  private final AtomicLong idUsuario = new AtomicLong(1);
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
    criterioFechaCarga.setTipo("FECHACARGA");
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
            .ubicacion(new Ubicacion(-34.914536f,-60.035774f))
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
            .ubicacion(new Ubicacion(-27.691883f,-67.052886f))
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
            .ubicacion(new Ubicacion(-25.575639f,-54.543917f))
            .id(this.idHecho.getAndIncrement())
            .build();

    this.hechos.put(hecho.getId(), hecho);
    this.hechos.put(hecho2.getId(), hecho2);
    this.hechos.put(hecho3.getId(), hecho3);
    //Usuario contribuyente = Usuario.of(new Contribuyente(), "Carlos", "Romualdo");
    //Usuario administrador = Usuario.of(new Administrador(), "Josefina", "Mariel");

    //Más ejemplitos para ver funcionamiento de mis-hechos y editar

    // aniado descripción en los 3 ejemplos iniciales
    hecho.setDescripcion("Evento de inundación en Bahía Blanca, cargado por admin.");
    hecho2.setDescripcion("Inundación reportada por contribuyente. Ejemplo editable si <= 7 días.");
    hecho3.setDescripcion("Reporte similar, con origen PROXY.");

    // Agrego más hechos del contribuyente para probar el listado y el 7-días
    for (int i = 1; i <= 18; i++) {
      LocalDateTime carga = LocalDateTime.now().minusDays(i % 11); // mezcla 0..10 días (algunos >7)
      HechoDTOOutput hx = HechoDTOOutput.builder()
          .titulo("Hecho contribuyente #" + i)
          .descripcion("Descripción de ejemplo " + i + ". Carga hace " + (i % 11) + " día(s).")
          .categoria(new Categoria((i % 2 == 0) ? "Inundacion" : "Terremoto"))
          .fechaCarga(carga)
          .fechaAcontecimiento(LocalDateTime.of(2025, 3, 7, 0, 0).minusDays(i))
          .contenidoMultimedia(contenidoMultimedia)
          .origen((i % 3 == 0) ? origenProxy : origenDinamica)
          .ubicacion(new Ubicacion(-34.60f + i * 0.01f, -58.38f - i * 0.01f))
          .idUsuario(usuarioContribuyente) // todos pertenecen al contribuyente
          .id(this.idHecho.getAndIncrement())
          .build();
      this.hechos.put(hx.getId(), hx);
    }

    //Ejemplos de solicitudes de eliminación !
    SolicitudEliminacionDTO sol1 = new SolicitudEliminacionDTO();
    sol1.setId(idSolicitudEliminacion.getAndIncrement());
    sol1.setIdHecho(hecho2.getId());
    sol1.setIdusuario(usuarioContribuyente);
    sol1.setJustificacion("El hecho contiene información cuestionable");
    sol1.setEstado("PENDIENTE");
    this.solicitudesEliminacion.put(sol1.getId(), sol1);

    SolicitudEliminacionDTO sol2 = new SolicitudEliminacionDTO();
    sol2.setId(idSolicitudEliminacion.getAndIncrement());
    sol2.setIdHecho(hecho2.getId());
    sol2.setIdusuario(usuarioContribuyente2);
    sol2.setJustificacion("Información incorrecta en la ubicación");
    sol2.setEstado("PENDIENTE");
    this.solicitudesEliminacion.put(sol2.getId(), sol2);

    SolicitudEliminacionDTO sol3 = new SolicitudEliminacionDTO();
    sol3.setId(idSolicitudEliminacion.getAndIncrement());
    sol3.setIdHecho(hecho3.getId());
    sol3.setIdusuario(usuarioContribuyente);
    sol3.setJustificacion("Ya fue aprobada en otra fuente");
    sol3.setEstado("ACEPTAR");
    this.solicitudesEliminacion.put(sol3.getId(),sol3);

    SolicitudEliminacionDTO sol4 = new SolicitudEliminacionDTO();
    sol4.setId(idSolicitudEliminacion.getAndIncrement());
    sol4.setIdHecho(hecho.getId());
    sol4.setIdusuario(usuarioContribuyente2);
    sol4.setJustificacion("No cumple criterios mínimos");
    sol4.setEstado("CANCELADA");
    this.solicitudesEliminacion.put(sol4.getId(),sol4);

  }

  @Override
  public HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl) {
    HechoDTOOutput hechoParseado = this.toHechoDTOOutput(hecho);
    Long id = this.idHecho.getAndIncrement();
    hechoParseado.setId(id);
    if(hechoParseado.getFechaCarga() == null){
      hechoParseado.setFechaCarga(LocalDateTime.now());
    }
    hechoParseado.setEstado("PENDIENTE");
    hechoParseado.setFechaAprobacion(null);
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

  //Para subidas de hechos
  public HechoDTOOutput aprobarHecho(Long idHecho) {
    HechoDTOOutput hecho = this.hechos.get(idHecho);
    if(hecho != null){
      hecho.setEstado("APROBAR");
      hecho.setFechaAprobacion(LocalDateTime.now());
    }
    return hecho;
  }

  public HechoDTOOutput rechazarHecho(Long idHecho) {
    HechoDTOOutput hecho = this.hechos.get(idHecho);
    if(hecho != null){
      hecho.setEstado("RECHAZAR");
//      hecho.setEliminado(true);
    }
    return hecho;
  }


  //Solicitudes de EDICIÓN
  public List<SolicitudEdicionDTO> findAllSolicitudesEdicion() {
    return new ArrayList<>(this.solicitudesEdicion.values());
  }

  @Override
  public SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl) {
    if(solicitudEdicion == null)return null;
    Long idSolicitudEdicion = this.idSolicitudEdicion.getAndIncrement();
    solicitudEdicion.setId(idSolicitudEdicion);
    if(solicitudEdicion.getEstado() == null)solicitudEdicion.setEstado("PENDIENTE");
    if(solicitudEdicion.getFechaSolicitud() == null)solicitudEdicion.setFechaSolicitud(LocalDateTime.now());
//    this.solicitudesEdicion.put(solicitudEdicion.getId(), solicitudEdicion);
    this.solicitudesEdicion.put(idSolicitudEdicion, solicitudEdicion);
    return solicitudEdicion;
  }

  @Override
  public SolicitudEdicionDTO procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO) {
    SolicitudEdicionDTO solicitudEdicionDTO = this.solicitudesEdicion.get(idSolicitud);
    if(solicitudEdicionDTO == null)return null;
    String nuevoEstado = revisionDTO != null && revisionDTO.getEstado() != null ? revisionDTO.getEstado() : "CANCELADA";

    solicitudEdicionDTO.setEstado(nuevoEstado);
    solicitudEdicionDTO.setJustificacion(revisionDTO != null ? revisionDTO.getComentario() : null);

    if("ACEPTAR".equalsIgnoreCase(nuevoEstado)){
      HechoDTOOutput original = this.hechos.get(solicitudEdicionDTO.getIdHecho());
      HechoDTOInput p = solicitudEdicionDTO.getPropuesta();
      if(original != null && p != null){
        if(p.getTitulo() != null){
          original.setTitulo(p.getTitulo());
        }
        if(p.getDescripcion() != null){
          original.setDescripcion(p.getDescripcion());
        }
        if(p.getCategoria() != null){
          original.setCategoria(p.getCategoria());
        }
        if(p.getUbicacion() != null){
          original.setUbicacion(p.getUbicacion());
        }
        if(p.getFechaAcontecimiento() != null){
          original.setFechaAcontecimiento(p.getFechaAcontecimiento());
        }
        if(original.getEstado() == null || !"APROBAR".equalsIgnoreCase(original.getEstado())){
          original.setEstado("APROBAR");
        }
        original.setFechaAprobacion(LocalDateTime.now());
      }
    }
    // Si queremos que desaparezca de la bandeja al resolverse
    // this.solicitudesEdicion.remove(idSolicitud);
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


  @Override //retorna solo los hechos NO eliminados
  public List<HechoDTOOutput> findAllHechos(FiltroDTO filtro) {
    return this.hechos.values().stream()
        .filter(h -> !h.isEliminado())

        //Excluimos a los que tienen la solicitud de eliminación aceptada
        .filter(h -> this.solicitudesEliminacion.values().stream()
            .noneMatch(sol ->
                sol.getIdHecho().equals(h.getId()) &&
                    "ACEPTAR".equalsIgnoreCase(sol.getEstado())))

        .toList();
  }

  @Override
  public List<HechoDTOOutput> findHechosByColeccionId(UUID coleccionId, FiltroDTO filtro) {
    List<HechoDTOOutput> hechos = new ArrayList<>(this.hechos.values().stream().toList());
    Collections.shuffle(hechos);
    return hechos.subList(0, 3);
  }

  //Solicitudes de ELIMINACION

  @Override
  public List<SolicitudEliminacionDTO> findAllSolicitudes() {
    return new ArrayList<>(this.solicitudesEliminacion.values());
  }

  @Override
  public SolicitudEliminacionDTO crearSolicitud(SolicitudEliminacionDTO solicitudEliminacionDTO) {
    Long id = this.idSolicitudEliminacion.getAndIncrement();
    solicitudEliminacionDTO.setId(id);
    solicitudEliminacionDTO.setEstado("PENDIENTE");
    this.solicitudesEliminacion.put(id, solicitudEliminacionDTO);
    return solicitudEliminacionDTO;
  }

  @Override
  public SolicitudEliminacionDTO cancelarSolicitud(Long idSolicitud) {
    SolicitudEliminacionDTO solicitudEliminacionDTO = this.solicitudesEliminacion.get(idSolicitud);
    solicitudEliminacionDTO.setEstado("CANCELADA");
    return solicitudEliminacionDTO;
  }

  @Override
  public SolicitudEliminacionDTO aceptarSolicitud(Long idSolicitud) {
    SolicitudEliminacionDTO solicitudEliminacionDTO = this.solicitudesEliminacion.get(idSolicitud);
    if(solicitudEliminacionDTO == null)return null;

    solicitudEliminacionDTO.setEstado("ACEPTAR");

    HechoDTOOutput hecho = this.hechos.get(solicitudEliminacionDTO.getIdHecho());
    if(hecho != null){
      hecho.setEliminado(true);
      hecho.setFechaDeBaja(LocalDateTime.now());
    }
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

  @Override
  public HechoDTOOutput getHecho(Long idHecho) {
    return this.hechos.get(idHecho);
  }

  @Override
  public List<ColeccionDTOOutput> findColecciones() {
    return this.coleccion.values().stream().toList();
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


  public List<HechoDTOOutput> listHechosDelUsuario(Long userId) {
    return this.hechos.values().stream()
        .filter(h -> userId != null && userId.equals(h.getIdUsuario()))
        .filter(h -> !h.isEliminado())
        .sorted((a,b) -> {
          var fa = a.getFechaCarga(); var fb = b.getFechaCarga();
          if (fa == null && fb == null) return 0;
          if (fa == null) return 1;
          if (fb == null) return -1;
          return fb.compareTo(fa); // DESC
        })
        .toList();
  }

  @Override
  public AuthResponseDTO getTokens(String username, String password) {
    UsuarioDTO usuarioDTO = this.usuarioDTO.get(username);
    AuthResponseDTO responseDTO = new AuthResponseDTO();
    responseDTO.setAccessToken(JwtUtil.generarAccessToken(usuarioDTO));
    responseDTO.setRefreshToken(JwtUtil.generarRefreshToken(usuarioDTO));
    return responseDTO;
  }

  @Override
  public RolesPermisosDTO getRolesPermisos(String tokenAcceso) {
    String email = JwtUtil.validarToken(tokenAcceso);
    UsuarioDTO usuarioDTO = this.usuarioDTO.get(email);
    RolesPermisosDTO rolesPermisosDTO = new RolesPermisosDTO();
    rolesPermisosDTO.setRol(usuarioDTO.getRol().toUpperCase());
    rolesPermisosDTO.setPermisos(List.of(Permiso.CREARCOLECCION.name(), Permiso.ELIMINARCOLECCION.name()));
    return  rolesPermisosDTO;
  }

  @Override
  public UsuarioDTO crearUsuario(UsuarioDTO dto) {
    Long id = this.idUsuario.getAndIncrement();
    dto.setId(id);
    this.usuarioDTO.put(dto.getEmail(), dto);
    this.usuariosById.put(id,dto);
    return dto;
  }

  @Override
  public UsuarioDTO getMe() {
    return null;
  }

  public String getNombreUsuario(Long id) {
    UsuarioDTO usuario = this.usuariosById.get(id);
    if(usuario == null) return "Usuario " + id;
    String nombre = (usuario.getNombre() != null ? usuario.getNombre() : "");
    String apellido = (usuario.getApellido() != null ? usuario.getApellido() : "");
    String nombreCompleto = (nombre + " " + apellido).trim();
    return nombreCompleto.isEmpty() ? "Usuario " + id : nombreCompleto;
  }
}
