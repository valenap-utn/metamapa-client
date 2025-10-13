package ar.edu.utn.frba.dds.metamapa_client.clients;

import ar.edu.utn.frba.dds.metamapa_client.dtos.EstadisticaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ServicioDeEstadistica {

    private WebClient initWebClient(String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies
                        .builder()
                        .codecs(codecs -> codecs
                                .defaultCodecs()
                                .maxInMemorySize(50 * 1024 * 1024))
                        .build()).build();
    }

    public EstadisticaDTO obtenerEstadistica(EstadisticaDTO estadistica, String baseUrl) {
        return initWebClient(baseUrl).post().uri(uriBuilder -> uriBuilder.path("/api/estadisticas/{clave}").build(estadistica.getClave()))
                .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(EstadisticaDTO.class).block();
    }

    public byte[] exportarCSV(EstadisticaDTO estadistica, String baseUrl) {
        return initWebClient(baseUrl).post().uri(uriBuilder -> uriBuilder.path("/api/estadisticas/{clave}/exportar").build(estadistica.getClave()))
                .accept(MediaType.valueOf("text/csv")).retrieve().bodyToMono(byte[].class).block();
    }
}

