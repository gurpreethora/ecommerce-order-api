package com.tomtom.ecommerce.order.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.tomtom.ecommerce.constants.ECommerceConstants;
import com.tomtom.ecommerce.exception.ECommerceCartException;
import com.tomtom.ecommerce.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.PaymentMode;
import com.tomtom.ecommerce.model.Product;
import com.tomtom.ecommerce.model.ResponseStatus;
import com.tomtom.ecommerce.order.mock.CartDetailsMockFactory;
import com.tomtom.ecommerce.order.mock.OrderDetailsMockFactory;
import com.tomtom.ecommerce.order.mock.ProductMockFactory;
import com.tomtom.ecommerce.repository.OrderDataAccessRepository;
import com.tomtom.ecommerce.service.ECommerceOrderServiceImpl;
@ExtendWith(MockitoExtension.class)
public class ECommerceOrderServiceImplTest {

	@InjectMocks
	private ECommerceOrderServiceImpl eCommerceServiceImpl;

	@Mock
	OrderDataAccessRepository orderDataAccessRepository;
	
	@Mock
    private RestTemplate restTemplate;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(eCommerceServiceImpl, "ecommerceProductApiName", "dummyProductURL");
		ReflectionTestUtils.setField(eCommerceServiceImpl, "ecommerceCartApiName", "dummyCartURL");
		ReflectionTestUtils.setField(eCommerceServiceImpl, "restTemplate", restTemplate);
	}

	private final String USER_ID = "userId";
	
	@Test
	public void placeOrderTest() throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException {
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		Product product1 = ProductMockFactory.getProduct("product1", 1, 5, BigDecimal.TEN);
		ResponseStatus responseStatus = ProductMockFactory.getResponseStatusProduct(product1);
		responseStatus.setStatus(ECommerceConstants.SUCCESS);
		Product product2 = ProductMockFactory.getProduct("product2", 2, 10, BigDecimal.ONE);
		ResponseStatus responseStatus2 = ProductMockFactory.getResponseStatusProduct(product2);
		when(restTemplate.getForObject(Mockito.anyString(), Mockito.anyObject()))
    	.thenReturn(cartDetails);
		
		when(restTemplate.getForObject(Mockito.anyString(), Mockito.anyObject()))
    	.thenReturn(responseStatus, responseStatus2);
		//ResponseEntity<ResponseStatus> responseStatus = new ResponseEntity<ResponseStatus>();
		
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(responseStatus, null, HttpStatus.OK);
		
		when(restTemplate.postForEntity(Mockito.anyString(), Mockito.anyObject(), Mockito.anyObject()))
    	.thenReturn(responseEntity);
		when(orderDataAccessRepository.save(Mockito.any(OrderDetails.class))).thenReturn(OrderDetailsMockFactory.getDummyValuedOrderDetails());
		
		eCommerceServiceImpl.placeOrder(cartDetails);
		verify(restTemplate).delete(Mockito.anyString());
		
	}
	
	@Test (expected = ProductNotFoundECommerceException.class)
	public void placeOrder_ProductNotFoundECommerceExceptionTest() throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException {
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		when(restTemplate.getForObject(Mockito.anyString(), Mockito.anyObject()))
    	.thenReturn(null);
		eCommerceServiceImpl.placeOrder(cartDetails);
	}
	
	@Test
	public void getUserOrdersTest() throws NoOrdersFoundECommerceException{
		when(orderDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(OrderDetailsMockFactory.getDummyValuedOrderDetails()));
		OrderDetails orderDetails = eCommerceServiceImpl.getUserOrders(USER_ID);
		assertNotNull(orderDetails);
		assertEquals(PaymentMode.CASH, orderDetails.getPaymentMode());
	}
	
	@Test (expected = NoOrdersFoundECommerceException.class)
	public void getUserOrders_NoOrdersFoundECommerceException() throws NoOrdersFoundECommerceException{
		when(orderDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(null));
		eCommerceServiceImpl.getUserOrders(USER_ID);
	}
}
