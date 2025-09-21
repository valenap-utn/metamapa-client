package ar.edu.utn.frba.dds.metamapa_client.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfig {
  @Bean
  WebClient backend(@Value("${metamapa.backend.base-url:http://localhost:9000}") String baseUrl){
    return WebClient.builder().baseUrl(baseUrl).build();
  }
//  @Bean
//  WebClient backend(@Value("${mm.backend}") String baseUrl) {
//    return WebClient.builder()
//        .baseUrl(baseUrl) // ej: http://localhost:9001
//        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//        .build();
//  }
}
