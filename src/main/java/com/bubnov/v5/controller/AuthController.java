package com.bubnov.v5.controller;

import com.bubnov.v5.model.dto.JwtAuthenticationResponse;
import com.bubnov.v5.model.dto.SignInRequest;
import com.bubnov.v5.model.dto.SignUpRequest;
import com.bubnov.v5.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @PostMapping("/sign-in")
//    @CrossOrigin(origins = "http://localhost:8086", methods = {RequestMethod.GET, RequestMethod.POST}, allowCredentials = "true")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}