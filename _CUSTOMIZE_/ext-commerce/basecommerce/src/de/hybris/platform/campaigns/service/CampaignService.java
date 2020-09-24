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
package de.hybris.platform.campaigns.service;

import de.hybris.platform.campaigns.model.CampaignModel;

import java.util.List;


/**
 * The interface for managing campaigns
 */
public interface CampaignService
{
	/**
	 * Provides list of all campaigns.
	 *
	 * @return list of all {@link CampaignModel}s.
	 */
	List<CampaignModel> getAllCampaigns();

	/**
	 * Provides list of all active campaigns. Campaign is considered as active when it has {@link CampaignModel#ENABLED}
	 * eq true and the current time provided by {@link de.hybris.platform.servicelayer.time.TimeService} API is between
	 * {@link CampaignModel#STARTDATE} and {@link CampaignModel#ENDDATE} inclusive. When the value of
	 * {@link CampaignModel#STARTDATE} or {@link CampaignModel#ENDDATE} is not set then this value is considered as
	 * within date range.
	 *
	 * @return list of active {@link CampaignModel}s.
	 */
	List<CampaignModel> getActiveCampaigns();

	/**
	 * Provides {@link CampaignModel} with requested {@link CampaignModel#CODE}
	 *
	 * @param code
	 * 		code of campaign
	 * @return matching {@link CampaignModel}
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
	 * 		if campaign with given code hasn't been found
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException
	 * 		if there are more than one campaign using the same code
	 */
	CampaignModel getCampaignByCode(String code);
}
