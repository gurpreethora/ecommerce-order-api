package com.tomtom.ecommerce.repository;

import org.springframework.data.repository.CrudRepository;

import com.tomtom.ecommerce.model.Product;

public interface ProductDataAccessRepository  extends CrudRepository<Product, Integer>{ 

}