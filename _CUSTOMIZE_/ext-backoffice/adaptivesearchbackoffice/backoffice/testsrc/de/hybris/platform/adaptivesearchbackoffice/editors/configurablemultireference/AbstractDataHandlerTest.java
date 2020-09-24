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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;

import java.util.ArrayList;


public abstract class AbstractDataHandlerTest
{
	protected <T, R> AsConfigurationHolder<T, R> createConfigurationHolder(final T configuration)
	{
		final AsConfigurationHolder<T, R> configurationHolder = new AsConfigurationHolder<>();
		configurationHolder.setConfiguration(configuration);

		return configurationHolder;
	}

	protected SearchResultData createSearchResult()
	{

		final AsSearchProfileResult searchProfileResult = new AsSearchProfileResult();
		searchProfileResult.setBoostRules(new ArrayList<>());

		final AsSearchResultData asSearchResult = new AsSearchResultData();
		asSearchResult.setSearchProfileResult(searchProfileResult);

		final SearchResultData searchResult = new SearchResultData();
		searchResult.setAsSearchResult(asSearchResult);

		return searchResult;
	}
}
