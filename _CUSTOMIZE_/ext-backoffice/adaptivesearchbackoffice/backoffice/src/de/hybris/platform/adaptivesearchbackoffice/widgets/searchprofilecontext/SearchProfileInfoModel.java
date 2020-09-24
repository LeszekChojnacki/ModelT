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
package de.hybris.platform.adaptivesearchbackoffice.widgets.searchprofilecontext;

import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Required;


/**
 * View model for information about the search profile context.
 */
public class SearchProfileInfoModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String searchProfileLabel;
	private AsSearchConfigurationInfoData searchConfigurationInfo;

	public String getSearchProfileLabel()
	{
		return searchProfileLabel;
	}

	@Required
	public void setSearchProfileLabel(final String searchProfileLabel)
	{
		this.searchProfileLabel = searchProfileLabel;
	}

	public AsSearchConfigurationInfoData getSearchConfigurationInfo()
	{
		return searchConfigurationInfo;
	}

	@Required
	public void setSearchConfigurationInfo(final AsSearchConfigurationInfoData searchConfigurationInfo)
	{
		this.searchConfigurationInfo = searchConfigurationInfo;
	}
}
