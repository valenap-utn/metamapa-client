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
    if (s != null && s.getAttribute("AUTH_ROLE") != null) return true;
    var p = remember.parseCookie(req);
    if (p.isPresent()) {
      s = req.getSession(true);
      s.setAttribute("AUTH_EMAIL", p.get().email());
      s.setAttribute("AUTH_ROLE", p.get().role());
    }
    return true;
  }

}
