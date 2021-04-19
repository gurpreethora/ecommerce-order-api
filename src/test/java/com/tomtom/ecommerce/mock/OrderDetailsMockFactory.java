package com.tomtom.ecommerce.mock;

import java.math.BigDecimal;

import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.PaymentMode;

public class OrderDetailsMockFactory {

	
	public static OrderDetails getOrderDetails() {
		return  new OrderDetails();
	}
	
	public static OrderDetails getDummyValuedOrderDetails() {
		return getOrderDetails("addressOne", PaymentMode.CASH, "user1", BigDecimal.TEN);
	}
	
	
	public static OrderDetails getOrderDetails(String address, PaymentMode paymentMode,
					String userId, BigDecimal productPrice) {
		OrderDetails orderDetails = new OrderDetails();
		orderDetails.setAddress(address);
		orderDetails.setPaymentMode(paymentMode);
		orderDetails.setUserId(userId);
		orderDetails.setLstProducts(ProductOrderMockFactory.getProductOrder());
		return orderDetails;
	}
}
