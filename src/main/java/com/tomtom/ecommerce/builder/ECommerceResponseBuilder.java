package com.tomtom.ecommerce.builder;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tomtom.ecommerce.model.OrderDetails;
import com.tomtom.ecommerce.model.Product;
import com.tomtom.ecommerce.model.ResponseStatus;

public final class ECommerceResponseBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ECommerceResponseBuilder.class);

	private ECommerceResponseBuilder() {

	}

	public static ResponseEntity<ResponseStatus> buildResponse(String status, HttpStatus httpStatus, Object... objects) {
		com.tomtom.ecommerce.model.ResponseStatus responseStatus = createResponse(status);
		if(objects != null) {
			for(Object object : objects) {

				if(object instanceof List) {
					List<Product> lstproducts = (List<Product>) object;
					responseStatus.setProducts(lstproducts);
					if(lstproducts.isEmpty() || lstproducts.size()==0) {
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
