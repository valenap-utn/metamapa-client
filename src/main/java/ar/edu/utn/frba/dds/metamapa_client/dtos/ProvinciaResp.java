package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProvinciaResp {
    private List<UbicacionDTO> provincias;
    private int cantidad;
    private int total;
}