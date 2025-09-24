package ar.edu.utn.frba.dds.metamapa_client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/index", "/iniciar-sesion", "/crear-cuenta",
                "/main-gral", "/colecciones", "/terminos", "/privacidad",
                "/hechos/**", "/admin/**",
                "/css/**", "/js/**", "/components/**", "/images/**",
                "/favicon.ico", "/error/**",
                "/auth/**", "/oauth2/**", "/login/**"
            ).permitAll()
            .anyRequest().permitAll()
        ).oauth2Login(oauth -> oauth
            .loginPage("/iniciar-sesion")
            .defaultSuccessUrl("/oauth2/success", true) //se procesa el email/rol
        ).logout(Customizer.withDefaults())
        .csrf((csrf->csrf.disable()));
    return http.build();
  }
}
