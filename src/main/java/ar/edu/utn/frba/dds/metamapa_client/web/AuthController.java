package ar.edu.utn.frba.dds.metamapa_client.web;

import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginResp;
import ar.edu.utn.frba.dds.metamapa_client.dtos.AuthResponseDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RolesPermisosDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.usuarios.Rol;
import ar.edu.utn.frba.dds.metamapa_client.security.RememberService;
import ar.edu.utn.frba.dds.metamapa_client.services.ConexionServicioUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Controller
//@Profile("real")
@RequestMapping("/auth")
public class AuthController {
//  private final WebClient backend;
  private final BackendAPI api;
  private final RememberService rememberService;
  //private final ConexionServicioUser servicioUsuarios;
  private final Environment env;

  public AuthController(/*WebClient backend, */ BackendAPI api , RememberService rememberService, Environment enviroment) {
//    this.backend = backend;
    this.api = api;
    this.rememberService =  rememberService;

    this.env = enviroment;
  }

  @Value("${metamapa.dev.auth.mock:true}")  // true en dev, false en prod
  boolean authMock;

  @PostMapping("/login")
  public String login(@RequestParam String email, @RequestParam String password, HttpSession session, RedirectAttributes ra) {
    /*try {
      if (authMock) {
        // ✅ Fallback local: acepto cualquier user/pass y seteo sesión
        session.setAttribute("accessToken", "dev");
        session.setAttribute("refreshToken", "dev");
        // elegí un ID que exista en tu ClientSeader (2L es contribuyente en tu mock)
        session.setAttribute("user_id", 2L);
        session.setAttribute("userId",  2L);
        session.setAttribute("isContribuyente", true);
        session.setAttribute("isAdmin", false);
        return "redirect:/main-gral";
      }

      // 🔐 Camino real con backend
      AuthResponseDTO tokens = servicioUsuarios.getTokens(email, password);
      session.setAttribute("accessToken", tokens.getAccessToken());
      session.setAttribute("refreshToken", tokens.getRefreshToken());

      Long userId = null;
      try { var me = servicioUsuarios.getMe(); if (me!=null) userId = me.getId(); } catch (Exception ignored) {}
      if (userId == null) userId = 2L; // fallback dev opcional
      session.setAttribute("user_id", userId);
      session.setAttribute("userId",  userId);

      boolean isAdmin=false, isContrib=false;
      try {
        RolesPermisosDTO rp = servicioUsuarios.getRolesPermisos(tokens.getAccessToken());
        if (rp != null && rp.getRol()!=null) {
          String up = rp.getRol().toUpperCase();
          if (up.contains("ADMIN"))         isAdmin = true;
          if (up.contains("CONTRIBUYENTE")) isContrib = true;
        }
      } catch (Exception ignored) {}
      if (!isAdmin && !isContrib) isContrib = true;

      session.setAttribute("isAdmin", isAdmin);
      session.setAttribute("isContribuyente", isContrib);

      return "redirect:/main-gral";
    } catch (Exception e) {
      ra.addFlashAttribute("error", "No se pudo iniciar sesión.");
      return "redirect:/iniciar-sesion";
    }*/
    return null;
  }

  // Dev: ver qué quedó en sesión
  @GetMapping("/whoami")
  @ResponseBody
  public String whoami(Authentication auth) {
    return (auth == null) ? "anon" : "principal=" + auth.getName() + " auths=" + auth.getAuthorities();
  }

  @PostMapping("/logout")
  public String logout(HttpSession session, HttpServletResponse response){
    session.invalidate();
    rememberService.clearCookie(response);
    return "redirect:/";
  }

  @PostMapping("/regi")
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
