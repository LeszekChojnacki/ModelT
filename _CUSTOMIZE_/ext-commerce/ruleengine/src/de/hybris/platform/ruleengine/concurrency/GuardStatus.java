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
package de.hybris.platform.ruleengine.concurrency;

/**
 * The class keeping the status of the {@link GuardedSuspension#checkPreconditions(Object)} check
 */
public class GuardStatus
{
	public enum Type
	{
		GO, NO_GO
	}

	private Type type;
	private String message;

	private GuardStatus(final Type type)
	{
		this.type = type;
	}

	public static GuardStatus of(final Type type)
	{
		return new GuardStatus(type);
	}

	public Type getType()
	{
		return type;
	}

	public void setType(final Type type)
	{
		this.type = type;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(final String message)
	{
		this.message = message;
	}
}
