package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.entity.OfferType;

import java.util.List;

public interface OfferTypeService {

    List<OfferType> findAll();

    OfferType save(OfferType offerType);

    OfferType update(Integer id, OfferType offerType);

    boolean deleteById(Integer id);
}
