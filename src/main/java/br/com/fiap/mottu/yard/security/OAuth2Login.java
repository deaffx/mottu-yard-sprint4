package br.com.fiap.mottu.yard.security;

import br.com.fiap.mottu.yard.model.Usuario;
import br.com.fiap.mottu.yard.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2Login extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User principal = oauth2Token.getPrincipal();
            String username = principal.getAttribute("login");

            if (username != null) {
                Usuario usuario = usuarioService.findByUsername(username).orElse(null);
                if (usuario != null) {
                    if (!Boolean.TRUE.equals(usuario.getPerfilConfirmado())) {
                        HttpSession session = request.getSession(true);
                        session.setAttribute("pendingUserId", usuario.getId());
                        getRedirectStrategy().sendRedirect(request, response, "/auth/selecionar-perfil");
                        return;
                    }
                    super.onAuthenticationSuccess(request, response, authentication);
                    return;
                }
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
