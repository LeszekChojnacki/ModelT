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
package de.hybris.platform.warehousing.shipping.strategy.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.shipping.strategy.DeliveryTrackingIdStrategy;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Default implementation for {@link DeliveryTrackingIdStrategy}
 */
public class DefaultDeliveryTrackingIdStrategy implements DeliveryTrackingIdStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDeliveryTrackingIdStrategy.class);

	@Override
	public String generateTrackingId(final ConsignmentModel consignment)
	{
		validateParameterNotNull(consignment, "Consignment cannot be null");

		final Random random = new Random();
		final long trackingId = random.nextLong();
		LOGGER.info("Tracking ID generated ==> {}", trackingId);
		return Long.toString(trackingId > 0 ? trackingId : trackingId * -1);
	}
}
