package com.tomtom.ecommerce.order.service;

import com.tomtom.ecommerce.order.exception.ECommerceCartException;
import com.tomtom.ecommerce.order.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.order.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.order.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.order.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.order.model.CartDetails;
import com.tomtom.ecommerce.order.model.OrderDetails;

public interface ECommerceOrderService {

	OrderDetails placeOrder(CartDetails cartDetails) throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException;

	OrderDetails getUserOrders(String userId) throws NoOrdersFoundECommerceException;

}
