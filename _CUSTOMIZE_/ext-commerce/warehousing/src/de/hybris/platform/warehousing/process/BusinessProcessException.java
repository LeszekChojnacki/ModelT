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
 */
package de.hybris.platform.warehousing.process;

/**
 * Exception thrown when logical errors occur when working with the process engine.
 */
public class BusinessProcessException extends RuntimeException
{
	private static final long serialVersionUID = 4397866426091264312L;

	public BusinessProcessException(final String message)
	{
		super(message);
	}

	public BusinessProcessException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
