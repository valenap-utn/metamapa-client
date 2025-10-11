package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
//import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.StatsResp;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEliminacionDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {
  private final ClientSeader agregador;
  private final DefaultErrorAttributes defaultErrorAttributes;
//  private final BackendAPI api;

  public AdminController(ClientSeader agregador, /*, BackendAPI api */DefaultErrorAttributes defaultErrorAttributes) {
    this.agregador = agregador;
//    this.api = api;
    this.defaultErrorAttributes = defaultErrorAttributes;
  }


  @GetMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String dashboard(Model model) {
    model.addAttribute("metrics", Map.of("hechos",124, "fuentes",8, "solicitudes",3));
    return "admin";
  }

  @GetMapping("/crear-coleccion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String crearColeccion(Model model) {
    model.addAttribute("coleccion", new ColeccionDTOInput());
    model.addAttribute("titulo", "Crear Coleccion");
    return "admins/crear-coleccion";
  }

  @PostMapping("/crear-coleccion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String crearColeccionPost(@ModelAttribute("coleccion") ColeccionDTOInput coleccion, Model model) {
    ColeccionDTOOutput coleccionDTOOutput = this.agregador.crearColeccion(coleccion);

    return "admins/crear-coleccion";
  }

  @GetMapping("/modificar-coleccion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String modificarColeccion() {
    return "admins/modificar-coleccion";
  }

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

  //Para solicitudes de eliminación

  //View-Model simple para la vista
  public static class SolicitudVM {
    public final SolicitudEliminacionDTO solicitud;
    public final String tituloHecho;
    public final String urlHecho;

    public SolicitudVM(SolicitudEliminacionDTO solicitud, String tituloHecho, String urlHecho){
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
      String titulo = (h != null && h.getTitulo() != null) ? h.getTitulo() : "-" ;
      String url = (h != null && h.getId() != null) ? "/hechos/" + h.getId()  : "#" ;
      return new SolicitudVM(s,titulo,url);
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
  public ResponseEntity<Void> aceptarSolicitud(@PathVariable("id") Long id){
    this.agregador.aceptarSolicitud(id);
    return ResponseEntity.noContent().build(); //204 en caso de exito !
  }

  //Rechazar solicitud
  @PostMapping("/gest-solEliminacion/{id}/cancelar")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ResponseEntity<Void> cancelarSolicitud(@PathVariable("id") Long id){
    this.agregador.cancelarSolicitud(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/dashboard-estadisticas")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String estadisticas() {

    return "admins/dashboard-estadisticas";
  }
}
