package ar.edu.utn.frba.dds.metamapa_client.core.dtos;

public record LoginResp(boolean ok, String rol, String error) {

  public static LoginResp ok(String rol) {
    return new LoginResp(true, rol, null);
  }

  public static LoginResp fail(String errorCode) {
    return new LoginResp(false, "", errorCode);
  }
}
