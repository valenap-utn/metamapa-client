package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.clients.utils.JwtUtil;
import ar.edu.utn.frba.dds.metamapa_client.dtos.*;
import ar.edu.utn.frba.dds.metamapa_client.dtos.usuarios.Rol;
import ar.edu.utn.frba.dds.metamapa_client.services.ConexionServicioUser;
import ar.edu.utn.frba.dds.metamapa_client.services.IConexionServicioUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hechos")
public class HechosController {
  private final ClientSeader agregador;
  private final IConexionServicioUser servicioUsuarios;
  private final WebClient georefWebClient;

  public HechosController(ClientSeader agregador, IConexionServicioUser servicioUsuarios, WebClient georefWebClient) {
    this.agregador = agregador;
    this.servicioUsuarios = servicioUsuarios;
      this.georefWebClient = georefWebClient;
  }

  @GetMapping("/{idHecho}")
  public String hechoCompleto(@PathVariable Long idHecho, Model model, @RequestParam(value = "urlColeccion", required = false) String urlColeccion) {
    HechoDTOOutput hecho = this.agregador.getHecho(idHecho);
    model.addAttribute("titulo", "Detalle de Hecho");
    model.addAttribute("hecho", hecho);
    model.addAttribute("urlColeccion", urlColeccion);
    return "hechos/hecho-completo";
  }

  @GetMapping("/nav-hechos")
  public String navHechos(Model model) {
    FiltroDTO filtroDTO = new FiltroDTO();

    ProvinciaResp provinciasResponse = georefWebClient.get()
            .uri("/provincias?campos=id,nombre")
            .retrieve()
            .bodyToMono(ProvinciaResp.class)
            .block();

    List<UbicacionDTO> provincias = provinciasResponse.getProvincias();

    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(filtroDTO).stream()
        .filter(h-> "APROBAR".equalsIgnoreCase(h.getEstado()))
        .filter(h -> !h.isEliminado())
        .toList();

    model.addAttribute("provincias", provincias);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("titulo", "Listado de todos los hechos");
    return "hechos/nav-hechos";
  }

  @PostMapping("/nav-hechos")
  public String navHechosPost(Model model, @ModelAttribute("filtros") FiltroDTO filtroDTO) {
    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("titulo", "Listado de todos los hechos");
    return "hechos/nav-hechos";
  }

  @GetMapping("/subir-hecho")
  public String subirHecho(Model model) {
    model.addAttribute("hecho", new HechoDTOInput());
    return "hechos/subir-hecho";
  }

  @PostMapping("/subir-hecho")
  public String subirHechoPost(@Valid @ModelAttribute("hecho") HechoDTOInput hechoDtoInput, Model model, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session) {
    if(bindingResult.hasErrors()){
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.hecho", bindingResult);
      redirectAttributes.addFlashAttribute("hecho", hechoDtoInput);
      redirectAttributes.addFlashAttribute("titulo", "Revisá los campos marcados");
      return "redirect:/hechos/subir-hecho";
    }

    //Obtenemos user
    String accessToken = (String) session.getAttribute("accessToken");
    Long userId = null;
    if (accessToken != null) {
      userId = JwtUtil.getId(accessToken);
    }

    try{
      hechoDtoInput.setIdUsuario(userId);
      if(hechoDtoInput.getFechaCarga() == null){
        hechoDtoInput.setFechaCarga(LocalDateTime.now());
      }
      this.agregador.crearHecho(hechoDtoInput, "http://localhost:4000");
      redirectAttributes.addFlashAttribute("success", "Tu hecho se creó exitosamente, pronto un administrador lo estará revisado !");
      return "redirect:/main-gral";
    }catch(Exception e){
      log.error("Error al crear el hecho", e);
      redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al crear el hecho. Volvé a intentarlo");
      redirectAttributes.addFlashAttribute("hecho", hechoDtoInput);
      return "redirect:/hechos/subir-hecho";
    }
  }


  private static final Logger log = LoggerFactory.getLogger(HechosController.class);

  // Para ver Hechos subidos por uno mismo
  @GetMapping("/mis-hechos")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public String misHechos(@RequestParam(defaultValue = "12") int limit, @RequestParam(defaultValue = "12") int step, HttpSession session, Model model) {

    String accessToken = session.getAttribute("accessToken").toString();
    Long userId = JwtUtil.getId(accessToken);
    if (userId == null) return "redirect:/iniciar-sesion";

    //Hechos con solicitud de edición PENDIENTE
    Set<Long> enRevision = agregador.findAllSolicitudesEdicion().stream()
        .filter(h -> "PENDIENTE".equalsIgnoreCase(h.getEstado()))
        .map(SolicitudEdicionDTO :: getIdHecho)
        .collect(Collectors.toSet());

    // Mostramos solo todos los hechos ya APROBADOS
    List<HechoDTOOutput> all = agregador.listHechosDelUsuario(userId).stream()
        .filter(h -> "APROBAR".equalsIgnoreCase(h.getEstado()))
        .filter(h-> !enRevision.contains(h.getId()))
        .sorted((a,b)-> {
          var ka = (a.getFechaAprobacion() !=null ? a.getFechaAprobacion() : a.getFechaCarga());
          var kb = (b.getFechaAprobacion() !=null ? b.getFechaAprobacion() : b.getFechaCarga());
          if(ka == null && kb == null) return 0;
          if(ka ==null) return 1;
          if(kb ==null) return -1;
          return kb.compareTo(ka);
        }).toList();

    int total = all.size();
    int shown = Math.min(Math.max(limit,0), total);

    model.addAttribute("items", all.subList(0, shown));
    model.addAttribute("shown", shown);
    model.addAttribute("total", total);
    model.addAttribute("hasMore", shown < total);
    model.addAttribute("nextLimit", Math.min(shown + step, total));
    model.addAttribute("step", step);
    model.addAttribute("titulo", "Mis Hechos");
    return "hechos/mis-hechos";
  }

  // Para editar el Hecho
  /* Validamos existencia del Usuario actual,
   y ventana de 7 días desde fecha de Carga del Hecho
   Si falla => redirige con flash error (ver si lo queremos cambiar a esto)
   Sino => renderiza editar.html (reutilizamos subir-hecho con click-to-edit)
 */
  @GetMapping("/{idHecho}/editar")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public String editar(@PathVariable Long idHecho, HttpSession session, RedirectAttributes ra, Model model) {
    String accessToken = session.getAttribute("accessToken").toString();

    Long userId = JwtUtil.getId(accessToken);

    if(userId == null) {
        return "redirect:/iniciar-sesion";
    }

    HechoDTOOutput hecho = agregador.revisarHecho(idHecho, "http://localhost:3000");
    if (hecho == null) {
      ra.addFlashAttribute("error", "El hecho no existe.");
      return "redirect:/hechos/mis-hechos";
    }
    if (!userId.equals(hecho.getIdUsuario())) {
      ra.addFlashAttribute("error", "No podés editar un hecho que no es tuyo.");
      return "redirect:/hechos/mis-hechos";
    }

    boolean editable = hecho.getFechaCarga() != null && LocalDateTime.now().isBefore(hecho.getFechaCarga().plusDays(7));
    if(!editable){
      ra.addFlashAttribute("error", "La edición está disponible solo durante los primeros 7 días");
      return "redirect:/hechos/mis-hechos";
    }

    model.addAttribute("hecho", hecho);
    model.addAttribute("titulo", "Editar Hecho");
    return "hechos/editar";
  }

  @PostMapping("/{idHecho}/editar")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public String enviarEdicion(@PathVariable Long idHecho, @ModelAttribute("hecho")HechoDTOInput hechoDtoInput, HttpSession session, RedirectAttributes ra) {
    String accessToken = session.getAttribute("accessToken").toString();
    Long userId = JwtUtil.getId(accessToken);

    SolicitudEdicionDTO solicitud = new SolicitudEdicionDTO();
    solicitud.setIdHecho(idHecho);
    solicitud.setEstado("PENDIENTE");
    solicitud.setFechaSolicitud(LocalDateTime.now());
    solicitud.setPropuesta(hechoDtoInput);

    this.agregador.solicitarModificacion(solicitud, "http://localhost:4000");

    ra.addFlashAttribute("success","Tu edición fue enviada a revisión. Aparecerá nuevamente cuando sea aprobada.");

    return "redirect:/hechos/mis-hechos";
  }

  //Solicitudes de Eliminación
  @PostMapping("/{idHecho}/solicitud-eliminacion")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public ResponseEntity<?> crearSolicitudEliminacion(@PathVariable Long idHecho, @RequestParam String justificacion, HttpSession session){
    String accessToken = session.getAttribute("accessToken").toString();
    if(accessToken == null) return ResponseEntity.status(401).body("No autenticado");

    Long userId = JwtUtil.getId(accessToken);
    if(userId == null) return ResponseEntity.status(401).body("Token inválido");

    if(justificacion == null || justificacion.trim().length() < 500){
      return ResponseEntity.badRequest().body("La justificación debe tener al menos 500 caracteres");
    }

    SolicitudEliminacionDTO solicitud = new SolicitudEliminacionDTO();
    solicitud.setIdHecho(idHecho);
    solicitud.setIdusuario(userId);
    solicitud.setJustificacion(justificacion);
    solicitud.setEstado("PENDIENTE");
    solicitud.setFechaSolicitud(LocalDateTime.now());
    this.agregador.crearSolicitud(solicitud);
    return ResponseEntity.noContent().build(); //204 en caso de éxito!
  }


  //-------------------------------------

  record HechoDto(Long id, String titulo, String fecha, String categoria, String ubicacion) {}

  @GetMapping("/hechos/nav-hechos")
  public String navHechos(@RequestParam(defaultValue="0") int page, Model model, WebClient backend, HttpSession session) {
    var spec = backend.get().uri(uri -> uri.path("/api/hechos")
                                                    .queryParam("page", page)
                                                    .queryParam("size", 20)
                                                    .build());

    var token = (String) session.getAttribute("AUTH_TOKEN");
    if (token != null) spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    var hechos = spec.retrieve().bodyToFlux(HechoDto.class).collectList().block();
    model.addAttribute("hechos", hechos);
    model.addAttribute("page", page);
    return "hechos/nav-hechos";
  }

}
