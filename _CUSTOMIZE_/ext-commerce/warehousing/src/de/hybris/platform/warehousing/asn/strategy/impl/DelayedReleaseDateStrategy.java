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
package de.hybris.platform.warehousing.asn.strategy.impl;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.warehousing.asn.strategy.AsnReleaseDateStrategy;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeEntryModel;

import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Strategy to calculate delayed release date for Stock Level based on Advanced Shipping Notice release date and
 * configuration:<br>
 * release date from ASN + delay days according to a property {@value #DELAY_DAYS}
 */
public class DelayedReleaseDateStrategy implements AsnReleaseDateStrategy
{
	public static final String DELAY_DAYS = "warehousing.asn.delay.days";
	private static final Logger LOGGER = LoggerFactory.getLogger(DelayedReleaseDateStrategy.class);

	private ConfigurationService configurationService;

	@Override
	public Date getReleaseDateForStockLevel(final AdvancedShippingNoticeEntryModel asnEntry)
	{
		validateParameterNotNullStandardMessage("asnEntry", asnEntry);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(asnEntry.getAsn().getReleaseDate());
		try
		{
			final int delayDays = getConfigurationService().getConfiguration().getInt(DELAY_DAYS);
			cal.add(Calendar.DATE, +delayDays);
			return cal.getTime();
		}
		catch (final NoSuchElementException | NumberFormatException e) //NOSONAR
		{
			LOGGER.warn("Property {} is missing or not an integer. Using 0 day as default", DELAY_DAYS);
			cal.add(Calendar.DATE, 0);
			return cal.getTime();
		}
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

}
