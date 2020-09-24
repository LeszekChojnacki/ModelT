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

import static java.util.regex.Pattern.compile;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;



/**
 * JSR-303 implementation of pattern-based ConstraintValidator
 */
public class ObjectPatternValidator implements ConstraintValidator<ObjectPattern, Object>
{
	private java.util.regex.Pattern pattern;

	@Override
	public void initialize(final ObjectPattern parameters)
	{
		final Pattern.Flag[] flags = parameters.flags();

		int intFlag = 0;

		for (final Pattern.Flag flag : flags)
		{
			intFlag |= flag.getValue();
		}

		try
		{
			pattern = compile(parameters.regexp(), intFlag);
		}
		catch (final PatternSyntaxException e)
		{
			final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid regular expression.", e);
			final StackTraceElement[] st = illegalArgumentException.getStackTrace();
			illegalArgumentException.setStackTrace(Arrays.copyOfRange(st, 1, st.length));
			throw illegalArgumentException;
		}
	}

	@Override
	public boolean isValid(final Object value, final ConstraintValidatorContext constraintValidatorContext)
	{
		if (value == null)
		{
			return true;
		}

		final Matcher m = pattern.matcher(value.toString());
		return m.matches();
	}
}
