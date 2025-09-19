package ar.edu.utn.frba.dds.metamapa_client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMcvConfig implements WebMvcConfigurer {
  @Autowired AdminGuard guard;

  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(guard).addPathPatterns("/admin/**","/admin");
  }
}
