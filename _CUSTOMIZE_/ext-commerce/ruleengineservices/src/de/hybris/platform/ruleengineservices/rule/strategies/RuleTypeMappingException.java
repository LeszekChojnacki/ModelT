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
package de.hybris.platform.ruleengineservices.rule.strategies;

import de.hybris.platform.ruleengineservices.RuleEngineServiceRuntimeException;


public class RuleTypeMappingException extends RuleEngineServiceRuntimeException
{
	public RuleTypeMappingException()
	{
		super();
	}

	public RuleTypeMappingException(final String message)
	{
		super(message);
	}

	public RuleTypeMappingException(final Throwable cause)
	{
		super(cause);
	}

	public RuleTypeMappingException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
