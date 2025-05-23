package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.entity.OfferType;
import com.olympics.tickets.backend.repository.OfferTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferTypeServiceImpl implements OfferTypeService {

    private final OfferTypeRepository repository;

    @Autowired
    public OfferTypeServiceImpl(OfferTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OfferType> findAll() {
        return repository.findAll();
    }

    @Override
    public OfferType save(OfferType offerType) {
        return repository.save(offerType);
    }

    @Override
    public OfferType update(Integer id, OfferType offerType) {
        Optional<OfferType> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }
        OfferType existing = existingOpt.get();
        existing.setName(offerType.getName()); // exemple d'attribut, adapte selon ton entity
        existing.setDescription(offerType.getDescription()); // idem
        return repository.save(existing);
    }

    @Override
    public boolean deleteById(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
