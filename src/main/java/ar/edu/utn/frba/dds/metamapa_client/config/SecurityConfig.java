package ar.edu.utn.frba.dds.metamapa_client.config;

import ar.edu.utn.frba.dds.metamapa_client.provider.AuthProviderCreado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {
  @Bean
  public AuthenticationManager authManager(HttpSecurity http, AuthProviderCreado provider) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(provider)
            .build();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/index", "/iniciar-sesion", "/crear-cuenta",
                "/main-gral", "/colecciones", "/terminos", "/privacidad",
                "/hechos/**",
                "/css/**", "/js/**", "/components/**", "/images/**",
                "/favicon.ico", "/error/**",
                "/auth/**", "/oauth2/**", "/login/**"
            ).permitAll()
            .anyRequest().permitAll()
        ).oauth2Login(oauth -> oauth
            .loginPage("/iniciar-sesion")
            .defaultSuccessUrl("/oauth2/success", true) //se procesa el email/rol
        ).logout(Customizer.withDefaults())
            .formLogin( form -> form
                    .loginPage("/iniciar-sesion")
                    .defaultSuccessUrl("/main", true)
            )
            .csrf((AbstractHttpConfigurer::disable))
            .exceptionHandling( httpSecurityExceptionHandlingConfigurer ->
                    httpSecurityExceptionHandlingConfigurer
                            .authenticationEntryPoint((request, response, authException) -> {response.sendRedirect("/iniciar-sesion?unauthorized");})
                            .accessDeniedHandler((request, response, accessDeniedException) -> {response.sendRedirect("/403");})
            );

    return http.build();
  }

  //Para evitar que se autentique 2 veces y tenga dos sesiones distintas
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }
}
