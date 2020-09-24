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
package de.hybris.platform.warehousing.allocation.strategy.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.warehousing.allocation.strategy.ShippingDateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.Date;


/**
 * Default implementation of {@link ShippingDateStrategy}.
 * It sets the next day as consignment's shipping date
 */
public class NextDayShippingDateStrategy implements ShippingDateStrategy
{

	private TimeService timeService;

	private static final Logger LOGGER = LoggerFactory.getLogger(NextDayShippingDateStrategy.class);

	/**
	 * Sets next day as the consignment's shipping date. Used as an estimated shipping date
	 */
	@Override
	public Date getExpectedShippingDate(final ConsignmentModel consignment)
	{
		ServicesUtil.validateParameterNotNull(consignment, "Consignment cannot be null");

		final Calendar cal = Calendar.getInstance();
		cal.setTime(getTimeService().getCurrentTime());
		cal.add(Calendar.DATE, 1);

		LOGGER.debug("Adding 1 day as default delay in consignment's shipping");
		return cal.getTime();
	}


	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

}
