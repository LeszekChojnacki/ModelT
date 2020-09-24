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

import de.hybris.platform.basecommerce.enums.OrderCancelState;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.OrderCancelStateMappingStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;

import java.util.List;

import org.apache.log4j.Logger;


/**
 * This strategy forbids cancel when Order is in one of the provided OrderCancelStates.
 */
public class OrderStateDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy,
		StateMappingStrategyDependent
{
	private static final Logger LOG = Logger.getLogger(OrderStateDenialStrategy.class.getName());
	private OrderCancelStateMappingStrategy stateMappingStrategy;
	private List<OrderCancelState> partialCancelDeniedStates;
	private List<OrderCancelState> fullCancelDeniedStates;

	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requester, final boolean partialCancel, final boolean partialEntryCancel)
	{
		if (stateMappingStrategy == null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Missing OrderCancelStateMappingStrategy!");
			}
			return null;
		}
		final OrderCancelState currentOrderState = stateMappingStrategy.getOrderCancelState(order);
		if (partialCancel)
		{
			if (partialCancelDeniedStates.contains(currentOrderState))
			{
				return getReason();
			}
		}
		else
		{
			if (fullCancelDeniedStates.contains(currentOrderState))
			{
				return getReason();
			}
		}
		return null;
	}


	/**
	 * @return the stateMappingStrategy
	 */
	public OrderCancelStateMappingStrategy getStateMappingStrategy()
	{
		return stateMappingStrategy;
	}

	/**
	 * @param stateMappingStrategy
	 *           the stateMappingStrategy to set
	 */
	@Override
	public void setStateMappingStrategy(final OrderCancelStateMappingStrategy stateMappingStrategy)
	{
		this.stateMappingStrategy = stateMappingStrategy;
	}


	/**
	 * @return the partialCancelDeniedStates
	 */
	public List<OrderCancelState> getPartialCancelDeniedStates()
	{
		return partialCancelDeniedStates;
	}


	/**
	 * @param partialCancelDeniedStates
	 *           the partialCancelDeniedStates to set
	 */
	public void setPartialCancelDeniedStates(final List<OrderCancelState> partialCancelDeniedStates)
	{
		this.partialCancelDeniedStates = partialCancelDeniedStates;
	}


	/**
	 * @return the fullCancelDeniedStates
	 */
	public List<OrderCancelState> getFullCancelDeniedStates()
	{
		return fullCancelDeniedStates;
	}


	/**
	 * @param fullCancelDeniedStates
	 *           the fullCancelDeniedStates to set
	 */
	public void setFullCancelDeniedStates(final List<OrderCancelState> fullCancelDeniedStates)
	{
		this.fullCancelDeniedStates = fullCancelDeniedStates;
	}
}
