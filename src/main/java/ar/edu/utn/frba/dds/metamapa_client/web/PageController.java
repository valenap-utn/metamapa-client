package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.dtos.ColeccionDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
public class PageController {
  private final ClientSeader cliente;

  public PageController(ClientSeader cliente) {
    this.cliente = cliente;
  }

  @GetMapping({"/","/index"})
  public String home() {
    return "index";
  }

  @GetMapping("/iniciar-sesion")
  public String iniciarSesion() {
    return "iniciar-sesion";
  }


  @GetMapping("/main-gral")
  public String mainGral(HttpSession session, Model model) {
    model.addAttribute("titulo", "MetaMapa");
    model.addAttribute("isContribuyente", Boolean.TRUE.equals(session.getAttribute("isContribuyente")));
    model.addAttribute("isAdmin",         Boolean.TRUE.equals(session.getAttribute("isAdmin")));
    return "main-gral";
  }

  @GetMapping("/colecciones")
  public String colecciones(Model model) {
    List<ColeccionDTOOutput> colecciones = this.cliente.findColecciones();
    model.addAttribute("colecciones", colecciones);
    return "colecciones";
  }

  @GetMapping("/colecciones/{id}/nav-hechos")
  public String coleccionesNavHechos(Model model, @PathVariable UUID id) {
    FiltroDTO filtroDTO = new FiltroDTO();
    List<HechoDTOOutput> hechos =  this.cliente.findHechosByColeccionId(id, filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("urlColeccion", "/colecciones/" + id + "/nav-hechos");
    return "hechos/nav-hechos";
  }

  @PostMapping("/colecciones/{id}/nav-hechos")
  public String coleccionesNavHechos(Model model, @PathVariable UUID id, @ModelAttribute("filtroDTO") FiltroDTO filtroDTO) {
    List<HechoDTOOutput> hechos =  this.cliente.findHechosByColeccionId(id, filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("urlColeccion", "/colecciones/" + id + "/nav-hechos");
    return "hechos/nav-hechos";
  }

  @GetMapping("/crear-cuenta")
  public String crearCuenta(Model model) {
    model.addAttribute("cuenta", new UsuarioDTO());
    return "crear-cuenta";
  }

  @PostMapping("/auth/register")
  public String register(Model model, @ModelAttribute("cuenta") UsuarioDTO usuario) {
    UsuarioDTO usuario2 = this.cliente.crearUsuario(usuario);

    return "redirect:/iniciar-sesion";
  }

  @GetMapping("/privacidad")
  public String privacidad() {
    return "privacidad";
  }

  @GetMapping("/terminos")
  public String terminos() {
    return "terminos";
  }

  @GetMapping("/main")
  public String mainDeUsuario() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = attributes.getRequest();
    String rol = request.getSession().getAttribute("rol").toString();
    return rol.startsWith("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }

}
