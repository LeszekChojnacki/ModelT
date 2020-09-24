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
package de.hybris.platform.adaptivesearchbackoffice.common.impl;

import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Objects;


/**
 * Implementation of {@link DataProvider} for index properties.
 */
public class AsIndexPropertyDataProvider implements DataProvider<AsIndexPropertyData, String>
{
	protected static final String INDEX_TYPE = "indexType";

	private AsSearchProviderFactory asSearchProviderFactory;

	@Override
	public List<AsIndexPropertyData> getData(final Map<String, Object> parameters)
	{
		final String indexType = resolveIndexType(parameters);

		if (StringUtils.isBlank(indexType))
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		return searchProvider.getIndexProperties(indexType);
	}

	@Override
	public String getValue(final AsIndexPropertyData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return null;
		}

		return data.getCode();
	}

	@Override
	public String getLabel(final AsIndexPropertyData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return StringUtils.EMPTY;
		}

		return data.getCode();
	}

	protected String resolveIndexType(final Map<String, Object> parameters)
	{
		return Objects.toString(parameters.get(INDEX_TYPE));
	}

	public AsSearchProviderFactory getAsSearchProviderFactory()
	{
		return asSearchProviderFactory;
	}

	@Required
	public void setAsSearchProviderFactory(final AsSearchProviderFactory asSearchProviderFactory)
	{
		this.asSearchProviderFactory = asSearchProviderFactory;
	}
}
