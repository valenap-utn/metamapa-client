package ar.edu.utn.frba.dds.metamapa_client.clients;

import org.springframework.web.multipart.MultipartFile;

public interface IFuenteEstatica {
    String subirHechosCSV(MultipartFile archivo, Long idUsuario, String baseURL);
}
