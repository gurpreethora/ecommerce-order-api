package com.tomtom.ecommerce.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

import com.tomtom.ecommerce.exception.ECommerceCartException;
import com.tomtom.ecommerce.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.mock.CartDetailsMockFactory;
import com.tomtom.ecommerce.mock.OrderDetailsMockFactory;
import com.tomtom.ecommerce.mock.ProductMockFactory;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.PaymentMode;
import com.tomtom.ecommerce.model.Product;
import com.tomtom.ecommerce.repository.CartDataAccessRepository;
import com.tomtom.ecommerce.repository.OrderDataAccessRepository;
import com.tomtom.ecommerce.repository.ProductDataAccessRepository;
@ExtendWith(MockitoExtension.class)
public class ECommerceServiceImplTest {

	@InjectMocks
	private ECommerceServiceImpl eCommerceServiceImpl;

	@Mock
	ProductDataAccessRepository productDataAccessRepository;
	
	@Mock
	CartDataAccessRepository cartDataAccessRepository;
	
	@Mock
	OrderDataAccessRepository orderDataAccessRepository;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	private final String USER_ID = "userId";
	
	@Test
	public void addProductTest() throws PriceMisMatchECommerceException, ProductNotFoundECommerceException {
		Optional<Product> product = Optional.ofNullable(ProductMockFactory.getDummyValuedProduct());
		when(productDataAccessRepository.findById(product.get().getProductId())).thenReturn(product);
		when(productDataAccessRepository.save(Mockito.any(Product.class))).thenReturn(new Product());
		eCommerceServiceImpl.addProduct(product.get());
		verify(productDataAccessRepository).save(Mockito.any(Product.class));
	}
	
	@Test(expected = PriceMisMatchECommerceException.class)
	public void addProduct_PriceMisMatchECommerceExceptionTest() throws PriceMisMatchECommerceException, ProductNotFoundECommerceException {
		Optional<Product> product = Optional.ofNullable(ProductMockFactory.getDummyValuedProduct());
		Product productWithPriceDiff = ProductMockFactory.getDummyValuedProduct();
		productWithPriceDiff.setProductPrice(BigDecimal.ZERO);
		when(productDataAccessRepository.findById(product.get().getProductId())).thenReturn(product);
		eCommerceServiceImpl.addProduct(productWithPriceDiff);
	}

	
	@Test
	public void getproductTest() throws ProductNotFoundECommerceException {
		
		Optional<Product> inputproduct = Optional.ofNullable(ProductMockFactory.getDummyValuedProduct());
		when(productDataAccessRepository.findById(inputproduct.get().getProductId())).thenReturn(inputproduct);
		Product productOp = eCommerceServiceImpl.getProduct(inputproduct.get().getProductId());
		assertNotNull(productOp);
		assertTrue(productOp.equals(productOp));
		assertEquals(inputproduct.get().getProductId(), productOp.getProductId());
	}
	
	@Test(expected = ProductNotFoundECommerceException.class)
	public void getproduct_ProductNotFoundECommerceExceptionTest() throws ProductNotFoundECommerceException {
		Optional<Product> inputproduct = Optional.ofNullable(null);
		when(productDataAccessRepository.findById(11)).thenReturn(inputproduct);
		eCommerceServiceImpl.getProduct(11);
	}
	
	
	@Test
	public void getUserCartTest() throws EmptyCartECommerceException, ProductNotFoundECommerceException, ECommerceCartException{
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		Product product1 = ProductMockFactory.getProduct("product1", 1, 5, BigDecimal.TEN);
		Product product2 = ProductMockFactory.getProduct("product2", 2, 10, BigDecimal.ONE);
		when(cartDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(cartDetails));
		when(productDataAccessRepository.findById(product1.getProductId())).thenReturn(Optional.ofNullable(product1));
		when(productDataAccessRepository.findById(product2.getProductId())).thenReturn(Optional.ofNullable(product2));
		OrderDetails orderDetails = eCommerceServiceImpl.getUserCart(USER_ID);
		assertNotNull(orderDetails);
		assertEquals(cartDetails.getUserId(), orderDetails.getUserId());
		assertEquals(product1.getProductId(),orderDetails.getLstProducts().get(0).getProductId());
		assertEquals(product2.getProductId(),orderDetails.getLstProducts().get(1).getProductId());
		assertEquals(product1.getProductPrice().
				multiply(new BigDecimal(product1.getProductQuantity())),
				orderDetails.getLstProducts().get(0).getTotalPrice());
		assertEquals(product2.getProductPrice().
				multiply(new BigDecimal(product2.getProductQuantity())),
				orderDetails.getLstProducts().get(1).getTotalPrice());
		
		assertEquals(new BigDecimal(60), orderDetails.getLstProducts().stream().map(x->x.getTotalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add)); 
	}
	
	@Test (expected = EmptyCartECommerceException.class)
	public void getUserCart_EmptyCartECommerceExceptionTest() throws EmptyCartECommerceException, ProductNotFoundECommerceException, ECommerceCartException{
		when(cartDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(null));
		eCommerceServiceImpl.getUserCart(USER_ID);
	}
	
	@Test (expected = ProductNotFoundECommerceException.class)
	public void getUserCart_ProductNotFoundECommerceExceptionTest() throws EmptyCartECommerceException, ProductNotFoundECommerceException, ECommerceCartException{
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		when(cartDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(cartDetails));
		eCommerceServiceImpl.getUserCart(USER_ID);
	}
	
	@Test
	public void placeOrderTest() throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException {
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		Product product1 = ProductMockFactory.getProduct("product1", 1, 5, BigDecimal.TEN);
		Product product2 = ProductMockFactory.getProduct("product2", 2, 10, BigDecimal.ONE);
		
		when(cartDataAccessRepository.findById(cartDetails.getUserId())).thenReturn(Optional.ofNullable(cartDetails));
		when(productDataAccessRepository.findById(product1.getProductId())).thenReturn(Optional.ofNullable(product1));
		when(productDataAccessRepository.findById(product2.getProductId())).thenReturn(Optional.ofNullable(product2));
		when(orderDataAccessRepository.save(Mockito.any(OrderDetails.class))).thenReturn(OrderDetailsMockFactory.getDummyValuedOrderDetails());
		
		eCommerceServiceImpl.placeOrder(cartDetails);
		verify(cartDataAccessRepository).deleteById(cartDetails.getUserId());
		
	}
	
	@Test (expected = ProductNotFoundECommerceException.class)
	public void placeOrder_ProductNotFoundECommerceExceptionTest() throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException {
		CartDetails cartDetails = CartDetailsMockFactory.getDummyValuedCartDetails();
		when(cartDataAccessRepository.findById(cartDetails.getUserId())).thenReturn(Optional.ofNullable(null));
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
	public void NoOrdersFoundECommerceException() throws NoOrdersFoundECommerceException{
		when(orderDataAccessRepository.findById(USER_ID)).thenReturn(Optional.ofNullable(null));
		eCommerceServiceImpl.getUserOrders(USER_ID);
	}
	
	
	@Test
	public void deleteUserCartTest() {
		eCommerceServiceImpl.deleteUserCart(USER_ID);
		verify(cartDataAccessRepository).deleteById(USER_ID);
	}
}
