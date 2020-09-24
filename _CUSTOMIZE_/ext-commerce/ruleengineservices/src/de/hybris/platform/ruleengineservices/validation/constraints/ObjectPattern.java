/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ruleengineservices.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern.Flag;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Target(
{ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy =
{ ObjectPatternValidator.class })
public @interface ObjectPattern
{
	String regexp() default "";

	Flag[] flags() default Flag.UNICODE_CASE;

	String message() default "{de.hybris.platform.ruleengineservices.de.hybris.platform.ruleengineservices.validation.constraints.ObjectPattern.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
