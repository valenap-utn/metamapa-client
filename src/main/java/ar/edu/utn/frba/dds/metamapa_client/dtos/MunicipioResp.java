package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MunicipioResp {
    private List<UbicacionDTO> municipios;
    private int cantidad;
    private int total;
}