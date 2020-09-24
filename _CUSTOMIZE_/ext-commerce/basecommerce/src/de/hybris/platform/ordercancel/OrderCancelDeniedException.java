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
package de.hybris.platform.ordercancel;

/**
 * Exception thrown when
 * {@link OrderCancelService#requestOrderCancel(OrderCancelRequest, de.hybris.platform.core.model.security.PrincipalModel)}
 * method is invoked and Cancel is denied for given conditions. Cancel denial decisions can be read out using
 * {@link #getCancelDecision()} method.
 */
public class OrderCancelDeniedException extends OrderCancelException
{
	private final CancelDecision cancelDecision;

	public OrderCancelDeniedException(final String orderCode, final CancelDecision cancelDecision)
	{
		super(orderCode);
		this.cancelDecision = cancelDecision;
	}

	/**
	 * @return the cancelDecision
	 */
	public CancelDecision getCancelDecision()
	{
		return cancelDecision;
	}
}
