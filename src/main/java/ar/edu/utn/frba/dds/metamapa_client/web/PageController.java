package ar.edu.utn.frba.dds.metamapa_client.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  @GetMapping({"/","/index"})
  public String home() {
    return "index";
  }

  @GetMapping("/iniciar-sesion")
  public String iniciarSesion() {
    return "iniciar-sesion";
  }

  @GetMapping("/main-gral")
  public String mainGral() {
    return "main-gral";
  }

  @GetMapping("/colecciones")
  public String colecciones() {
    return "colecciones";
  }

  @GetMapping("/crear-cuenta")
  public String crearCuenta() {
    return "crear-cuenta";
  }

  @GetMapping("/privacidad")
  public String privacidad() {
    return "privacidad";
  }

  @GetMapping("/terminos")
  public String terminos() {
    return "terminos";
  }
//  @GetMapping("/admin")
//  public String admin() {
//    return "admin";       // protegido por interceptor (abajo)
//  }
}
