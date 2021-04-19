package com.tomtom.ecommerce.repository;

import org.springframework.data.repository.CrudRepository;

import com.tomtom.ecommerce.model.OrderDetails;

public interface OrderDataAccessRepository  extends CrudRepository<OrderDetails, String>{ 

}