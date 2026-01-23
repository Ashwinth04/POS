package com.increff.pos.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/session")
    public String debugSession(HttpServletRequest request, Authentication auth) {
        HttpSession session = request.getSession(false);

        return """
            Session ID: %s
            Auth null?: %s
            Principal: %s
            """.formatted(
                session == null ? "null" : session.getId(),
                auth == null,
                auth == null ? "null" : auth.getName()
        );
    }
}


