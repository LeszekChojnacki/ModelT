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
package de.hybris.platform.commercewebservicescommons.errors.exceptions;

import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException;


/**
 * Thrown when request parameter is wrong.
 */
public class RequestParameterException extends WebserviceException
{
	public static final String INVALID = "invalid";
	public static final String MISSING = "missing";
	public static final String UNKNOWN_IDENTIFIER = "unknownIdentifier";
	private static final String TYPE = "ValidationError";
	private static final String SUBJECT_TYPE = "parameter";

	public RequestParameterException(final String message)
	{
		super(message);
	}

	public RequestParameterException(final String message, final String reason)
	{
		super(message, reason);
	}

	public RequestParameterException(final String message, final String reason, final Throwable cause)
	{
		super(message, reason, cause);
	}

	public RequestParameterException(final String message, final String reason, final String subject)
	{
		super(message, reason, subject);
	}

	public RequestParameterException(final String message, final String reason, final String subject, final Throwable cause)
	{
		super(message, reason, subject, cause);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getSubjectType()
	{
		return SUBJECT_TYPE;
	}
}
