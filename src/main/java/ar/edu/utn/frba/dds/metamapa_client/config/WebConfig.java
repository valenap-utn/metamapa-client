package ar.edu.utn.frba.dds.metamapa_client.config;

import ar.edu.utn.frba.dds.metamapa_client.security.RememberInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final RememberInterceptor rememberInterceptor;

  public WebConfig(RememberInterceptor rememberInterceptor) {
    this.rememberInterceptor = rememberInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rememberInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/css/**","/js/**","/images/**","/webjars/**");
  }
}
