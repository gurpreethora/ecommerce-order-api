package com.tomtom.ecommerce.service;

import com.tomtom.ecommerce.exception.ECommerceCartException;
import com.tomtom.ecommerce.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;

public interface ECommerceOrderService {

	OrderDetails placeOrder(CartDetails cartDetails) throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException;

	OrderDetails getUserOrders(String userId) throws NoOrdersFoundECommerceException;

}
