package ar.edu.utn.frba.dds.metamapa_client.core;

import ar.edu.utn.frba.dds.metamapa_client.core.dtos.LoginResp;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.StatsResp;

public interface BackendAPI {
  LoginResp login(String email, String password);
//  void register(String email, String password, String rol);
  StatsResp getAdminStats(); //para el panel
  LoginResp register(String email,String password, String rol);
}
