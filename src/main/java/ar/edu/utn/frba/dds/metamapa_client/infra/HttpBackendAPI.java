package ar.edu.utn.frba.dds.metamapa_client.infra;

import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginReq;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginResp;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.RegisterReq;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.StatsResp;
import lombok.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Builder
@Service
@ConditionalOnProperty(name = "metamapa.mode", havingValue = "api")
public class HttpBackendAPI implements BackendAPI {

  private final WebClient http;

  @Override
  public LoginResp login(String email, String password) {
    return http.post().uri("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new LoginReq(email, password))
        .retrieve()
        .onStatus(s -> s.value() == 401, rsp -> Mono.just(new WebClientResponseException(401, "invalid_credentials",null,null,null)))
        .bodyToMono(LoginResp.class)
        .onErrorReturn(new LoginResp(false,"","invalid_credentials"))
        .block();
  }

  @Override
  public LoginResp register(String email, String password, String rol) {
    return http.post().uri("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new RegisterReq(email, password, rol)) // tu DTO con rol
        .retrieve()
        .onStatus(s->s.value() == 409, rsp -> Mono.just(new WebClientResponseException(409, "email_taken",null,null,null)))
        .onStatus(HttpStatusCode::is4xxClientError, rsp->Mono.just(new WebClientResponseException(400, "invalid_form",null,null,null)))
        .bodyToMono(LoginResp.class)
        .onErrorReturn(new LoginResp(false,"","unknown"))
        .block();
  }

  @Override
  public StatsResp getAdminStats() {
    return http.get().uri("/api/admin/stats")
        .retrieve()
        .bodyToMono(StatsResp.class)
        .onErrorReturn(new StatsResp(0,0,0))
        .block();
  }
}
