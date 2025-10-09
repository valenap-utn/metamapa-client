package ar.edu.utn.frba.dds.metamapa_client.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

//A chequear esto, provisoriamente así...
@Data
public class UsuarioDTO {
  private Long id;
  private String email;
  private String password;
  private String nombre;
  private String apellido;
  private LocalDate fechaDeNacimiento;
  private String rol;
}
