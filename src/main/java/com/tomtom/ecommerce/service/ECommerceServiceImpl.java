package com.tomtom.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tomtom.ecommerce.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.Product;
import com.tomtom.ecommerce.model.ProductOrder;
import com.tomtom.ecommerce.model.ProductQuantityCart;
import com.tomtom.ecommerce.repository.CartDataAccessRepository;
import com.tomtom.ecommerce.repository.OrderDataAccessRepository;
import com.tomtom.ecommerce.repository.ProductDataAccessRepository;

/**
 * @author Gurpreet Hora
 *
 */
@Service
@Transactional
public class ECommerceServiceImpl implements ECommerceOrderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ECommerceServiceImpl.class);

	private final ProductDataAccessRepository productDataAccessRepository;
	
	private final CartDataAccessRepository cartDataAccessRepository;
	
	private final OrderDataAccessRepository orderDataAccessRepository;
	
	@Autowired
	public ECommerceServiceImpl(ProductDataAccessRepository productDataAccessRepository,
			CartDataAccessRepository cartDataAccessRepository, OrderDataAccessRepository orderDataAccessRepository) {
		super();
		this.productDataAccessRepository = productDataAccessRepository;
		this.cartDataAccessRepository = cartDataAccessRepository;
		this.orderDataAccessRepository = orderDataAccessRepository;
	}

	@Override
	public void addProduct(Product product) throws PriceMisMatchECommerceException {
		LOGGER.debug("Trying to add product {}" ,product.getProductName());
		Optional<Product> existingProduct = this.productDataAccessRepository.findById(product.getProductId());
		if (existingProduct.isPresent()) {
			if(product.getProductPrice()!=null && existingProduct.get().getProductPrice().compareTo(product.getProductPrice()) !=0) {
				throw new PriceMisMatchECommerceException("Existing price for same product is "+existingProduct.get().getProductPrice()+" "
						+ "and new price supplied is "+product.getProductPrice()+", both should be same !");
			}
			existingProduct.get().setProductQuantity(existingProduct.get().getProductQuantity() + product.getProductQuantity());
			productDataAccessRepository.save(existingProduct.get());
		} else {
			productDataAccessRepository.save(product);
		}
		
		LOGGER.debug("Transaction successful to add product {}" ,product.getProductName());
	}

	@Override
	public Product getProduct(Integer productId) throws ProductNotFoundECommerceException  {
		LOGGER.debug("Trying to get product if{}" ,productId);
		Optional<Product> existingProduct = this.productDataAccessRepository.findById(productId);
		if (existingProduct.isPresent()) {
			LOGGER.debug("Product found for product id {}" ,productId);
			return existingProduct.get();
		} else {
			throw new ProductNotFoundECommerceException("Product not found for supplied productId : "+productId);
		}
		

	}

	
	@Override
	public OrderDetails getUserCart(String userId) throws EmptyCartECommerceException, ProductNotFoundECommerceException {
		Optional<CartDetails> existingCart = this.cartDataAccessRepository.findById(userId);
		if (existingCart.isPresent()) {
			return calculateCartValue(existingCart.get());
		} else {
			throw new EmptyCartECommerceException("No Products in cart for user : "+userId);
		}
	}

	/**
	 * @param cartDetails
	 * @return
	 * @throws ProductNotFoundECommerceException
	 * @throws EmptyCartECommerceException
	 * This method is responsible for calculating overall amounts from cart.
	 * It calculates amount at product level as well at order level. 
	 */
	public OrderDetails calculateCartValue(CartDetails cartDetails) throws ProductNotFoundECommerceException, EmptyCartECommerceException {
		OrderDetails orderDetails =new OrderDetails();
		List <ProductOrder> lstProductOrder = new ArrayList<>();
		Product product;
		ProductOrder productOrder;
		LOGGER.debug("Calculating cart value for User {}" , cartDetails.getUserId());
		if(cartDetails.getLstProductQuantityCart()== null || cartDetails.getLstProductQuantityCart().isEmpty()) {
			return this.getUserCart(cartDetails.getUserId());
		}
		for(ProductQuantityCart productQuantityCart : cartDetails.getLstProductQuantityCart()) {
			productOrder = new ProductOrder();
			product = this.getProduct(productQuantityCart.getProductId());
			productOrder.setProductName(product.getProductName());
			productOrder.setProductPrice(product.getProductPrice());
			productOrder.setProductId(productQuantityCart.getProductId());
			productOrder.setProductQuantity(productQuantityCart.getProductQuantity());
			productOrder.setTotalPrice(new BigDecimal(productQuantityCart.getProductQuantity()).multiply(product.getProductPrice()));
			lstProductOrder.add(productOrder);
		}
		orderDetails.setUserId(cartDetails.getUserId());
		orderDetails.setLstProducts(lstProductOrder);
		return orderDetails;
	}

	/**
	 * This method does the job of placing order
	 * 1. It gets all the products from cart and calculates its value, while calculating it again verifies stock quantity.
	 * 2. It places the order then in a transactional way -- Transaction starts
	 * 3. The product quantity is updated in stock as per the deductions from current order.
	 * 4. The user's cart is cleared -- Transaction Ends
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public OrderDetails placeOrder(CartDetails cartDetails) throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException {
		LOGGER.debug("Placing order for user {}" , cartDetails.getUserId());
		OrderDetails orderDetails =  orderDataAccessRepository.save(calculateCartValue(cartDetails));
		Product product = new Product();
		for(ProductOrder productOrder: orderDetails.getLstProducts()) {
			product.setProductId(productOrder.getProductId());
			product.setProductQuantity(-productOrder.getProductQuantity());
			this.addProduct(product);
		}
		this.deleteUserCart(cartDetails.getUserId());
		return orderDetails;
	}

	@Override
	public OrderDetails getUserOrders(String userId) throws NoOrdersFoundECommerceException {
		Optional<OrderDetails> existingOrderDetails = this.orderDataAccessRepository.findById(userId);
		if (existingOrderDetails.isPresent()) {
			return existingOrderDetails.get();
		} else {
			throw new NoOrdersFoundECommerceException("No Orders made by user : "+userId);
		}
	}

	@Override
	public void deleteUserCart(String userId) {
		this.cartDataAccessRepository.deleteById(userId);
	}
}
