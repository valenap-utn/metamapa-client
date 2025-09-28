package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.StatsResp;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.SolicitudEliminacionDTO;
import java.util.List;
import lombok.Builder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Controller
@RequestMapping("/admin")
public class AdminController {
  private final ClientSeader agregador;
  private final BackendAPI api;

  @GetMapping
  public String dashboard(Model model) {
//    // TODO: traer métricas reales del backend
//    model.addAttribute("metrics", Map.of("hechos",124, "fuentes",8, "solicitudes",3));
//    return "admin";
//  }
    StatsResp stats = api.getAdminStats();
    model.addAttribute("metrics", stats);
    return "admin";
  }

  @GetMapping("/crear-coleccion")
  public String crearColeccion(Model model) {
    model.addAttribute("coleccion", new ColeccionDTOInput());
    model.addAttribute("titulo", "Crear Coleccion");
    return "admins/crear-coleccion";
  }

  @PostMapping("/crear-coleccion")
  public String crearColeccionPost(@ModelAttribute("coleccion") ColeccionDTOInput coleccion, Model model) {
    ColeccionDTOOutput coleccionDTOOutput = this.agregador.crearColeccion(coleccion);

    return "admins/crear-coleccion";
  }

  @GetMapping("/modificar-coleccion")
  public String modificarColeccion() {
    return "admins/modificar-coleccion";
  }

  @GetMapping("/importar-csv")
  public String importarCsv(MultipartFile file, Model model) {

    this.agregador.subirHechosCSV(file, 1L, "http://localhost:5000/");
    return "admins/importar-csv";
  }

  @GetMapping("/gest-solEliminacion")
  public String solicitudes(Model model) {
    List<SolicitudEliminacionDTO> solicitudes = this.agregador.findAllSolicitudes();
    model.addAttribute("solicitudes", solicitudes);
    model.addAttribute("titulo", "Solicitudes de Eliminacion");
    return "admins/gest-solEliminacion";
  }

  @GetMapping("/dashboard-estadisticas")
  public String estadisticas() {
    return "admins/dashboard-estadisticas";
  }
}
