package ar.edu.utn.frba.dds.metamapa_client.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FuenteEstatica implements IFuenteEstatica {

  private WebClient initWebClient(String baseUrl) {
    return WebClient.builder().baseUrl(baseUrl)
            .exchangeStrategies(ExchangeStrategies
                    .builder()
                    .codecs(codecs -> codecs
                            .defaultCodecs()
                            .maxInMemorySize(50 * 1024 * 1024))
                    .build()).build();
  }

  public String subirHechosCSV(MultipartFile archivo, Long idUsuario, String baseURL) {
    return initWebClient(baseURL).post().uri(uriBuilder -> uriBuilder.queryParam("idUsuario", idUsuario).queryParam("archivo", archivo).build())
            .retrieve()
            .bodyToMono(String.class)
            .block();
  }

}
