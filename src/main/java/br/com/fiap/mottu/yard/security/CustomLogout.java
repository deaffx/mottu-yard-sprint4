package br.com.fiap.mottu.yard.security;

import br.com.fiap.mottu.yard.config.ResetRoleConfig;
import br.com.fiap.mottu.yard.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogout implements LogoutSuccessHandler {

    private final UsuarioService usuarioService;
    private final ResetRoleConfig resetRoleConfig;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (resetRoleConfig.isResetRoleOnLogout() && authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            String username = principal.getAttribute("login");
            
            if (username != null) {
                usuarioService.findByUsername(username).ifPresent(usuario -> {
                    usuario.setPerfilConfirmado(false);
                    usuarioService.save(usuario);
                });
            }
        }
        
        response.sendRedirect("/login?logout");
    }
}
