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
package de.hybris.platform.promotions.result;

import de.hybris.platform.jalo.JaloSystemException;


/**
 * PromotionEvaluationContext.
 * 
 * 
 */
public class PromotionException extends JaloSystemException
{
	public PromotionException(final String message)
	{
		super(message);
	}

	public PromotionException(final String message, final int errorCode)
	{
		super(message, errorCode);
	}

	public PromotionException(final Throwable nested)
	{
		super(nested);
	}

	public PromotionException(final Throwable nested, final int errorCode)
	{
		super(nested, errorCode);
	}

	public PromotionException(final Throwable nested, final String message, final int errorCode)
	{
		super(nested, message, errorCode);
	}
}
