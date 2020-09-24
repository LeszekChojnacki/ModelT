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
package de.hybris.platform.campaigns.dao;

import java.util.Date;
import java.util.List;

import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;


/**
 * Interface allows searching for {@link CampaignModel}
 */
public interface CampaignDao extends GenericDao<CampaignModel>
{
	/**
	 * Provides list of all campaigns.
	 *
	 * @return list of all {@link CampaignModel}s.
	 */
	List<CampaignModel> findAllCampaigns();

	/**
	 * Provides list of all active campaigns. Campaign is considered as active when it has {@link CampaignModel#ENABLED}
	 * eq true and the currentDateTime provided as a parameter is between {@link CampaignModel#STARTDATE} and
	 * {@link CampaignModel#ENDDATE} inclusive. When the value of {@link CampaignModel#STARTDATE} or
	 * {@link CampaignModel#ENDDATE} is not set then this value is considered as within date range.
	 *
	 * @param currentDateTime
	 *           - Current date time that is used to get active campaign for
	 * @return list of active {@link CampaignModel}s.
	 */
	List<CampaignModel> findActiveCampaigns(Date currentDateTime);

	/**
	 * Provides {@link CampaignModel} with requested {@link CampaignModel#CODE}
	 * 
	 * @param code
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
	 *            if campaign with given code hasn't been found
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException
	 *            if there are more than one campaign using the same code
	 * @return matching {@link CampaignModel}
	 */
	CampaignModel findCampaignByCode(String code);
}
