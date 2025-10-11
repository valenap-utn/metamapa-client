package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.clients.ClientSeader;
import ar.edu.utn.frba.dds.metamapa_client.clients.utils.JwtUtil;
import ar.edu.utn.frba.dds.metamapa_client.dtos.FiltroDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.usuarios.Rol;
import ar.edu.utn.frba.dds.metamapa_client.services.ConexionServicioUser;
import ar.edu.utn.frba.dds.metamapa_client.services.IConexionServicioUser;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
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

  private final IConexionServicioUser servicioUsuarios;

  public HechosController(ClientSeader agregador, IConexionServicioUser servicioUsuarios) {
    this.agregador = agregador;
    this.servicioUsuarios = servicioUsuarios;
  }

  @GetMapping("/{idHecho}")
  public String hechoCompleto(@PathVariable Long idHecho, Model model, @RequestParam(value = "urlColeccion", required = false) String urlColeccion) {
    HechoDTOOutput hecho = this.agregador.getHecho(idHecho);
    model.addAttribute("titulo", "Detalle de Hecho");
    model.addAttribute("hecho", hecho);
    model.addAttribute("urlColeccion", urlColeccion);
    return "hechos/hecho-completo";
  }

  @GetMapping("/nav-hechos")
  public String navHechos(Model model) {
    FiltroDTO filtroDTO = new FiltroDTO();
    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("titulo", "Listado de todos los hechos");
    return "hechos/nav-hechos";
  }

  @PostMapping("/nav-hechos")
  public String navHechosPost(Model model, @ModelAttribute("filtros") FiltroDTO filtroDTO) {
    List<HechoDTOOutput> hechos = this.agregador.findAllHechos(filtroDTO);
    model.addAttribute("hechos", hechos);
    model.addAttribute("filtros", filtroDTO);
    model.addAttribute("titulo", "Listado de todos los hechos");
    return "hechos/nav-hechos";
  }

  @GetMapping("/subir-hecho")
  public String subirHecho(Model model) {
    model.addAttribute("hecho", new HechoDTOInput());
    return "hechos/subir-hecho";
  }

  @PostMapping("/subir-hecho")
//  public String subirHechoPost(@ModelAttribute("hecho") HechoDTOInput hechoDtoInput, Model model, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
//    HechoDTOOutput hechoDTO = this.agregador.crearHecho(hechoDtoInput, "http://localhost:4000");
//
//    return "hechos/subir-hecho";
//  }
  public String subirHechoPost(@Valid @ModelAttribute("hecho") HechoDTOInput hechoDtoInput, Model model, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session) {
    if(bindingResult.hasErrors()){
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.hecho", bindingResult);
      redirectAttributes.addFlashAttribute("hecho", hechoDtoInput);
      redirectAttributes.addFlashAttribute("titulo", "Revisá los campos marcados");
      return "redirect:/hechos/subir-hecho";
    }

    //Obtenemos user
    String accessToken = session.getAttribute("accessToken").toString();
    Long userId = JwtUtil.getId(accessToken);

    try{
      hechoDtoInput.setIdUsuario(userId);
      if(hechoDtoInput.getFechaCarga() == null){
        hechoDtoInput.setFechaCarga(LocalDateTime.now());
      }
      this.agregador.crearHecho(hechoDtoInput, "http://localhost:4000");
      redirectAttributes.addFlashAttribute("success", "Tu hecho se creó exitosamente, pronto un administrador lo estará revisado !");
      return "redirect:/main-gral";
    }catch(Exception e){
      log.error("Error al crear el hecho", e);
      redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al crear el hecho. Volvé a intentarlo");
      redirectAttributes.addFlashAttribute("hecho", hechoDtoInput);
      return "redirect:/hechos/subir-hecho";
    }
  }

  //Método para chequear que tenga rol
  private boolean hasRole(HttpSession session, Rol r) {
    boolean isAdmin  = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    boolean isContrib= Boolean.TRUE.equals(session.getAttribute("isContribuyente"));
    return (r == Rol.ADMINISTRADOR && isAdmin) || (r == Rol.CONTRIBUYENTE && isContrib);
  }

  private static final Logger log = LoggerFactory.getLogger(HechosController.class);

  // Para ver Hechos subidos por uno mismo
  @GetMapping("/mis-hechos")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public String misHechos(@RequestParam(defaultValue = "12") int limit, @RequestParam(defaultValue = "12") int step, @RequestParam(required = false) boolean partial, HttpSession session, Model model, RedirectAttributes ra) {
    String accessToken = session.getAttribute("accessToken").toString();

    Long userId = JwtUtil.getId(accessToken);
    // Listamos y acumulamos los hechos
    List<HechoDTOOutput> all = agregador.listHechosDelUsuario(userId);

    log.info("[mis-hechos] userId={}, isContrib={}, totalItems={}", userId, hasRole(session, Rol.CONTRIBUYENTE), (all==null?0:all.size()));
    if (all != null && !all.isEmpty()) {
      log.info("[mis-hechos] firstItemId={}, title={}", all.get(0).getId(), all.get(0).getTitulo());
    }

    int total = all.size();
    int shown = Math.min(Math.max(limit,0), total);

    model.addAttribute("items", all.subList(0, shown));
    model.addAttribute("shown", shown);
    model.addAttribute("total", total);
    model.addAttribute("hasMore", shown < total);
    model.addAttribute("nextLimit", Math.min(shown + step, total));
    model.addAttribute("step", step);
    model.addAttribute("titulo", "Mis Hechos");
    return "hechos/mis-hechos";
  }

  // Para editar el Hecho
  // Validamos existencia del Usuario actual,
  // y ventana de 7 días desde fecha de Carga del Hecho
  // Si falla => redirige con flash error (ver si lo queremos cambiar a esto)
  // Sino => renderiza editar.html (reutilizamos subir-hecho con click-to-edit)
  @GetMapping("/{idHecho}/editar")
  @PreAuthorize("hasRole('CONTRIBUYENTE')")
  public String editar(@PathVariable Long idHecho, HttpSession session, RedirectAttributes ra, Model model) {
    String accessToken = session.getAttribute("accessToken").toString();

    Long userId = JwtUtil.getId(accessToken);

    if(userId == null) {
        return "redirect:/iniciar-sesion";
    }

    HechoDTOOutput hecho = agregador.revisarHecho(idHecho, "http://localhost:3000");
    if (hecho == null) {
      ra.addFlashAttribute("error", "El hecho no existe.");
      return "redirect:/hechos/mis-hechos";
    }
    if (!userId.equals(hecho.getIdUsuario())) {
      ra.addFlashAttribute("error", "No podés editar un hecho que no es tuyo.");
      return "redirect:/hechos/mis-hechos";
    }

    boolean editable = hecho.getFechaCarga() != null && LocalDateTime.now().isBefore(hecho.getFechaCarga().plusDays(7));
    if(!editable){
      ra.addFlashAttribute("error", "La edición está disponible solo durante los primeros 7 días");
      return "redirect:/hechos/mis-hechos";
    }

    model.addAttribute("hecho", hecho);
    model.addAttribute("titulo", "Editar Hecho");
    return "hechos/editar";
  }

  // ---------- Helper para el ID ----------




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
