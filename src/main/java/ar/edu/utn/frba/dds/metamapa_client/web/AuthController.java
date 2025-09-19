package ar.edu.utn.frba.dds.metamapa_client.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {
  private final WebClient backend;
  public AuthController(WebClient backend){
    this.backend = backend;
  }

  @PostMapping("/login")
  public String iniciarSesion(@RequestParam String email, @RequestParam String password, HttpSession session){
    Map<?,?> resp = backend.post().uri("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("email",email,"password",password))
        .retrieve().bodyToMono(Map.class).onErrorReturn(null).block();
    if(resp == null || !Boolean.TRUE.equals(resp.get("ok"))){
      return "redirect:/iniciar-sesion?error=1";
    }

    String role = String.valueOf(resp.get("role")).toUpperCase();
    session.setAttribute("AUTH_ROLE",role);
    session.setAttribute("AUTH_EMAIL",email);
    return role.startsWith("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }

  @PostMapping("/logout")
  public String logout(HttpSession session){
    session.invalidate();
    return "redirect:/";
  }

  @PostMapping("/mock")
  @ResponseBody
  public ResponseEntity<Void> mock(@RequestParam String role, HttpSession session) {
    String norm = role == null ? "" : role.trim().toUpperCase();
    String serverRole = norm.startsWith("ADMIN") ? "ADMIN" : (norm.startsWith("CONTRI") ? "CONTRIBUYENTE" : "VISUALIZADOR");
    session.setAttribute("AUTH_ROLE", serverRole);
    session.setAttribute("AUTH_EMAIL", "mock@metamapa");
    return ResponseEntity.noContent().build(); // 204
  }
}
