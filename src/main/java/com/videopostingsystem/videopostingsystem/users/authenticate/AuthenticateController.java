package com.videopostingsystem.videopostingsystem.users.authenticate;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticateController {

    AuthenticateService authenticateService;

    public AuthenticateController(AuthenticateService authenticateService){
        this.authenticateService = authenticateService;
    }

    @GetMapping(path = "/confirm")
    public ResponseEntity<?> confirmToken(@RequestParam("token") String token){
        System.out.println(token);
        return authenticateService.confirmToken(token);
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resend(@RequestBody String email){
        return authenticateService.resendToken(email);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody AuthenticateModel signUp, HttpSession session) {
        return authenticateService.signup(signUp, session);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticateModel login, HttpSession session) {
        return authenticateService.login(login, session);
    }
}
