package com.tomtom.ecommerce.order.builder;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tomtom.ecommerce.order.model.OrderDetails;
import com.tomtom.ecommerce.order.model.Product;
import com.tomtom.ecommerce.order.model.ResponseStatus;

public final class ECommerceResponseBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ECommerceResponseBuilder.class);

	private ECommerceResponseBuilder() {

	}

	public static ResponseEntity<ResponseStatus> buildResponse(String status, HttpStatus httpStatus, Object... objects) {
		com.tomtom.ecommerce.order.model.ResponseStatus responseStatus = createResponse(status);
		if(objects != null) {
			for(Object object : objects) {

				if(object instanceof List) {
					List<Product> lstproducts = (List<Product>) object;
					responseStatus.setProducts(lstproducts);
					if(lstproducts.isEmpty()) {
						responseStatus.setMessages(Arrays.asList(("No Product found")));
					}
				} else  if(object instanceof OrderDetails) {
						OrderDetails orderDetails = (OrderDetails) object;
						responseStatus.setOrderDetails(orderDetails);
				}  else if(object instanceof String) {
					LOGGER.debug("Possible exception {}", object);
					responseStatus.setMessages(Arrays.asList(((String) object))); 
				}
			}
		}
		return ResponseEntity.status(httpStatus).body(responseStatus);
	}
	public static ResponseStatus createResponse(String status) {
		ResponseStatus responseStatus = new ResponseStatus();
		responseStatus.setStatus(status);
		return responseStatus;
	}

}
