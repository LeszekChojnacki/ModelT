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
package de.hybris.platform.basecommerce.jalo;

import de.hybris.platform.jalo.order.delivery.DeliveryCostsStrategy;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.delivery.JaloDeliveryModeException;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.util.PriceValue;

import org.apache.log4j.Logger;


/**
 * This implementation ignores the configured {@link DeliveryCostsStrategy} and uses its own (assigned)
 * {@link DeliveryMode} only.
 */
public class MultiAddressInMemoryCart extends GeneratedMultiAddressInMemoryCart
{
	private static final Logger LOG = Logger.getLogger(MultiAddressInMemoryCart.class);

	@Override
	protected PriceValue findDeliveryCosts() throws JaloPriceFactoryException
	{
		final DeliveryMode deliveryMode = this.getDeliveryMode();

		if (deliveryMode != null)
		{
			try
			{
				return deliveryMode.getCost(this);
			}
			catch (final JaloDeliveryModeException e)
			{
				LOG.error("Delivery mode error for mode " + deliveryMode.getCode(), e);
			}
		}
		return null;
	}
}
