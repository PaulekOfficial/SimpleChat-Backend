package pro.paulek.simplechat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.paulek.simplechat.domain.auth.LoginRequest;
import pro.paulek.simplechat.domain.auth.LogoutRequest;
import pro.paulek.simplechat.domain.auth.RefreshRequest;
import pro.paulek.simplechat.domain.auth.SignupRequest;
import pro.paulek.simplechat.service.AuthorizationService;

@RestController
@CrossOrigin()
@RequestMapping("/api/v1/authenticate")
public class AuthorizationController {
    @Autowired
    AuthorizationService service;

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody LogoutRequest logoutRequest) {
        return service.logout(request, response, logoutRequest);
    }

    @PostMapping("/signin")
    @ResponseBody
    public ResponseEntity<?> authenticateUser(HttpServletRequest request, @Valid @RequestBody LoginRequest loginRequest) {
        return service.login(request, loginRequest);
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return service.register(signUpRequest);
    }

    @PostMapping("/refresh-token")
    @ResponseBody
    public ResponseEntity<?> refreshToken(HttpServletRequest request, @Valid @RequestBody RefreshRequest refreshRequest) {
        return service.refreshToken(request, refreshRequest);
    }

    @GetMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> logoutUser() {
        return service.verify();
    }
}
