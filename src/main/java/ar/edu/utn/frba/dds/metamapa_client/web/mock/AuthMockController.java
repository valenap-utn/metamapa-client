package ar.edu.utn.frba.dds.metamapa_client.web.mock;

import ar.edu.utn.frba.dds.metamapa_client.security.RememberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("mock")
@RequestMapping("/auth")
public class AuthMockController {

  private final RememberService rememberService;
  public AuthMockController(RememberService rememberService) {
    this.rememberService = rememberService;
  }

  @PostMapping("/login")
  public String iniciarSesion(@RequestParam String email, @RequestParam String password, @RequestParam(required=false) String remember, HttpSession session, HttpServletResponse response){

    // regla simple: si empieza con "admin" => ADMIN; sino CONTRIBUYENTE
    String role = email.toLowerCase().startsWith("admin") ? "ADMIN" : "CONTRIBUYENTE";
    session.setAttribute("AUTH_EMAIL", email);
    session.setAttribute("AUTH_ROLE", role);

    if ("1".equals(remember) || "on".equalsIgnoreCase(remember)) {
      rememberService.setRememberCookie(response, email, role);
    }

    return role.equals("ADMIN") ? "redirect:/admin" : "redirect:/main-gral";
  }

  @PostMapping("/logout")
  public String logout(HttpSession s, HttpServletResponse res) {
    s.invalidate();
    rememberService.clearCookie(res);
    return "redirect:/";
  }

}
