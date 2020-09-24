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
 *
 */

package de.hybris.platform.warehousing.returns;

public class RestockException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public RestockException(final String message)
	{
		super(message);
	}
	
	/**
	 * @param exception
	 */
	public RestockException(final Exception exception)
	{
		super(exception);
	}
	
}
