package com.tomtom.ecommerce.order.mock;

import java.math.BigDecimal;

import com.tomtom.ecommerce.order.model.CartDetails;
import com.tomtom.ecommerce.order.model.PaymentMode;

public class CartDetailsMockFactory {

	
	public static CartDetails getCartDetails() {
		return  new CartDetails();
	}
	
	public static CartDetails getDummyValuedCartDetails() {
		return getCartDetails("addressOne", PaymentMode.CASH, "user1", BigDecimal.TEN);
	}
	
	
	public static CartDetails getCartDetails(String address, PaymentMode paymentMode,
					String userId, BigDecimal productPrice) {
		CartDetails cartDetails = new CartDetails();
		cartDetails.setAddress(address);
		cartDetails.setPaymentMode(paymentMode);
		cartDetails.setUserId(userId);
		cartDetails.setLstProductQuantityCart(ProductQuantityMockFactory.getProductQuantity());
		return cartDetails;
	}

}
