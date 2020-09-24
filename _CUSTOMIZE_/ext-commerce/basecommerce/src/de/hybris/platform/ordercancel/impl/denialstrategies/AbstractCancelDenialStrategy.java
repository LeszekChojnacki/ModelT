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
package de.hybris.platform.ordercancel.impl.denialstrategies;

import de.hybris.platform.ordercancel.OrderCancelDenialReason;


/**
 */
public class AbstractCancelDenialStrategy
{
	private OrderCancelDenialReason reason;

	/**
	 * @return the reason
	 */
	public OrderCancelDenialReason getReason()
	{
		return reason;
	}

	/**
	 * @param reason
	 *           the reason to set
	 */
	public void setReason(final OrderCancelDenialReason reason)
	{
		this.reason = reason;
	}
}
