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
package de.hybris.platform.payment.jalo;

import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.DefaultCloneOrderStrategy;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.OrderManager;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.user.Address;
import de.hybris.platform.payment.constants.PaymentConstants;


/**
 *
 */
public class PaymentCloneOrderStrategy extends DefaultCloneOrderStrategy
{
	@Override
	public Order clone(final ComposedType orderType, final ComposedType entryType, final AbstractOrder originalOrder,
			final OrderManager orderManager)
	{
		final Order clonedOrder = super.clone(orderType, entryType, originalOrder, orderManager);
		if (clonedOrder.getPaymentInfo() != null)
		{
			@SuppressWarnings("deprecation")
			final Address clonedBillingAddress = (Address) clonedOrder.getPaymentInfo().getProperty(
					PaymentConstants.Attributes.PaymentInfo.BILLINGADDRESS);
			@SuppressWarnings("deprecation")
			final Address originalBillingAddress = (Address) originalOrder.getPaymentInfo().getProperty(
					PaymentConstants.Attributes.PaymentInfo.BILLINGADDRESS);
			if (clonedBillingAddress != null)
			{
				clonedBillingAddress.setDuplicate(Boolean.TRUE);
				clonedBillingAddress.setOriginal(originalBillingAddress);
				try
				{
					clonedBillingAddress.setOwner(clonedOrder.getPaymentInfo());
				}
				catch (final ConsistencyCheckException e)
				{
					throw new JaloSystemException(e);
				}
			}
		}
		return clonedOrder;
	}
}
