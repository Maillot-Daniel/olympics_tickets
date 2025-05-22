package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.OfferType;
import org.springframework.data.repository.CrudRepository;

public interface OfferTypeRepository extends CrudRepository<OfferType, Long> {
    // Méthodes personnalisées si besoin
}
