package com.tomtom.ecommerce.order.repository;

import org.springframework.data.repository.CrudRepository;

import com.tomtom.ecommerce.order.model.OrderDetails;

public interface OrderDataAccessRepository  extends CrudRepository<OrderDetails, String>{ 

}