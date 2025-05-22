package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<OurUsers, Long> {

    // Méthode retournant un Optional pour gérer le cas où l'utilisateur n'existe pas
    Optional<OurUsers> findByEmail(String email);

    // Vérifie si un utilisateur existe avec cet email
    boolean existsByEmail(String email);

    // Recherche les utilisateurs par rôle
    List<OurUsers> findByRole(OurUsers.Role role);

    // Méthode redondante avec findByEmail(Optional), à éviter pour éviter ambiguïté
    // OurUsers findByEmail(String email); // À supprimer car findByEmail(Optional) suffit

}
