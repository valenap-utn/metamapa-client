package ar.edu.utn.frba.dds.metamapa_client.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
  @GetMapping
  public String dashboard(Model model) {
    // TODO: traer métricas reales del backend
    model.addAttribute("metrics", Map.of("hechos",124, "fuentes",8, "solicitudes",3));
    return "admin";  // templates/admin.html
  }

  @GetMapping("/crear-coleccion")
  public String crearColeccion() {
    return "admins/crear-coleccion";
  }

  @GetMapping("/modificar-coleccion")
  public String modificarColeccion() {
    return "admins/modificar-coleccion";
  }

  @GetMapping("/importar-csv")
  public String importarCsv() {
    return "admins/importar-csv";
  }

  @GetMapping("/gest-solEliminacion")
  public String solicitudes() {
    return "admins/gest-solEliminacion";
  }

  @GetMapping("/dashboard-estadisticas")
  public String estadisticas() {
    return "admins/dashboard-estadisticas";
  }
}
