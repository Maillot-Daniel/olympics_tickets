package com.olympics.tickets.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "offer_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_offer_type_name", columnNames = "name"))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom unique, obligatoire, longueur max 50 caractères
    @Column(nullable = false, length = 50)
    private String name;

    // Méthode statique pour créer une instance facilement
    public static OfferType create(String name) {
        return OfferType.builder()
                .name(name)
                .build();
    }

    // Met à jour le nom si valide (non nul, non vide)
    public void updateDetails(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    // Override equals pour comparer par ID uniquement (si ID est non nul)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OfferType offerType = (OfferType) o;
        return id != null && Objects.equals(id, offerType.id);
    }

    // Hash code basé sur la classe (recommandé par Hibernate pour entités)
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
