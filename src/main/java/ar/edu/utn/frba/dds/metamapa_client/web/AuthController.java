package ar.edu.utn.frba.dds.metamapa_client.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth")
public class AuthController {

  // Dev: ver qué quedó en sesión
  @GetMapping("/whoami")
  @ResponseBody
  public String whoami(Authentication auth) {
    return (auth == null) ? "anon" : "principal=" + auth.getName() + " auths=" + auth.getAuthorities();
  }

  @PostMapping("/logout")
  public String logout(HttpSession session, HttpServletResponse response){
    session.invalidate();
//    rememberService.clearCookie(response);
    return "redirect:/";
  }

  private static String normalizeRole(String r) {
    if (r == null) return "VISUALIZADOR";
    String x = r.trim().toUpperCase();
    if (x.startsWith("ADMIN")) return "ADMIN";
    if (x.startsWith("CONTRI")) return "CONTRIBUYENTE";
    return x; // por si ya viene correcto
  }
}
