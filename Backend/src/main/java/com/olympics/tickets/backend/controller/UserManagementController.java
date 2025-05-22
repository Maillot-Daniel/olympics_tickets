package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.dto.ReqRes;
import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.service.UsersManagementService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequestMapping("/users")
public class UserManagementController {

    private final UsersManagementService usersManagementService;

    public UserManagementController(UsersManagementService usersManagementService) {
        this.usersManagementService = usersManagementService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes registrationRequest) {
        return ResponseEntity.ok(usersManagementService.register(registrationRequest));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes loginRequest) {
        return ResponseEntity.ok(usersManagementService.login(loginRequest));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ReqRes> refreshToken(@RequestBody ReqRes refreshRequest) {
        return ResponseEntity.ok(usersManagementService.refreshToken(refreshRequest));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ReqRes> getAllUsers() {
        return ResponseEntity.ok(usersManagementService.getAllUsers());
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<ReqRes> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(usersManagementService.getUsersById(userId));
    }

    @PutMapping("/admin/{userId}")
    public ResponseEntity<ReqRes> updateUser(
            @PathVariable Long userId,
            @RequestBody OurUsers userUpdateRequest) {
        return ResponseEntity.ok(usersManagementService.updateUser(userId, userUpdateRequest));
    }

    @GetMapping("/admin/get-profile")
    public ResponseEntity<ReqRes> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + authentication);
        String email = authentication.getName();
        System.out.println("Email extracted from token: " + email);

        ReqRes response = usersManagementService.getMyInfo(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ReqRes> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }
}