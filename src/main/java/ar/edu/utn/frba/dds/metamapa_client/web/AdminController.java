package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.StatsResp;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEliminacionDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
  private final BackendAPI api;

  public AdminController(ClientSeader agregador, BackendAPI api) {
    this.agregador = agregador;
    this.api = api;
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

  @GetMapping("/gest-solEliminacion")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String solicitudes(Model model) {
    List<SolicitudEliminacionDTO> solicitudes = this.agregador.findAllSolicitudes();
    model.addAttribute("solicitudes", solicitudes);
    model.addAttribute("titulo", "Solicitudes de Eliminacion");
    return "admins/gest-solEliminacion";
  }

  @GetMapping("/dashboard-estadisticas")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public String estadisticas() {

    return "admins/dashboard-estadisticas";
  }
}
