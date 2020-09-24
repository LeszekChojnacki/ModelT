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
package de.hybris.platform.warehousing.returns.service.impl;

import de.hybris.platform.warehousing.model.RestockConfigModel;
import de.hybris.platform.warehousing.returns.RestockException;
import de.hybris.platform.warehousing.returns.dao.RestockConfigDao;
import de.hybris.platform.warehousing.returns.service.RestockConfigService;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default OMS implementation of RestockConfigService.
 */
public class DefaultRestockConfigService implements RestockConfigService
{
	private static final Logger logger = LoggerFactory.getLogger(RestockConfigService.class);
	
	private RestockConfigDao restockConfigDao;
	
	@Override
	public RestockConfigModel getRestockConfig() throws RestockException
	{
		return getRestockConfigDao().getRestockConfig();
	}
	
	public String getReturnedBinCode()
	{
		String returnedBinCode = "";
		try
		{
			final RestockConfigModel restockConfigModel = getRestockConfig();
			if (!Objects.isNull(restockConfigModel) && !Objects.isNull(restockConfigModel.getReturnedBinCode()))
			{
				returnedBinCode = getRestockConfig().getReturnedBinCode();
			}
		}
		catch (RestockException e)
		{
			logger.error(e.getMessage(), e);
		}
		return returnedBinCode;
	}

	protected RestockConfigDao getRestockConfigDao()
	{
		return restockConfigDao;
	}

	@Required
	public void setRestockConfigDao (RestockConfigDao restockConfigDao)
	{
		this.restockConfigDao = restockConfigDao;
	}
}
