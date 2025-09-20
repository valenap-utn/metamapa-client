package ar.edu.utn.frba.dds.metamapa_client.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Component
public class RememberService {

  @Value("${mm.remember.secret")
  private String secret;

  @Value("${mm.rememvber.days:30}")
  private int days;

  @Value("${mm.remember.secure:false}")
  private boolean secure;


  private static final String COOKIE = "MM_REMEMBER";

  public void setRememberCookie(HttpServletResponse response, String email, String role) {
    long exp = Instant.now().plusSeconds(days * 24L * 3600L).getEpochSecond();
    String payload = b64(email) + "." + b64(role) + "." + exp;
    String sig = sign(payload);
    String value = payload + "." + sig;
    Cookie c = new Cookie(COOKIE, URLEncoder.encode(value, StandardCharsets.UTF_8));
    c.setHttpOnly(true);
    c.setSecure(secure);           // configurable
    c.setPath("/");
    c.setMaxAge(days * 24 * 3600);
    c.setAttribute("SameSite", "Lax");
    response.addCookie(c);
  }

  public Optional<RememberPayload> parseCookie(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies == null) return Optional.empty();
    for (Cookie c : cookies) {
      if (!COOKIE.equals(c.getName())) continue;
      String value = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
      String[] parts = value.split("\\.");
      if (parts.length != 4) return Optional.empty();
      String payload = parts[0] + "." + parts[1] + "." + parts[2];
      if (!secureEquals(parts[3], sign(payload))) return Optional.empty();
      String email = ub64(parts[0]);
      String role  = ub64(parts[1]);
      long exp = Long.parseLong(parts[2]);
      if (Instant.now().getEpochSecond() > exp) return Optional.empty();
      return Optional.of(new RememberPayload(email, role, exp));
    }
    return Optional.empty();
  }

  public void clearCookie(HttpServletResponse res) {
    Cookie c = new Cookie(COOKIE, "");
    c.setPath("/");
    c.setMaxAge(0);
    c.setHttpOnly(true);
    c.setAttribute("SameSite", "Lax");
    res.addCookie(c);
  }

  private String sign(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private static String b64(String s){ return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }

  private static String ub64(String s){ return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8); }

  private static boolean secureEquals(String a, String b){
    if (a.length()!=b.length()) return false;
    int r=0; for (int i=0;i<a.length();i++) r|=a.charAt(i)^b.charAt(i); return r==0;
  }

  public record RememberPayload(String email, String role, long exp) {}

}
