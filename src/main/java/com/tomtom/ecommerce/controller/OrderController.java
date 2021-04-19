package com.tomtom.ecommerce.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomtom.ecommerce.builder.ECommerceResponseBuilder;
import com.tomtom.ecommerce.constants.ECommerceConstants;
import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.ResponseStatus;
import com.tomtom.ecommerce.service.ECommerceOrderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "ECommerce Order API")
@RestController
@RequestMapping(value = "/user")
public class OrderController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	ECommerceOrderService eCommerceService;
	
	
	@ApiOperation( value = "Place order of products in cart")
	@ApiResponses(value = {
			@ApiResponse(response = List.class, message = "Success", code = 201)})
	@PostMapping("{userId}/order")
	public ResponseEntity<ResponseStatus> placeOrder (@PathVariable @NotBlank @Size(min = 1, max = 50) String userId,
			@RequestBody @Valid CartDetails cartDetails){
		OrderDetails orderDetails = new OrderDetails();
		try {
			cartDetails.setUserId(userId);
			orderDetails.setAddress(cartDetails.getAddress());
			orderDetails.setPaymentMode(cartDetails.getPaymentMode());
			orderDetails = eCommerceService.placeOrder(cartDetails);
			orderDetails.setUserId(userId);
			orderDetails.setStatus("Order Placed");
		} catch (Exception e) {
			LOGGER.warn("Exception occured while placing order : ", e);
			return ECommerceResponseBuilder.buildResponse(ECommerceConstants.FAILURE,HttpStatus.OK, e.getMessage());
		}
		return ECommerceResponseBuilder.buildResponse(ECommerceConstants.SUCCESS, HttpStatus.CREATED, orderDetails);
	}
	
	@ApiOperation( value = "Gets order details")
	@ApiResponses(value = {
			@ApiResponse(response = OrderDetails.class, message = ECommerceConstants.SUCCESS, code = 200)})
	@GetMapping (value = "orders/{orderId}")
	public ResponseEntity<ResponseStatus> getUserOrders (@PathVariable @NotBlank @Size(min = 1, max = 50) String orderId){
		OrderDetails orderDetails;
		try {
			orderDetails =  eCommerceService.getUserOrders(orderId);
			orderDetails.setStatus("Order Placed");
		} catch (Exception e) {
			LOGGER.warn("Exception occured while getting user order : ", e);
			return ECommerceResponseBuilder.buildResponse(ECommerceConstants.FAILURE,HttpStatus.OK , e.getMessage());
		}
		return ECommerceResponseBuilder.buildResponse(ECommerceConstants.SUCCESS, HttpStatus.OK ,  orderDetails);
	}
	
}
