package com.tomtom.ecommerce.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Test;
import org.springframework.validation.Errors;

import com.tomtom.ecommerce.model.CartDetails;
import com.tomtom.ecommerce.model.Product;
import com.tomtom.ecommerce.model.ProductQuantityCart;

public class ECommerceValidatorTest {

	Errors errors = mock(Errors.class);
	ECommerceValidator eCommerceValidator = new ECommerceValidator();
	
	private final String EXPECTED_POSITIVE_NUMERIC= " expected positive numeric value";
	private final String IS_MANDATORY = " is mandatory";
	private final String EXPECTED_ALPA_NUMERIC = " expected alpha-numeric value";
	
	@Test
	public void productMandatoryValidatortest() {
		eCommerceValidator.validate(new Product(), errors);
		verify(errors, times(1)).rejectValue("ProductPrice", "" , IS_MANDATORY);
		verify(errors, times(1)).rejectValue("ProductName", "" , IS_MANDATORY);
		verify(errors, times(1)).rejectValue("ProductQuantity", "" , IS_MANDATORY);
		verify(errors, times(1)).rejectValue("ProductId", "" , IS_MANDATORY);
		reset(errors);
	}
	
	@Test
	public void product_inValidInputs_Validatortest() {
		Errors errors = mock(Errors.class);
		Product product = new Product();
		product.setProductId(-1);
		product.setProductPrice(new BigDecimal(-1));
		product.setProductQuantity(-1);
		product.setProductName("%");
		eCommerceValidator.validate(product, errors);
		verify(errors, times(1)).rejectValue("ProductPrice", "" , EXPECTED_POSITIVE_NUMERIC);
		verify(errors, times(1)).rejectValue("ProductQuantity", "" , EXPECTED_POSITIVE_NUMERIC);
		verify(errors, times(1)).rejectValue("ProductId", "" , EXPECTED_POSITIVE_NUMERIC);
		verify(errors, times(1)).rejectValue("ProductName", "" , EXPECTED_ALPA_NUMERIC);
		reset(errors);
	}
	
	@Test
	public void productQuantityCart_MandatoryValidatortest() {
		eCommerceValidator.validate(new ProductQuantityCart(), errors);
		verify(errors, times(1)).rejectValue("ProductQuantity", "" , IS_MANDATORY);
		verify(errors, times(1)).rejectValue("ProductId", "" , IS_MANDATORY);
		reset(errors);
	}
	
	@Test
	public void productQuantityCart_inValidInputs_Validatortest() {
		ProductQuantityCart productQuantityCart = new ProductQuantityCart();
		productQuantityCart.setProductId(-1);
		productQuantityCart.setProductQuantity(0);
		eCommerceValidator.validate(productQuantityCart, errors);
		verify(errors, times(1)).rejectValue("ProductQuantity", "" , " can be positive or negative only");
		verify(errors, times(1)).rejectValue("ProductId", "" ,EXPECTED_POSITIVE_NUMERIC);
		reset(errors);
	}
	
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
		assertTrue(eCommerceValidator.supports(Product.class));
		assertTrue(eCommerceValidator.supports(ProductQuantityCart.class));
		assertTrue(eCommerceValidator.supports(CartDetails.class));
	}
}
