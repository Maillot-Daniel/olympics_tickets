package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.repository.UsersRepo;
import com.olympics.tickets.backend.security.JWTUtils;
import com.olympics.tickets.backend.dto.ReqRes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();

        try {
            OurUsers ourUsers = new OurUsers();
            ourUsers.setEmail(registrationRequest.getEmail());
            ourUsers.setCity(registrationRequest.getCity());

            try {
                ourUsers.setRole(OurUsers.Role.valueOf(registrationRequest.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                resp.setStatusCode(400);
                resp.setMessage("Role invalide : " + registrationRequest.getRole());
                return resp;
            }

            ourUsers.setName(registrationRequest.getName());
            ourUsers.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

            OurUsers savedUser = usersRepo.save(ourUsers);

            resp.setOurUsers(savedUser);
            resp.setStatusCode(201);
            resp.setMessage("User registered successfully");

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
            resp.setMessage("Error during registration");
        }

        return resp;
    }

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            OurUsers user = usersRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().name());

            String jwt = jwtUtils.generateToken(claims, user);
            // Utilisation de generateToken avec une expiration plus longue pour le refresh token
            String refreshToken = jwtUtils.generateToken(new HashMap<>(), user, "refresh");

            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole().name());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        } catch (Exception e) {
            response.setStatusCode(401);
            response.setMessage("Authentication failed: " + e.getMessage());
        }

        return response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();

        try {
            String email = jwtUtils.extractUsername(refreshTokenRequest.getRefreshToken());
            OurUsers user = usersRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            if (jwtUtils.isTokenValid(refreshTokenRequest.getRefreshToken(), user)) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("role", user.getRole().name());

                String newToken = jwtUtils.generateToken(claims, user);
                response.setToken(newToken);
                response.setRefreshToken(refreshTokenRequest.getRefreshToken());
                response.setExpirationTime("24Hrs");
                response.setStatusCode(200);
                response.setMessage("Successfully refreshed token");
            } else {
                response.setStatusCode(401);
                response.setMessage("Invalid refresh token");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while refreshing token: " + e.getMessage());
        }

        return response;
    }

    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();

        try {
            List<OurUsers> result = usersRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setOurUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Users fetched successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }

        return reqRes;
    }

    public ReqRes getUsersById(Long id) {
        ReqRes reqRes = new ReqRes();
        try {
            OurUsers userById = usersRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User Not Found"));
            reqRes.setOurUsers(userById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("User with ID '" + id + "' found successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(404);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes deleteUser(Long userId) {
        ReqRes reqRes = new ReqRes();

        try {
            Optional<OurUsers> usersOptional = usersRepo.findById(userId);
            if (usersOptional.isPresent()) {
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for deletion");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user: " + e.getMessage());
        }

        return reqRes;
    }

    public ReqRes updateUser(Long userId, OurUsers updateUser) {
        ReqRes reqRes = new ReqRes();

        try {
            Optional<OurUsers> userOptional = usersRepo.findById(userId);
            if (userOptional.isPresent()) {
                OurUsers existingUser = userOptional.get();

                existingUser.setEmail(updateUser.getEmail());
                existingUser.setName(updateUser.getName());
                existingUser.setCity(updateUser.getCity());
                existingUser.setRole(updateUser.getRole());

                if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
                }

                OurUsers savedUser = usersRepo.save(existingUser);
                reqRes.setOurUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }

        return reqRes;
    }

    public ReqRes getMyInfo(String email) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findByEmail(email);
            if (userOptional.isPresent()) {
                reqRes.setOurUsers(userOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("User found successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }

        return reqRes;
    }
}