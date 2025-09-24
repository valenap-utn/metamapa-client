package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginResp;
import ar.edu.utn.frba.dds.metamapa_client.security.RememberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
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
//@Profile("real")
@RequestMapping("/auth")
public class AuthController {
//  private final WebClient backend;
  private final BackendAPI api;
  private final RememberService rememberService;

  public AuthController(/*WebClient backend, */ BackendAPI api , RememberService rememberService){
//    this.backend = backend;
    this.api = api;
    this.rememberService =  rememberService;
  }

  @PostMapping("/login")
  public String login(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam(name = "remember", required = false) String remember, HttpSession session, HttpServletResponse response) {
    LoginResp loginResp = api.login(email, password);
    if(loginResp == null || !loginResp.ok()){
      String code = (loginResp != null && loginResp.error() != null) ? loginResp.error() : "invalid_credentials";
      return "redirect:/iniciar-sesion?error=" + code;
    }

    String role = normalizeRole(loginResp.rol());
    session.setAttribute("AUTH_EMAIL", email);
    session.setAttribute("AUTH_ROLE", role);

    if("on".equals(remember) || "1".equals(remember)){
      rememberService.setRememberCookie(response,email,role);
    }

    return role.startsWith("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }


  @PostMapping("/logout")
  public String logout(HttpSession session, HttpServletResponse response){
    session.invalidate();
    rememberService.clearCookie(response);
    return "redirect:/";
  }

  @PostMapping("/register")
  public String register(@RequestParam String email, @RequestParam String password, @RequestParam String rol, @RequestParam(name="remember", required=false) String remember, HttpSession session, HttpServletResponse response) {
    var r = api.register(email, password, rol);
    if (r == null || !r.ok()) {
      String code = (r != null && r.error() != null) ? r.error() : "unknown";
      return "redirect:/crear-cuenta?error=" + code; // o mostrar mensaje “email ya registrado”
    }

    String role = normalizeRole(r.rol());
    session.setAttribute("AUTH_EMAIL", email);
    session.setAttribute("AUTH_ROLE",  role);

    if ("on".equalsIgnoreCase(remember) || "1".equals(remember)) {
      rememberService.setRememberCookie(response, email, role);
    }
    return role.startsWith("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }

  private static String normalizeRole(String r) {
    if (r == null) return "VISUALIZADOR";
    String x = r.trim().toUpperCase();
    if (x.startsWith("ADMIN")) return "ADMIN";
    if (x.startsWith("CONTRI")) return "CONTRIBUYENTE";
    return x; // por si ya viene correcto
  }
}
