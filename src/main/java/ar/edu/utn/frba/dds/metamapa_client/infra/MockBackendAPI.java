package ar.edu.utn.frba.dds.metamapa_client.infra;

import ar.edu.utn.frba.dds.metamapa_client.core.BackendAPI;
import ar.edu.utn.frba.dds.metamapa_client.core.dtos.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@ConditionalOnProperty(name="metamapa.mode", havingValue="mock", matchIfMissing = true)
public class MockBackendAPI implements BackendAPI {

  private final Map<String, User> users = new HashMap<>();

  public MockBackendAPI() {
    users.put("valucha@gmail.com",    new User("valucha@gmail.com",    "valuchaa", "CONTRIBUYENTE"));
    users.put("admin@example.com",   new User("admin@example.com",   "metamapa", "ADMIN"));
    users.put("contrib@example.com", new User("contrib@example.com", "metamapa", "CONTRIBUYENTE"));
  }

  @Override
  public LoginResp login(String email, String password) {
    var user = users.get(key(email));
    if(user == null || !user.password.equals(password)) {
      return LoginResp.fail("invalid_credentials");
    }
    return LoginResp.ok(user.rol);
  }

  @Override
  public LoginResp register(String email, String password, String rol) {
    // Validaciones mínimas
    if (isBlank(email) || isBlank(password)) {
      return LoginResp.fail("invalid_form");
    }

    String k = key(email);
    if (users.containsKey(k)) {
      // simulamos “email ya registrado”
      var existente = users.get(k);
      return LoginResp.fail("email_taken");
    }

    String role = normalizeRole(rol);
    users.put(k, new User(email.trim(), password, role));
    return LoginResp.ok(role);
  }

  @Override
  public StatsResp getAdminStats() {
    return new StatsResp(124, 8, 3); //cambiar mas adelante por lo que se deba xd
  }

  // helpers
  private static String key(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }


  private static String normalizeRole(String r) {
    String x = (r == null ? "" : r).trim().toUpperCase();
    if (x.startsWith("ADMIN")) return "ADMIN";
    if (x.startsWith("CONTRI")) return "CONTRIBUYENTE";
    return "CONTRIBUYENTE"; // default
  }

  // DTO local del mock
  private record User (String email, String password, String rol) {}
}
