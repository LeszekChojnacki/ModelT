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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * This strategy forbids cancel when Order shipment has already started and the configuration does not allow to cancel
 * such orders.
 */
public class ShippingDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy,
		StateMappingStrategyDependent
{
	private static final Logger LOG = Logger.getLogger(ShippingDenialStrategy.class.getName());
	private OrderCancelStateMappingStrategy stateMappingStrategy;
	private List<OrderCancelState> strategyInvolvedStates;

	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requester, final boolean partialCancel, final boolean partialEntryCancel)
	{
		validateParameterNotNull(configuration, "Parameter configuration must not be null");
		if (stateMappingStrategy == null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Missing OrderCancelStateMappingStrategy!");
			}
			return null;
		}
		final OrderCancelState currentOrderState = stateMappingStrategy.getOrderCancelState(order);
		if (strategyInvolvedStates.contains(currentOrderState))
		{
			final boolean decision;

			/*
			 * Order shipment has begun. Partial cancel requests will be handled if cancel is allowed after sending order
			 * to a warehouse for fulfillment. Complete Cancel requests, if allowed by configuration, will be satisfied
			 * only partially or at all.
			 */
			if (partialCancel)
			{
				//Partial Cancel
				decision = configuration.isCancelAfterWarehouseAllowed();
			}
			else
			{
				//Complete Cancel
				decision = configuration.isCancelAfterWarehouseAllowed()
						&& configuration.isCompleteCancelAfterShippingStartedAllowed();
			}

			if (decision)
			{
				return null;
			}
			else
			{
				return getReason();
			}
		}
		else
		{
			return null;
		}
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
	 * @return the strategyInvolvedStates
	 */
	public List<OrderCancelState> getStrategyInvolvedStates()
	{
		return strategyInvolvedStates;
	}

	/**
	 * @param strategyInvolvedStates
	 *           the strategyInvolvedStates to set
	 */
	public void setStrategyInvolvedStates(final List<OrderCancelState> strategyInvolvedStates)
	{
		this.strategyInvolvedStates = strategyInvolvedStates;
	}
}
