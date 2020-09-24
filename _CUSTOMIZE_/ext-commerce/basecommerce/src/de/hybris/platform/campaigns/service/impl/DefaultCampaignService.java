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
package de.hybris.platform.campaigns.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.campaigns.dao.CampaignDao;
import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.campaigns.service.CampaignService;
import de.hybris.platform.servicelayer.time.TimeService;


/**
 * Default implementation of {@link CampaignService}
 */
public class DefaultCampaignService implements CampaignService
{
	private CampaignDao campaignDao;
	private TimeService timeService;

	@Override
	public List<CampaignModel> getAllCampaigns()
	{
		return getCampaignDao().findAllCampaigns();
	}

	@Override
	public List<CampaignModel> getActiveCampaigns()
	{
		return getCampaignDao().findActiveCampaigns(getTimeService().getCurrentTime());
	}

	@Override
	public CampaignModel getCampaignByCode(final String code)
	{
		validateParameterNotNullStandardMessage(CampaignModel.CODE, code);
		return getCampaignDao().findCampaignByCode(code);
	}

	protected CampaignDao getCampaignDao()
	{
		return campaignDao;
	}

	@Required
	public void setCampaignDao(final CampaignDao campaignDao)
	{
		this.campaignDao = campaignDao;
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
