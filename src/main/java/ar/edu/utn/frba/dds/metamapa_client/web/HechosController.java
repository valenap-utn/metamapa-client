package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.model.dtos.HechoDTOOutput;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hechos")
public class HechosController {
  private final ClientSeader agregador;

  public HechosController(ClientSeader agregador) {
    this.agregador = agregador;
  }

  @GetMapping("/{idHecho}")
  public String hechoCompleto(@PathVariable Long idHecho, Model model) {
    HechoDTOOutput hecho = this.agregador.getHecho(idHecho);
    model.addAttribute("titulo", "Detalle de Hecho");
    model.addAttribute("hecho", hecho);
    return "hechos/hecho-completo";
  }

  @GetMapping("/nav-hechos")
  public String navHechos(Model model, @ModelAttribute("filtros") FiltroDTO filtroDTO) {
    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("titulo", "Listado de todos los hechos");
    return "hechos/nav-hechos";
  }

  @GetMapping("/subir-hecho")
  public String subirHecho(Model model) {
    model.addAttribute("hecho", new HechoDTOInput());
    return "hechos/subir-hecho";
  }

  @PostMapping("/subir-hecho")
  public String subirHechoPost(@ModelAttribute("hecho") HechoDTOInput hechoDtoInput, Model model, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    HechoDTOOutput hechoDTO = this.agregador.crearHecho(hechoDtoInput, "http://localhost:4000");

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
