/*
package ar.edu.utn.frba.dds.metamapa_client.config;

import ar.edu.utn.frba.dds.metamapa_client.security.RememberInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMcvConfig implements WebMvcConfigurer {
//  @Autowired AdminGuard guard;

  private final RememberInterceptor remember;
  private final AdminGuard adminGuard;

  public WebMcvConfig(RememberInterceptor remember, AdminGuard adminGuard) {
    this.remember = remember;
    this.adminGuard = adminGuard;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(remember).order(0); //primero se reconstruye la sesión
    registry.addInterceptor(adminGuard).addPathPatterns("/admin/**","/admin").order(1);
  }
}
*/
