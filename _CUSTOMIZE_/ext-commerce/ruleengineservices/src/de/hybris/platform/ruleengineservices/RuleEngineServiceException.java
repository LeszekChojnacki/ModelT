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
package de.hybris.platform.ruleengineservices;


public class RuleEngineServiceException extends RuntimeException
{
	public RuleEngineServiceException()
	{
		super();
	}

	public RuleEngineServiceException(final String message)
	{
		super(message);
	}

	public RuleEngineServiceException(final Throwable cause)
	{
		super(cause);
	}

	public RuleEngineServiceException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
