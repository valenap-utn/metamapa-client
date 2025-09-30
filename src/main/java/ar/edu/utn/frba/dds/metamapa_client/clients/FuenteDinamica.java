package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOInput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.HechoDTOOutput;
import ar.edu.utn.frba.dds.metamapa_client.dtos.RevisionDTO;
import ar.edu.utn.frba.dds.metamapa_client.dtos.SolicitudEdicionDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FuenteDinamica implements IFuenteDinamica {

  private WebClient initWebClient(String baseUrl) {
    return WebClient.builder().baseUrl(baseUrl)
            .exchangeStrategies(ExchangeStrategies
                    .builder()
                    .codecs(codecs -> codecs
                            .defaultCodecs()
                            .maxInMemorySize(50 * 1024 * 1024))
                    .build()).build();
  }

  public HechoDTOOutput crearHecho(HechoDTOInput hecho, String baseUrl) {
    return initWebClient(baseUrl).post().uri(uriBuilder -> uriBuilder.path("/api/hechos").build())
            .bodyValue(hecho).retrieve().bodyToMono(HechoDTOOutput.class).block();
  }

  public HechoDTOOutput actualizarHecho(HechoDTOInput hecho, String baseUrl) {
    return initWebClient(baseUrl).put().uri(uriBuilder -> uriBuilder.path("/api/hechos/{id}").build(hecho.getId()))
            .bodyValue(hecho).retrieve().bodyToMono(HechoDTOOutput.class).block();
  }

  public HechoDTOOutput revisarHecho(Long idHecho, String baseUrl) {
    return initWebClient(baseUrl).put().uri(uriBuilder -> uriBuilder.path("/api/hechos/{id}/revisados").build(idHecho))
            .retrieve().bodyToMono(HechoDTOOutput.class).block();
  }

  public SolicitudEdicionDTO solicitarModificacion(SolicitudEdicionDTO solicitudEdicion, String baseUrl) {
    return initWebClient(baseUrl).post().uri(uriBuilder -> uriBuilder.path("/api/solicitudes").build())
            .bodyValue(solicitudEdicion)
            .retrieve()
            .bodyToMono(SolicitudEdicionDTO.class)
            .block();
  }

  public SolicitudEdicionDTO  procesarSolicitudEdicion(Long idSolicitud, String baseUrl, RevisionDTO revisionDTO) {
    return this.initWebClient(baseUrl).put().uri(uriBuilder -> uriBuilder.path("/api/solicitudes/{id}").build(idSolicitud)).bodyValue(revisionDTO)
            .retrieve().bodyToMono(SolicitudEdicionDTO.class)
            .block();
  }
}
