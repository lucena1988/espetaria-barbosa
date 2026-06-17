package br.com.espetariabarbosa.config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class MenuPermissionsAdvice {

    @ModelAttribute("canDashboard")
    public boolean canDashboard(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "CAIXA", "FUNCIONARIO");
    }

    @ModelAttribute("canPedidos")
    public boolean canPedidos(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO");
    }

    @ModelAttribute("canNovoPedido")
    public boolean canNovoPedido(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO");
    }

    @ModelAttribute("canClientes")
    public boolean canClientes(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO");
    }

    @ModelAttribute("canMesas")
    public boolean canMesas(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO");
    }

    @ModelAttribute("canCadastrosAdmin")
    public boolean canCadastrosAdmin(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN");
    }

    @ModelAttribute("canFuncionarios")
    public boolean canFuncionarios(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN");
    }

    @ModelAttribute("canCozinha")
    public boolean canCozinha(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "COZINHA", "FUNCIONARIO");
    }

    @ModelAttribute("canPainel")
    public boolean canPainel(Authentication authentication) {
        return hasAnyRole(authentication, "ADMIN", "COZINHA", "FUNCIONARIO");
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (String role : roles) {
            String authority = "ROLE_" + role;
            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));

            if (hasRole) {
                return true;
            }
        }

        return false;
    }
}
