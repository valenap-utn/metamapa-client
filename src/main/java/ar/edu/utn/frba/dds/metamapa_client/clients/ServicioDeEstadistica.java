package ar.edu.utn.frba.dds.metamapa_client.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ServicioDeEstadistica {
  @Value("${api.servicioEstadistica.url}")
  private String baseURL;
  private WebClient webClient;

  public ServicioDeEstadistica() {
    webClient = WebClient.builder().baseUrl(baseURL).build();
  }

  public String getEstadisticas() {
    return null;
  }

  public String getFileEstadistica() {
    return null;
  }

}
