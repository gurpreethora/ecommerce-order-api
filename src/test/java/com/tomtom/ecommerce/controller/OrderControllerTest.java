package com.tomtom.ecommerce.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tomtom.ecommerce.constants.ECommerceConstants;
import com.tomtom.ecommerce.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.mock.CartDetailsMockFactory;
import com.tomtom.ecommerce.mock.OrderDetailsMockFactory;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.PaymentMode;
import com.tomtom.ecommerce.model.ResponseStatus;
import com.tomtom.ecommerce.service.ECommerceOrderService;
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
	
	@InjectMocks
	private OrderController userController;
	
	@Mock
	private ECommerceOrderService commerceService ;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.openMocks(this);
	}

	private final String USER_ID = "userId";
	
	
	@Test
	public void placeOrderTest() throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException {
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		OrderDetails orderDetails = OrderDetailsMockFactory.getDummyValuedOrderDetails();
		when(commerceService.placeOrder(cartDetails)).thenReturn(orderDetails);
		ResponseEntity<ResponseStatus> respo = userController.placeOrder(USER_ID, cartDetails);
		assertNotNull(respo);
		assertEquals(ECommerceConstants.SUCCESS,respo.getBody().getStatus());
		assertEquals("Order Placed",respo.getBody().getOrderDetails().getStatus());
		assertEquals(HttpStatus.CREATED, respo.getStatusCode());
	}
	

	@Test
	public void placeOrderTest_EmptyCartECommerceException() throws EmptyCartECommerceException, ProductNotFoundECommerceException, PriceMisMatchECommerceException{
		CartDetails cartDetails = CartDetailsMockFactory.getCartDetails();
		when(commerceService.placeOrder(cartDetails)).thenThrow(new EmptyCartECommerceException("Cart Empty"));
		ResponseEntity<ResponseStatus> respo = userController.placeOrder(USER_ID, cartDetails);
		assertNotNull(respo);
		assertEquals(HttpStatus.OK, respo.getStatusCode());
		assertEquals(ECommerceConstants.FAILURE, respo.getBody().getStatus());
		assertEquals("Cart Empty", respo.getBody().getMessages().get(0));
	}
	
	@Test
	public void placeOrderTest_ProductNotFoundECommerceException() throws EmptyCartECommerceException, ProductNotFoundECommerceException, PriceMisMatchECommerceException{
		CartDetails cartDetails = CartDetailsMockFactory.getCartDetails();
		when(commerceService.placeOrder(cartDetails)).thenThrow(new ProductNotFoundECommerceException("Product Not Found"));
		ResponseEntity<ResponseStatus> respo = userController.placeOrder(USER_ID, cartDetails);
		assertNotNull(respo);
		assertEquals(HttpStatus.OK, respo.getStatusCode());
		assertEquals("Failure", respo.getBody().getStatus());
		assertEquals("Product Not Found", respo.getBody().getMessages().get(0));
	}

	@Test
	public void getUserOrdersTest() throws NoOrdersFoundECommerceException {
		when(commerceService.getUserOrders(USER_ID)).thenReturn(OrderDetailsMockFactory.getDummyValuedOrderDetails());
		ResponseEntity<ResponseStatus> respo = userController.getUserOrders(USER_ID);
		assertNotNull(respo);
		assertEquals(ECommerceConstants.SUCCESS,respo.getBody().getStatus());
		assertEquals("Order Placed",respo.getBody().getOrderDetails().getStatus());
		assertEquals(PaymentMode.CASH,respo.getBody().getOrderDetails().getPaymentMode());
		assertEquals(HttpStatus.OK, respo.getStatusCode());
	}
	
	@Test
	public void getUserOrders_NoOrdersFoundECommerceExceptionTest() throws NoOrdersFoundECommerceException {
		when(commerceService.getUserOrders(USER_ID)).thenThrow(new NoOrdersFoundECommerceException("Order Not Found"));
		ResponseEntity<ResponseStatus> respo = userController.getUserOrders(USER_ID);
		assertNotNull(respo);
		assertEquals(HttpStatus.OK, respo.getStatusCode());
		assertEquals("Failure", respo.getBody().getStatus());
		assertEquals("Order Not Found", respo.getBody().getMessages().get(0));
	}
}
