package ar.edu.utn.frba.dds.metamapa_client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.security")
public class RoleMappingProperties {
  //podemos setear desde properties emails que si se utilizan pueden ser rolearse para admins,
  // caso contrario te rolea como contrib
  private List<String> adminEmails = new ArrayList<>();

  //también podemos setear dominios para rolearlos como admin directamente
  private List<String> adminDomains = new ArrayList<>();
}

@Configuration
@EnableConfigurationProperties(RoleMappingProperties.class)
class PropsConfig {}
