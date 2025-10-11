package ar.edu.utn.frba.dds.metamapa_client.clients.utils;


import ar.edu.utn.frba.dds.metamapa_client.dtos.UsuarioDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.Getter;

public class JwtUtil {
    @Getter
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 min
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

    public static String generarAccessToken(UsuarioDTO usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuer("gestion-alumnos-server")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .claim("idUsuario", usuario.getId())
                .signWith(key)
                .compact();
    }

    public static String generarRefreshToken(UsuarioDTO usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuer("gestion-alumnos-server")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .claim("type", "refresh") // diferenciamos refresh del access
                .claim("idUsuario", usuario.getId())
                .signWith(key)
                .compact();
    }

    public static String validarToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static Long getId(String token) {
        return Jwts.parserBuilder().setSigningKey(key)
                .build().parseClaimsJws(token).getBody()
                .get("idUsuario", Long.class);
    }
}
