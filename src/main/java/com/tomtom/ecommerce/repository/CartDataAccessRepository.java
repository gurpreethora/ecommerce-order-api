package com.tomtom.ecommerce.repository;

import org.springframework.data.repository.CrudRepository;

import com.tomtom.ecommerce.model.CartDetails;

public interface CartDataAccessRepository  extends CrudRepository<CartDetails, String>{ 

}