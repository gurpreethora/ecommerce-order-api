package com.tomtom.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.tomtom.ecommerce.order.constants.ECommerceConstants;
import com.tomtom.ecommerce.order.exception.ECommerceCartException;
import com.tomtom.ecommerce.order.exception.EmptyCartECommerceException;
import com.tomtom.ecommerce.order.exception.NoOrdersFoundECommerceException;
import com.tomtom.ecommerce.order.exception.PriceMisMatchECommerceException;
import com.tomtom.ecommerce.order.exception.ProductNotFoundECommerceException;
import com.tomtom.ecommerce.order.model.CartDetails;
import com.tomtom.ecommerce.order.model.OrderDetails;
import com.tomtom.ecommerce.order.model.Product;
import com.tomtom.ecommerce.order.model.ProductOrder;
import com.tomtom.ecommerce.order.model.ProductQuantityCart;
import com.tomtom.ecommerce.order.model.ResponseStatus;
import com.tomtom.ecommerce.order.repository.OrderDataAccessRepository;

/**
 * @author Gurpreet Hora
 *
 */
@Service
@Transactional
public class ECommerceOrderServiceImpl implements ECommerceOrderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ECommerceOrderServiceImpl.class);

	@Value("${ecommerce.product.api.name}")
	private String ecommerceProductApiName;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${ecommerce.cart.api.name}")
	private String ecommerceCartApiName;
	
	private final OrderDataAccessRepository orderDataAccessRepository;
	
	@Autowired
	public ECommerceOrderServiceImpl(OrderDataAccessRepository orderDataAccessRepository) {
		super();
		this.orderDataAccessRepository = orderDataAccessRepository;
	}
	
	private String getProductApiURL(){
		return "http://" +ecommerceProductApiName +"/product/";
	}
	
	private String cartApiURL() {
		return "http://" +ecommerceCartApiName;
	}

	//Gets product details from ecommerce-product-api
	public Product getProduct(Integer productId) throws ProductNotFoundECommerceException  {
		LOGGER.debug("Trying to get product if{}" ,productId);
		Optional<ResponseStatus> responseStatus = Optional.ofNullable(restTemplate.getForObject(this.getProductApiURL() + "/product/"+productId, ResponseStatus.class));
		if(responseStatus.isPresent() && !responseStatus.get().getProducts().isEmpty() &&
				responseStatus.get().getProducts()!=null && responseStatus.get().getProducts().stream().findFirst().isPresent()){
			LOGGER.debug("Product found for product id {}" ,productId);
			return responseStatus.get().getProducts().stream().findFirst().get();
		} else {
			throw new ProductNotFoundECommerceException("Product not found for supplied productId : "+productId);
		}
	}

	public void saveProduct(Product product) throws PriceMisMatchECommerceException {
		ResponseEntity<ResponseStatus> responseStatus = restTemplate.postForEntity(this.getProductApiURL() + "/seller/product", product, ResponseStatus.class);
		if(!ECommerceConstants.SUCCESS.equals(responseStatus.getBody().getStatus())){
			throw new PriceMisMatchECommerceException(responseStatus.getBody().getMessages().get(0));
		}
	}
	
	public void addProduct(Product product) throws PriceMisMatchECommerceException, ProductNotFoundECommerceException {
		LOGGER.debug("Trying to add product {}" ,product.getProductName());
		Optional<Product> existingProduct =  Optional.ofNullable(getProduct(product.getProductId()));
		if (existingProduct.isPresent()) {
			if(product.getProductPrice()!=null && existingProduct.get().getProductPrice().compareTo(product.getProductPrice()) !=0) {
				throw new PriceMisMatchECommerceException("Existing price for same product is "+existingProduct.get().getProductPrice()+" "
						+ "and new price supplied is "+product.getProductPrice()+", both should be same !");
			}
			existingProduct.get().setProductQuantity(existingProduct.get().getProductQuantity() + product.getProductQuantity());
			saveProduct(existingProduct.get());
		} else {
			saveProduct(product);
		}
		
		LOGGER.debug("Transaction successful to add product {}" ,product.getProductName());
	}

	//Gets User Cart details from ecommerce-cart-api
		public OrderDetails getUserCart(String userId) throws ECommerceCartException  {
			LOGGER.debug("Trying to get userCart for user {}" ,userId);
			Optional<ResponseStatus> responseStatus = Optional.ofNullable(restTemplate.getForObject(cartApiURL()+"/user/" +userId+ "/cart/", ResponseStatus.class));
			if(responseStatus.isPresent() && responseStatus.get().getOrderDetails()!=null){
				LOGGER.debug("User Cart found for user id {}" ,userId);
				return  responseStatus.get().getOrderDetails();
			} else {
				throw new ECommerceCartException(responseStatus.get().getMessages().get(0));
			}
		}
	
	/**
	 * @param cartDetails
	 * @return
	 * @throws ProductNotFoundECommerceException
	 * @throws EmptyCartECommerceException
	 * This method is responsible for calculating overall amounts from cart.
	 * It calculates amount at product level as well at order level. 
	 * @throws ECommerceCartException 
	 */
	public OrderDetails calculateCartValue(CartDetails cartDetails) throws ProductNotFoundECommerceException, EmptyCartECommerceException, ECommerceCartException {
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
	 * @throws ECommerceCartException 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public OrderDetails placeOrder(CartDetails cartDetails) throws ProductNotFoundECommerceException, PriceMisMatchECommerceException, EmptyCartECommerceException, ECommerceCartException {
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
		return this.orderDataAccessRepository.findById(userId).
				orElseThrow(() -> new NoOrdersFoundECommerceException("No Orders made by user : "+userId));
	}

	
	public void deleteUserCart(String userId) {
		 restTemplate.delete(cartApiURL()+"/user/cart/"+userId);
	}
}
