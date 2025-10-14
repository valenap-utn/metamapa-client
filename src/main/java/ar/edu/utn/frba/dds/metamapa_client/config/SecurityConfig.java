package ar.edu.utn.frba.dds.metamapa_client.config;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import ar.edu.utn.frba.dds.metamapa_client.provider.AuthProviderCreado;
import ar.edu.utn.frba.dds.metamapa_client.security.CustomOAuth2UserService;
import ar.edu.utn.frba.dds.metamapa_client.security.GoogleSelectAccountResolver;
import ar.edu.utn.frba.dds.metamapa_client.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
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
  public OAuth2AuthorizationRequestResolver googleSelectAccountResolver(ClientRegistrationRepository clientRegistrationRepository) {
    return new GoogleSelectAccountResolver(clientRegistrationRepository);
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler, OAuth2AuthorizationRequestResolver googleSelectAccountResolver) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hechos/mis-hechos").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
//                .requestMatchers("/main-gral").hasAnyRole("ADMINISTRADOR","CONTRIBUYENTE") //provisorio, de pruebita
                .requestMatchers(
                "/", "/index", "/iniciar-sesion", "/crear-cuenta",
                "/main-gral", "/colecciones", "/terminos", "/privacidad",
                "/hechos/**",
                "/css/**", "/js/**", "/components/**", "/images/**",
                "/favicon.ico", "/error/**",
                "/auth/**", "/oauth2/**", "/login/**"
            ).permitAll()
                //.requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated()
        ).oauth2Login(oauth -> oauth
            .loginPage("/iniciar-sesion")
            .authorizationEndpoint(ae -> ae
                .authorizationRequestResolver(googleSelectAccountResolver))
            .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
            .successHandler(oAuth2SuccessHandler)
            .failureHandler((request, response, exception) -> {
              response.sendRedirect("/iniciar-sesion?error="+
                  encode(exception.getMessage() == null ? "oauth_failed" : exception.getMessage(), UTF_8));
            })
//            .failureUrl("/iniciar-sesion?error=oauth_failed")
//            .defaultSuccessUrl("/oauth2/success", true) //se procesa el email/rol
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
