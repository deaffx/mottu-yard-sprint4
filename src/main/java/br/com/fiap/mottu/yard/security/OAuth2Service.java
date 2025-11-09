package br.com.fiap.mottu.yard.security;

import br.com.fiap.mottu.yard.model.Usuario;
import br.com.fiap.mottu.yard.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2Service extends DefaultOAuth2UserService {

    private final UsuarioService usuarioService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String username = (String) attributes.getOrDefault("login", null);
        if (username == null) {
            throw new UsernameNotFoundException("Usuário do GitHub sem login identificado");
        }

        String email = (String) attributes.getOrDefault("email", null);
        if (email == null || email.isBlank()) {
            email = username + "@users.noreply.github.com";
        }

        final String resolvedEmail = email;

        String name = Optional.ofNullable((String) attributes.get("name")).filter(s -> !s.isBlank()).orElse(username);
        String avatarUrl = (String) attributes.getOrDefault("avatar_url", null);

        Usuario usuario = usuarioService.findByUsername(username).orElseGet(() -> {
            Usuario novoUsuario = new Usuario();
            novoUsuario.setUsername(username);
            novoUsuario.setEmail(resolvedEmail);
            novoUsuario.setName(name);
            novoUsuario.setAvatarUrl(avatarUrl);
            novoUsuario.setRole(Usuario.Role.OPERADOR);
            novoUsuario.setPerfilConfirmado(false);
            return usuarioService.save(novoUsuario);
        });

        usuario.setEmail(resolvedEmail);
        usuario.setName(name);
        usuario.setAvatarUrl(avatarUrl);
        usuarioService.save(usuario);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
        }

        oauth2User.getAuthorities().forEach(authority -> {
            if (!authorities.contains(authority)) {
                authorities.add(authority);
            }
        });

        return new DefaultOAuth2User(authorities, attributes, "login");
    }
}
