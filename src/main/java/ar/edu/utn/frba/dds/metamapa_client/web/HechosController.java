package ar.edu.utn.frba.dds.metamapa_client.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hechos")
public class HechosController {
  @GetMapping("/hecho-completo")
  public String hechoCompleto() {
    return "hechos/hecho-completo";
  }

  @GetMapping("/nav-hechos")
  public String navHechos() {
    return "hechos/nav-hechos";
  }

  @GetMapping("/subir-hecho")
  public String subirHecho() {
    return "hechos/subir-hecho";
  }
}
