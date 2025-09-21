package ar.edu.utn.frba.dds.metamapa_client.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontrado extends RuntimeException {
  public RecursoNoEncontrado(String message) {
    super(message);
  }
}
