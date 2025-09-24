package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hechos")
public class HechosController {
  @GetMapping("/hecho-completo")
  public String hechoCompleto(Model model) {
    return "hechos/hecho-completo";
  }

  @GetMapping("/nav-hechos")
  public String navHechos(Model model) {
    return "hechos/nav-hechos";
  }

  @GetMapping("/subir-hecho")
  public String subirHecho() {
    return "hechos/subir-hecho";
  }

  @PostMapping("/subir-hecho")
  public String subirHechoPost(HechoDTOInput hechoDtoInput, Model model) {
    return "hechos/subir-hecho";
  }
}
