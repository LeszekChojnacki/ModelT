/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.validation.annotations;

import de.hybris.platform.warehousing.validation.validators.AdvancedShippingNoticeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { AdvancedShippingNoticeValidator.class })
@Documented
public @interface AdvancedShippingNoticeValid
{
	String message() default "{de.hybris.platform.warehousing.validation.annotations.AdvancedShippingNoticeValid.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
