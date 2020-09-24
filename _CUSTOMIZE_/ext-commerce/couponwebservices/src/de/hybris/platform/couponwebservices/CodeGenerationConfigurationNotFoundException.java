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
package de.hybris.platform.couponwebservices;

import de.hybris.platform.webservicescommons.errors.exceptions.NotFoundException;


/**
 * Exception to be thrown in the case the coupon code generation configuration cannot be retrieved
 */
public class CodeGenerationConfigurationNotFoundException extends NotFoundException
{
	private static final String TYPE = "CodeGenerationConfigurationNotFoundError";

	public CodeGenerationConfigurationNotFoundException(final String message, final String reason, final String subject,
			final Throwable cause)
	{
		super(message, reason, subject, cause);
	}

	public CodeGenerationConfigurationNotFoundException(final String message, final String reason, final String subject)
	{
		super(message, reason, subject);
	}

	public CodeGenerationConfigurationNotFoundException(final String message, final String reason, final Throwable cause)
	{
		super(message, reason, cause);
	}

	public CodeGenerationConfigurationNotFoundException(final String message, final String reason)
	{
		super(message, reason);
	}


	public CodeGenerationConfigurationNotFoundException(final String message)
	{
		super(message);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
