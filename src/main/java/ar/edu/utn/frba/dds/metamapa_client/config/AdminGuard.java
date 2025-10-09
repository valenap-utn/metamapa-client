package ar.edu.utn.frba.dds.metamapa_client.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminGuard implements HandlerInterceptor {

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String role = (String) request.getSession().getAttribute("rol");
    if(role != null && role.startsWith("ADMIN")){
      return true;
    }
    response.sendRedirect("/iniciar-sesion?forbidden=1"); return false;
  }

}
