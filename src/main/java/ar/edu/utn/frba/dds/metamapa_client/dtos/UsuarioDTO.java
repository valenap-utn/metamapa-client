package ar.edu.utn.frba.dds.metamapa_client.dtos;
import lombok.Data;
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
