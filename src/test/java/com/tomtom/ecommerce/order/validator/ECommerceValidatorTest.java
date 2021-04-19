package com.tomtom.ecommerce.order.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.validation.Errors;

import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.validator.ECommerceValidator;

public class ECommerceValidatorTest {

	Errors errors = mock(Errors.class);
	ECommerceValidator eCommerceValidator = new ECommerceValidator();
	
	private final String IS_MANDATORY = " is mandatory";
	private final String EXPECTED_ALPA_NUMERIC = " expected alpha-numeric value";
	
	@Test
	public void cartDetails_MandatoryValidatortest() {
		eCommerceValidator.validate(new CartDetails(), errors);
		verify(errors, times(1)).rejectValue("Address", "" , IS_MANDATORY);
		verify(errors, times(1)).rejectValue("PaymentMode", "" , IS_MANDATORY);
		reset(errors);
	}
	
	@Test
	public void cartDetails_inValidInputs_Validatortest() {
		CartDetails cartDetails = new CartDetails();
		cartDetails.setAddress("%");
		eCommerceValidator.validate(cartDetails, errors);
		verify(errors, times(1)).rejectValue("Address", "" , EXPECTED_ALPA_NUMERIC);
		reset(errors);
	}

	@Test
	public void testSupports() {
		ECommerceValidator eCommerceValidator = new ECommerceValidator();
		assertTrue(eCommerceValidator.supports(CartDetails.class));
	}
}
