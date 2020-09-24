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
package de.hybris.platform.solrfacetsearch.reporting;

public class ReportingRuntimeException extends RuntimeException
{
	public ReportingRuntimeException()
	{
		super();
	}

	public ReportingRuntimeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ReportingRuntimeException(final String message)
	{
		super(message);
	}

	public ReportingRuntimeException(final Throwable cause)
	{
		super(cause);
	}
}
