package ar.edu.utn.frba.dds.metamapa_client.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RememberInterceptor implements HandlerInterceptor {

  private final RememberService remember;
  public RememberInterceptor(RememberService remember) {
    this.remember = remember;
  }

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler){
    HttpSession s = req.getSession(false);
//    if (s != null && s.getAttribute("AUTH_ROLE") != null) return true;
    if (s != null && (s.getAttribute("isAdmin") != null || s.getAttribute("isContribuyente") != null)) {
      return true;
    }

    var p = remember.parseCookie(req);
    if (p.isPresent()) {
      s = req.getSession(true);
      String role = String.valueOf(p.get().role());
      s.setAttribute("AUTH_EMAIL", p.get().email());
      s.setAttribute("AUTH_ROLE", role);

      String up = role == null ? "" : role.toUpperCase();
      boolean isAdmin  = up.contains("ADMINISTRADOR");
      boolean isContrib= up.contains("CONTRIBUYENTE");

      s.setAttribute("isAdmin", isAdmin);
      s.setAttribute("isContribuyente", isContrib);
    }
    return true;
  }

}
