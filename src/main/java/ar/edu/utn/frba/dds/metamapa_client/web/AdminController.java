package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.dtos.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
  private final ClientSeader agregador;
  private final DefaultErrorAttributes defaultErrorAttributes;
  private final WebClient georefWebClient;
//  private final BackendAPI api;

  public AdminController(ClientSeader agregador, /*, BackendAPI api */DefaultErrorAttributes defaultErrorAttributes, WebClient georefWebClient) {
    this.agregador = agregador;
//    this.api = api;
    this.defaultErrorAttributes = defaultErrorAttributes;
    this.georefWebClient = georefWebClient;
  }


  @GetMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String dashboard(Model model) {
    model.addAttribute("metrics", Map.of("hechos", 124, "fuentes", 8, "solicitudes", 3));
    return "admin";
  }

  // ---------- COLECCIONES ----------

  @GetMapping("/crear-coleccion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String crearColeccion(Model model) {
    model.addAttribute("coleccion", new ColeccionDTOInput());
    model.addAttribute("titulo", "Crear Coleccion");

    //Agregado provincia resp
    ProvinciaResp provinciasResponse = georefWebClient.get()
        .uri("/provincias?campos=id,nombre")
        .retrieve()
        .bodyToMono(ProvinciaResp.class)
        .block();

    model.addAttribute("provincias", provinciasResponse.getProvincias());

    return "admins/crear-coleccion";
  }

  @PostMapping("/crear-coleccion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String crearColeccionPost(@ModelAttribute("coleccion") ColeccionDTOInput coleccion, Model model, RedirectAttributes ra) {
    ColeccionDTOOutput coleccionDTOOutput = this.agregador.crearColeccion(coleccion);
    ra.addFlashAttribute("success", "Colección creada correctamente");

    return "redirect:/admin";
  }

  @GetMapping("/modificar-coleccion/{id}/editar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String modificarColeccion(@PathVariable UUID id, Model model, RedirectAttributes ra) {
    //Traemos la Coleccion
    ColeccionDTOOutput out = this.agregador.revisarColeccion(id);
    if (out == null) {
      ra.addFlashAttribute("error", "La colección no existe.");
      return "redirect:/colecciones";
    }

    //Mapeamos el Output
    ColeccionDTOInput in = new ColeccionDTOInput();
    in.setTitulo(out.getTitulo());
    in.setDescripcion(out.getDescripcion());
    in.setFuentes(out.getFuentes() != null ? out.getFuentes() : List.of());
    in.setAlgoritmo(out.getAlgoritmoDeConsenso());
    in.setCriterios(out.getCriterios() != null ? out.getCriterios() : List.of());

    model.addAttribute("coleccionId", out.getId());
    model.addAttribute("coleccion", in);
    model.addAttribute("titulo", "Modificar Coleccion");

    return "admins/modificar-coleccion";
  }

  @PostMapping("/modificar-coleccion/{id}/editar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String modificarColeccionPost(@PathVariable UUID id, @ModelAttribute("coleccion") ColeccionDTOInput in, Model model, RedirectAttributes ra) {

    try {
      ColeccionDTOOutput actualizado = this.agregador.modificarColeccion(in, id);
      if (actualizado == null) {
        model.addAttribute("error", "No se pudo actulizar la colección.");
        model.addAttribute("coleccionId", id);
        model.addAttribute("titulo", "Modificar Coleccion");
        return "redirect:/colecciones";
      }
      ra.addFlashAttribute("success", "Colección actulizada correctamente.");
      return "redirect:/colecciones";
    } catch (WebClientResponseException e) {
      // Muestra el cuerpo de error del backend
      String body = e.getResponseBodyAsString();
      model.addAttribute("error", "Error del backend: " + e.getRawStatusCode() + " " + body);
      model.addAttribute("coleccionId", id);
      model.addAttribute("titulo", "Modificar Colección");
      return "redirect:/colecciones";
    } catch (Exception e) {
      model.addAttribute("error", "Error inesperado: " + e.getMessage());
      model.addAttribute("coleccionId", id);
      model.addAttribute("titulo", "Editar Colección");
      return "redirect:/colecciones";
    }
  }

  @GetMapping("/colecciones")
  public String listarColecciones(Model model) {
    var items = agregador.findColecciones();        // <- trae las colecciones del ClientSeader
    model.addAttribute("colecciones", items);       // <- nombre que usa tu template
    model.addAttribute("titulo", "Colecciones");
    return "colecciones";                           // <- si tu archivo es templates/colecciones.html
  }

  @PostMapping("/eliminar-coleccion/{id}/eliminar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String eliminarColeccion(@PathVariable UUID id, RedirectAttributes ra) {
    var eliminada = this.agregador.eliminarColeccion(id);
    if(eliminada == null){
      ra.addFlashAttribute("error","La colección no existe o ya fue eliminada.");
    }else{
      ra.addFlashAttribute("success","Colección eliminada correctamente.");
    }
    return "redirect:/colecciones";
  }

  // ---------- IMPORTAR CSV ----------

  @GetMapping("/importar-csv")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String importarCsv() {
    return "admins/importar-csv";
  }

  @PostMapping("/importar-csv")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String importarCsvPost(@RequestParam("file") MultipartFile file) {

    this.agregador.subirHechosCSV(file, 1L, "http://localhost:5000/");
    return "admins/importar-csv";
  }

  //Para gestionar Nuevos Hechos (Aprobar o Rechazar)
  @GetMapping("/gest-nuevosHechos")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String gestNuevosHechos(Model model, @RequestParam(value = "estado", required = false, defaultValue = "TODAS") String estado, @RequestParam(value = "q", required = false, defaultValue = "") String q) {
    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(new FiltroDTO());

    long total = hechos.size();
    long pendientes = hechos.stream().filter(h -> "PENDIENTE".equals(h.getEstado())).count();
    long aprobados = hechos.stream().filter(h -> "APROBAR".equals(h.getEstado())).count();
    long rechazados = hechos.stream().filter(h -> "RECHAZAR".equals(h.getEstado())).count();

    //Filtro por Estado
    Stream<HechoDTOOutput> filtrados = hechos.stream();
    if (!"TODAS".equalsIgnoreCase(estado)) {
      filtrados = filtrados.filter(h -> estado.equalsIgnoreCase(String.valueOf(h.getEstado())));
    }

    //Filtro por búsqueda
    String qNorm = q.trim().toLowerCase();
    if (!qNorm.isEmpty()) {
      filtrados = filtrados.filter(h ->
          (h.getTitulo() != null && h.getTitulo().toLowerCase().contains(qNorm)) ||
              (h.getDescripcion() != null && h.getDescripcion().toLowerCase().contains(qNorm))
      );
    }

    List<HechoDTOOutput> filtradosList = filtrados.toList();

    model.addAttribute("hechos", filtradosList);
    model.addAttribute("estado", estado);
    model.addAttribute("q", q);

    model.addAttribute("countTodas", total);
    model.addAttribute("countPend", pendientes);
    model.addAttribute("countApro", aprobados);
    model.addAttribute("countRech", rechazados);

    model.addAttribute("titulo", "Gestionar Nuevos Hechos");
    return "admins/gest-nuevosHechos";
  }

  @PostMapping("/gest-nuevosHechos/{id}/aprobar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> aprobarHecho(@PathVariable("id") Long id) {
    this.agregador.aprobarHecho(id);
    return ResponseEntity.ok().build(); // 200 en caso de exito !
  }

  @PostMapping("/gest-nuevosHechos/{id}/rechazar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> rechazarHecho(@PathVariable("id") Long id) {
    this.agregador.rechazarHecho(id);
    return ResponseEntity.noContent().build(); //204 en caso de exito !
  }

  //Para solicitudes de Edición
  @GetMapping("/gest-solEdicion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String solicitudesEdicion(Model model, @RequestParam(value = "estado", required = false, defaultValue = "PENDIENTE") String estado, @RequestParam(value = "q", required = false, defaultValue = "") String q) {
    List<SolicitudEdicionDTO> solicitudes = this.agregador.findAllSolicitudesEdicion();

    List<Map<String, Object>> vms = solicitudes.stream()
        .filter(h -> "TODAS".equalsIgnoreCase(estado) || estado.equalsIgnoreCase(h.getEstado()))
        .map(h -> {
          HechoDTOOutput original = this.agregador.getHecho(h.getIdHecho());
          String nombreUsuario = this.agregador.getNombreUsuario(original.getIdUsuario());
          return Map.of("sol", h, "orig", original, "nombreUsuario", nombreUsuario);
        })
        .filter(vm -> {
          String qNorm = q.trim().toLowerCase();
          if (qNorm.isEmpty()) { return true; }
          HechoDTOOutput otpDTO = (HechoDTOOutput) vm.get("orig");
          SolicitudEdicionDTO solEdDTO = (SolicitudEdicionDTO) vm.get("sol");
          return (otpDTO != null && otpDTO.getTitulo() != null && otpDTO.getTitulo().toLowerCase().contains(qNorm))
              || (solEdDTO.getPropuesta() != null && solEdDTO.getPropuesta().getTitulo() != null && solEdDTO.getPropuesta().getTitulo().toLowerCase().contains(qNorm));
        }).toList();

    long total = solicitudes.size();
    long pendientes = solicitudes.stream().filter(h -> "PENDIENTE".equals(h.getEstado())).count();
    long aceptadas = solicitudes.stream().filter(h -> "ACEPTAR".equals(h.getEstado())).count();
    long canceladas = solicitudes.stream().filter(h -> "CANCELADA".equals(h.getEstado())).count();

    model.addAttribute("items", vms);
    model.addAttribute("estado", estado);
    model.addAttribute("q", q);

    model.addAttribute("countTodas", total);
    model.addAttribute("countPend", pendientes);
    model.addAttribute("countAcep", aceptadas);
    model.addAttribute("countCancel", canceladas);

    model.addAttribute("titulo", "Solicitudes de Edición");
    return "admins/gest-solEdicion";
  }

  @PostMapping("/gest-solEdicion/{idSolicitud}/aprobar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> aprobarSolicitudEdicion(@PathVariable("idSolicitud") Long idSolicitud) {
    RevisionDTO revision = new RevisionDTO();
    revision.setEstado("ACEPTAR");
    revision.setComentario("Solicitud de edición aprobada por el administrador");

    this.agregador.procesarSolicitudEdicion(idSolicitud, "http://localhost:4000/", revision);
    return ResponseEntity.noContent().build(); //204 en caso de exito !
  }

  @PostMapping("/gest-solEdicion/{idSolicitud}/rechazar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> rechazarSolicitudEdicion(@PathVariable("idSolicitud") Long idSolicitud) {
    RevisionDTO revision = new RevisionDTO();
    revision.setEstado("RECHAZAR");
//    revision.setEstado("CANCELADA");
    revision.setComentario("Edición rechazada por el administrador");

    this.agregador.procesarSolicitudEdicion(idSolicitud, "http://localhost:4000/", revision);
    return ResponseEntity.noContent().build(); //204 en caso de exito !
  }


  //Para solicitudes de eliminación
  //View-Model simple para la vista
  public static class SolicitudVM {
    public final SolicitudEliminacionDTO solicitud;
    public final String tituloHecho;
    public final String urlHecho;

    public SolicitudVM(SolicitudEliminacionDTO solicitud, String tituloHecho, String urlHecho) {
      this.solicitud = solicitud;
      this.tituloHecho = tituloHecho;
      this.urlHecho = urlHecho;
    }
  }

  //Listar con filtros y  contadores
  @GetMapping("/gest-solEliminacion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String solicitudes(Model model, @RequestParam(value = "estado", required = false, defaultValue = "TODAS") String estado,
                            @RequestParam(value = "q", required = false, defaultValue = "") String q) {
    List<SolicitudEliminacionDTO> solicitudes = this.agregador.findAllSolicitudes();

    //Enriquecemos con titulo y url del Hecho
    List<SolicitudVM> solicitudesVM = solicitudes.stream().map(s -> {
      var h = this.agregador.getHecho(s.getIdHecho());
      String titulo = (h != null && h.getTitulo() != null) ? h.getTitulo() : "-";
      String url = (h != null && h.getId() != null) ? "/hechos/" + h.getId() : "#";
      return new SolicitudVM(s, titulo, url);
    }).toList();

    //Contadores
    long total = solicitudesVM.size();
    long pendientes = solicitudesVM.stream().filter(h -> "PENDIENTE".equals(h.solicitud.getEstado())).count();
    long aceptadas = solicitudesVM.stream().filter(h -> "ACEPTAR".equals(h.solicitud.getEstado())).count();
    long canceladas = solicitudesVM.stream().filter(h -> "CANCELADA".equals(h.solicitud.getEstado())).count();

    //Filtros (por estado y/o búsqueda)
    String qNorm = q.trim().toLowerCase();
    List<SolicitudVM> filtradas = solicitudesVM.stream()
        .filter(h -> "TODAS".equalsIgnoreCase(estado) || estado.equalsIgnoreCase(h.solicitud.getEstado()))
        .filter(h -> qNorm.isEmpty()
            || (h.solicitud.getJustificacion() != null && h.solicitud.getJustificacion().toLowerCase().contains(qNorm))
            || (h.tituloHecho != null && h.tituloHecho.toLowerCase().contains(qNorm)))
        .toList();

    model.addAttribute("solicitudes", filtradas);
    model.addAttribute("estado", estado);
    model.addAttribute("q", q);

    model.addAttribute("countTodas", total);
    model.addAttribute("countPend", pendientes);
    model.addAttribute("countAcep", aceptadas);
    model.addAttribute("countCancel", canceladas);

    model.addAttribute("titulo", "Solicitudes de Eliminacion");
    return "admins/gest-solEliminacion";
  }

  //Aceptar solicitud
  @PostMapping("/gest-solEliminacion/{id}/aceptar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> aceptarSolicitud(@PathVariable("id") Long id) {
    this.agregador.aceptarSolicitud(id);
    return ResponseEntity.noContent().build(); //204 en caso de exito !
  }

  //Rechazar solicitud
  @PostMapping("/gest-solEliminacion/{id}/cancelar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> cancelarSolicitud(@PathVariable("id") Long id) {
    this.agregador.cancelarSolicitud(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/dashboard-estadisticas")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String estadisticas() {

    return "admins/dashboard-estadisticas";
  }

}
