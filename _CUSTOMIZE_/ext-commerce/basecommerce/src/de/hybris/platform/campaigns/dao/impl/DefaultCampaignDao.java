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
package de.hybris.platform.campaigns.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.hybris.platform.campaigns.dao.CampaignDao;
import de.hybris.platform.campaigns.model.CampaignModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;


/**
 * Default implementation of {@link CampaignDao}
 */
public class DefaultCampaignDao extends DefaultGenericDao<CampaignModel> implements CampaignDao
{
	private static final String CURRENT_DATETIME_QUERY_PARAM = "now";
	private static final String FIND_ACTIVE_CAMAPIGNS = "SELECT {Pk} FROM {" + CampaignModel._TYPECODE + "} WHERE {"
			+ CampaignModel.ENABLED + "} = ?" + CampaignModel.ENABLED + " AND " + "({" + CampaignModel.STARTDATE + "} IS NULL OR {"
			+ CampaignModel.STARTDATE + "} <= ?" + CURRENT_DATETIME_QUERY_PARAM + ") " + "AND " + "({" + CampaignModel.ENDDATE
			+ "} IS NULL OR {" + CampaignModel.ENDDATE + "} >= ?" + CURRENT_DATETIME_QUERY_PARAM + ") ";

	public DefaultCampaignDao()
	{
		super(CampaignModel._TYPECODE);
	}

	@Override
	public List<CampaignModel> findActiveCampaigns(Date currentDateTime)
	{
		validateParameterNotNullStandardMessage("currentDateTime", currentDateTime);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ACTIVE_CAMAPIGNS);
		query.addQueryParameter(CampaignModel.ENABLED, Boolean.TRUE);
		query.addQueryParameter(CURRENT_DATETIME_QUERY_PARAM, currentDateTime);
		return getFlexibleSearchService().<CampaignModel> search(query).getResult();
	}

	@Override
	public CampaignModel findCampaignByCode(final String code)
	{
		validateParameterNotNullStandardMessage(CampaignModel.CODE, code);
		final Map<String, Object> params = ImmutableMap.of(CampaignModel.CODE, code);
		final List<CampaignModel> campaigns = find(params);
		validateIfSingleResult(campaigns,"No campaign with given code["+code+"] was found","More than one campaign with given code [" + code + "] was found");
		return campaigns.iterator().next();
	}

	@Override
	public List<CampaignModel> findAllCampaigns()
	{
		return find();
	}
}
