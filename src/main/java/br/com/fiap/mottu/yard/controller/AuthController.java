package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.model.Usuario;
import br.com.fiap.mottu.yard.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "oauth2Error", required = false) String oauth2Error,
                        HttpServletRequest request,
                        Model model) {

        ClientRegistration githubRegistration = null;
        try {
            githubRegistration = clientRegistrationRepository.findByRegistrationId("github");
        } catch (Exception ignored) {
        }

        boolean githubConfigured = githubRegistration != null
                && githubRegistration.getClientId() != null
                && !githubRegistration.getClientId().isBlank()
                && !"github-client-id-placeholder".equals(githubRegistration.getClientId());

        if (error != null) {
            AuthenticationException authException = (AuthenticationException) request.getSession()
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

            if (oauth2Error != null || authException instanceof OAuth2AuthenticationException) {
                model.addAttribute("error", "Não foi possível autenticar com o GitHub. Verifique se as credenciais do aplicativo estão configuradas corretamente.");
            } else {
                model.addAttribute("error", "Não foi possível concluir o login. Tente novamente usando sua conta GitHub.");
            }
        }

        if (logout != null) {
            model.addAttribute("message", "Sessão encerrada com sucesso.");
        }

        if (githubConfigured) {
            model.addAttribute("githubAuthUrl", "/oauth2/authorization/github");
        }
        model.addAttribute("githubConfigured", githubConfigured);

        return "auth/login";
    }

    @GetMapping("/auth/selecionar-perfil")
    public String selecionarPerfil(@RequestParam(value = "force", defaultValue = "false") boolean force,
                                   HttpSession session,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = recuperarUsuarioSessaoOuAutenticacao(session, authentication);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Não foi possível identificar o usuário logado.");
            return "redirect:/dashboard";
        }

        if (!force && Boolean.TRUE.equals(usuario.getPerfilConfirmado())) {
            redirectAttributes.addFlashAttribute("info", "Seu perfil já está confirmado como " + usuario.getRole().name().toLowerCase() + ".");
            return "redirect:/dashboard";
        }

        session.setAttribute("pendingUserId", usuario.getId());

        if (force && Boolean.TRUE.equals(usuario.getPerfilConfirmado())) {
            model.addAttribute("info", "Selecione um novo perfil para atualizar seus acessos.");
        }

        model.addAttribute("usuario", usuario);
        return "auth/select-role";
    }

    @PostMapping("/auth/selecionar-perfil")
    public String confirmarPerfil(@RequestParam("role") String role,
                                  HttpSession session,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = recuperarUsuarioSessaoOuAutenticacao(session, authentication);
        if (usuario == null) {
            return "redirect:/dashboard";
        }

        Usuario.Role roleSelecionado;
        try {
            roleSelecionado = Usuario.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", "Perfil inválido selecionado.");
            return "redirect:/auth/selecionar-perfil";
        }

        usuario.setRole(roleSelecionado);
        usuario.setPerfilConfirmado(true);
        usuarioService.save(usuario);
        session.removeAttribute("pendingUserId");

        atualizarContextoSeguranca(authentication, roleSelecionado);

        redirectAttributes.addFlashAttribute("profileSuccess", "Perfil atualizado com sucesso!");
        return "redirect:/dashboard";
    }

    private Usuario recuperarUsuarioSessaoOuAutenticacao(HttpSession session, Authentication authentication) {
        Long pendingId = (Long) session.getAttribute("pendingUserId");
        if (pendingId != null) {
            return usuarioService.findById(pendingId).orElse(null);
        }

        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            String username = principal.getAttribute("login");
            if (username != null) {
                return usuarioService.findByUsername(username).orElse(null);
            }
        }

        return null;
    }

    private void atualizarContextoSeguranca(Authentication authentication, Usuario.Role role) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
            DefaultOAuth2User novoPrincipal = new DefaultOAuth2User(
                    java.util.List.of(authority),
                    principal.getAttributes(),
                    "login"
            );

            OAuth2AuthenticationToken novoToken = new OAuth2AuthenticationToken(
                    novoPrincipal,
                    java.util.List.of(authority),
                    token.getAuthorizedClientRegistrationId()
            );

            SecurityContextHolder.getContext().setAuthentication(novoToken);
        }
    }
}