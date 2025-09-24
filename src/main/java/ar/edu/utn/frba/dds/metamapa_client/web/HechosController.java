package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

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
