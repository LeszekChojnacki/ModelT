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
package de.hybris.platform.ruleengine.exception;

/**
 * RuleEngineRuntimeException thrown when errors are encountered during rule execution or the rule engine session is
 * prematurely ended or while rule engine initialization.
 *
 */
public class RuleEngineRuntimeException extends RuntimeException
{
	public RuleEngineRuntimeException()
	{
		super();
	}

	public RuleEngineRuntimeException(final Throwable t)
	{
		super(t);
	}

	public RuleEngineRuntimeException(final String s, final Throwable t)
	{
		super(s, t);
	}

	public RuleEngineRuntimeException(final String s)
	{
		super(s);
	}

}
