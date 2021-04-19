package com.tomtom.ecommerce.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.PaymentMode;

@Configurable
public class ECommerceValidator implements Validator {

	private static final String IS_MANDATORY = " is mandatory";
	private static final String EXPECTED_ALPHA_NUMERIC = " expected alpha-numeric value";
	private static final String ADDRESS = "Address";
	
	@Override
	public boolean supports(Class<?> clazz) {

		return CartDetails.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target.getClass().equals(CartDetails.class)) {
			validateCartDetails(target, errors);
		}
	}

	private void validateCartDetails(Object target, Errors errors) {
		CartDetails cartDetails = (CartDetails) target;
		if(StringUtils.isEmpty(cartDetails.getAddress())) {
			errors.rejectValue(ADDRESS, "" , IS_MANDATORY);
		}
		else if(!StringUtils.isEmpty(cartDetails.getAddress()) && cartDetails.getAddress().length()>50) {
			errors.rejectValue(ADDRESS, "", " can maxinum be 50 Characters");
		} else if(!StringUtils.isEmpty(cartDetails.getAddress()) && !StringUtils.isAlphanumeric(cartDetails.getAddress())) {
			errors.rejectValue(ADDRESS, "", EXPECTED_ALPHA_NUMERIC);
		}
		if(null==cartDetails.getPaymentMode()) {
			errors.rejectValue("PaymentMode", "" , IS_MANDATORY);
		}else if(null!=cartDetails.getPaymentMode() && 
				(cartDetails.getPaymentMode().equals(PaymentMode.CARD)
						|| cartDetails.getPaymentMode().equals(PaymentMode.CASH))) {
			errors.rejectValue("PaymentMode", "", " Valid options are CASH and CARD");
		}
	}

}
