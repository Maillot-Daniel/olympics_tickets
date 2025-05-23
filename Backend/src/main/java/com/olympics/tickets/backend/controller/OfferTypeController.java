package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.entity.OfferType;
import com.olympics.tickets.backend.service.OfferTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offer_types")
public class OfferTypeController {

    private static final String ADMIN_ROLE = "hasRole('ADMIN')";

    private final OfferTypeService offerTypeService;

    @Autowired
    public OfferTypeController(OfferTypeService offerTypeService) {
        this.offerTypeService = offerTypeService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<OfferType>> getAllOfferTypes() {
        List<OfferType> offerTypes = offerTypeService.findAll();
        return ResponseEntity.ok(offerTypes);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize(ADMIN_ROLE)
    public ResponseEntity<OfferType> createOfferType(@Valid @RequestBody OfferType offerType) {
        OfferType created = offerTypeService.save(offerType);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @PreAuthorize(ADMIN_ROLE)
    public ResponseEntity<OfferType> updateOfferType(
            @PathVariable Integer id,
            @Valid @RequestBody OfferType offerType) {
        OfferType updated = offerTypeService.update(id, offerType);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLE)
    public ResponseEntity<Void> deleteOfferType(@PathVariable Integer id) {
        boolean deleted = offerTypeService.deleteById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
