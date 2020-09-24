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
package de.hybris.platform.warehousing.sourcing.ban.service.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.warehousing.model.SourcingBanModel;
import de.hybris.platform.warehousing.sourcing.ban.dao.SourcingBanDao;
import de.hybris.platform.warehousing.sourcing.ban.service.SourcingBanService;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * The default implementation of {@link de.hybris.platform.warehousing.sourcing.ban.service.SourcingBanService}
 */

public class DefaultSourcingBanService implements SourcingBanService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSourcingBanService.class);

	protected static final String BAN_DAYS = "warehousing.ban.toobusy.days";

	private SourcingBanDao sourcingBanDao;
	private ModelService modelService;
	private ConfigurationService configurationService;
	private TimeService timeService;

	@Override
	public SourcingBanModel createSourcingBan(final WarehouseModel warehouse)
	{
		validateParameterNotNullStandardMessage("warehouse", warehouse);

		LOGGER.debug("Creating SourcingBan for Warehouse: {}", warehouse.getCode());

		SourcingBanModel sourcingBan = getModelService().create(SourcingBanModel.class);
		sourcingBan.setWarehouse(warehouse);
		getModelService().save(sourcingBan);
		return sourcingBan;
	}

	@Override
	public Collection<SourcingBanModel> getSourcingBan(final Collection<WarehouseModel> warehouses)
	{
		LOGGER.debug("Getting SourcingBans for: {} Warehouses", warehouses.size());
		// get the sourcing ban for default period of 1 day
		return warehouses.isEmpty() ?
				java.util.Collections.EMPTY_LIST :
				getSourcingBanDao().getSourcingBan(warehouses, getCurrentDateMinusBannedDays());
	}


	/**
	 * Calculates the current date -  # of ban days according to a property.
	 */
	protected Date getCurrentDateMinusBannedDays()
	{
		final Calendar cal = Calendar.getInstance();
		cal.setTime(getTimeService().getCurrentTime());

		try
		{
			final int banDays = getConfigurationService().getConfiguration().getInt(BAN_DAYS);

			cal.add(Calendar.DATE, -banDays);
			return cal.getTime();

		}
		catch (final NumberFormatException e)
		{
			LOGGER.warn("Property {} is missing or not an integer. Using 1 day as default", BAN_DAYS);
			cal.add(Calendar.DATE, -1);
			return cal.getTime();
		}
	}

	protected SourcingBanDao getSourcingBanDao()
	{
		return sourcingBanDao;
	}

	@Required
	public void setSourcingBanDao(SourcingBanDao sourcingBanDao)
	{
		this.sourcingBanDao = sourcingBanDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService)
	{
		this.modelService = modelService;
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
