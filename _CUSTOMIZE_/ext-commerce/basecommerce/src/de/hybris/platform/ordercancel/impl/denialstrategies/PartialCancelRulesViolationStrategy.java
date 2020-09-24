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

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * This strategy forbids cancel when rules regarding partial cancel are violated. These rules comes from configuration,
 * that may prohibit partial cancel at all or may allow partial cancel but prohibit partial entry cancel (decreasing
 * OrderEntry quantity)
 */
public class PartialCancelRulesViolationStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy
{
	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requester, final boolean partialCancel, final boolean partialEntryCancel)
	{
		validateParameterNotNull(configuration, "Parameter configuration must not be null");
		final boolean partialCancelViolation;

		if (partialCancel)
		{
			if (configuration.isPartialCancelAllowed())
			{
				if (partialEntryCancel)
				{
					/*
					 * It is a partial entry cancel request. Violation depends on whether configuration denies partial entry
					 * cancel.
					 */
					partialCancelViolation = !configuration.isPartialOrderEntryCancelAllowed();
				}
				else
				{
					/*
					 * It is not partial entry cancel request => it is just partial cancel. Configuration allows partial
					 * cancels, so no violation here.
					 */
					partialCancelViolation = false;
				}
			}
			else
			{
				/*
				 * It is a partial cancel request, and configuration prohibits partial cancels. Violation.
				 */
				partialCancelViolation = true;
			}
		}
		else
		{
			/*
			 * This is NOT partial cancel request. No violation here.
			 */
			partialCancelViolation = false;
		}

		if (partialCancelViolation)
		{
			return getReason();
		}
		else
		{
			return null;
		}
	}
}
