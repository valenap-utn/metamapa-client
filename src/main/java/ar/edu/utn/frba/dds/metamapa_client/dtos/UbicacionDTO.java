package ar.edu.utn.frba.dds.metamapa_client.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UbicacionDTO {
    private String id;
    private String nombre;
    private Provincia provincia; // en municipios y departamentos, viene anidada

    @Getter
    @Setter
    public static class Provincia {
        private String id;
        private String nombre;
    }
}