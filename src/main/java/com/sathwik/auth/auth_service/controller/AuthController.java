package com.sathwik.auth.auth_service.controller;

import com.sathwik.auth.auth_service.dto.LoginRequest;
import com.sathwik.auth.auth_service.dto.RegisterRequest;
import com.sathwik.auth.auth_service.entity.UserEntity;
import com.sathwik.auth.auth_service.repository.UserRepository;
import com.sathwik.auth.auth_service.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


// this is the authentication section..
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepo;
    public AuthController(JwtService jwtService,UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;

    }


    //get Maps..
    @GetMapping("/")
    public String home(){
        return "base";
    }
    @GetMapping("/register")
    public String register(){
        return "/auth/register";
    }
    @GetMapping("/login")
    public String login(){
        return "/auth/login";
    }


    // POST maps..
    @PostMapping("/register")
    public ResponseEntity<?> register(
           @RequestBody RegisterRequest dto) {
        try {
            UserEntity savedUser =
                    userRepo.save(new UserEntity(
                            dto.getUserName(),
                            dto.getEmail(),
                            dto.getPassword()
                    ));

            String token = jwtService.generateToken(savedUser.getUserId());
            return ResponseEntity.status(201).body(Map.of("token",token));
        } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Registration unsuccessful!"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto) {

        UserEntity user = userRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getUserId());

        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/ping")
    public String ping() {
        return "Auth service running";
    }
}

