package br.com.fiap.mottu.yard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class ResetRoleConfig {
    
    // Para desenvolvimento: define se o cargo do usuário deve ser resetado ao fazer logout.
    private boolean resetRoleOnLogout = false;
}
