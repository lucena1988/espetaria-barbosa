package br.com.espetariabarbosa.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (hasRole(authentication, "COZINHA")) {
            return "redirect:/cozinha";
        }

        if (hasRole(authentication, "ATENDENTE")) {
            return "redirect:/pedidos";
        }

        return "redirect:/dashboard";
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
