package ar.edu.utn.frba.dds.metamapa_client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfig {
  @Bean
  WebClient backend(@Value("${backend.url}") String baseUrl){
    return WebClient.builder().baseUrl(baseUrl).build();
  }
}
