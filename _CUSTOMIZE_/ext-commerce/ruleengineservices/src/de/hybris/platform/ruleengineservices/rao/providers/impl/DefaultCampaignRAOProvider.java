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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.campaigns.service.CampaignService;
import de.hybris.platform.ruleengineservices.rao.CampaignRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


public class DefaultCampaignRAOProvider implements RAOProvider
{

	private CampaignService campaignService;
	private Converter<CampaignModel, CampaignRAO> campaignRaoConverter;

	@Override
	public Set<?> expandFactModel(final Object modelFact)
	{
		return getCampaignService().getActiveCampaigns().stream().map(cm -> getCampaignRaoConverter().convert(cm))
				.collect(Collectors.toSet());
	}

	protected CampaignService getCampaignService()
	{
		return campaignService;
	}

	@Required
	public void setCampaignService(final CampaignService campaignService)
	{
		this.campaignService = campaignService;
	}

	protected Converter<CampaignModel, CampaignRAO> getCampaignRaoConverter()
	{
		return campaignRaoConverter;
	}

	@Required
	public void setCampaignRaoConverter(final Converter<CampaignModel, CampaignRAO> campaignRaoConverter)
	{
		this.campaignRaoConverter = campaignRaoConverter;
	}

}
