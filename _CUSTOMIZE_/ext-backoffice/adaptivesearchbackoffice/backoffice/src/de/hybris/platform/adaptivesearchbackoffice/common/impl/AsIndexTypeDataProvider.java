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

import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Objects;


/**
 * Implementation of {@link DataProvider} for index types.
 */
public class AsIndexTypeDataProvider implements DataProvider<AsIndexTypeData, String>
{
	private static final String INDEX_CONFIGURATION = "indexConfiguration";

	private AsSearchProviderFactory asSearchProviderFactory;

	@Override
	public List<AsIndexTypeData> getData(final Map<String, Object> parameters)
	{
		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final String indexConfiguration = resolveIndexConfiguration(parameters);

		List<AsIndexTypeData> indexTypes;

		if (StringUtils.isBlank(indexConfiguration))
		{
			indexTypes = searchProvider.getIndexTypes();
		}
		else
		{
			indexTypes = searchProvider.getIndexTypes(indexConfiguration);
		}

		Collections.sort(indexTypes, this::compareIndexTypes);

		return indexTypes;
	}

	@Override
	public String getValue(final AsIndexTypeData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return null;
		}

		return data.getCode();
	}

	@Override
	public String getLabel(final AsIndexTypeData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return StringUtils.EMPTY;
		}

		return data.getCode();
	}

	protected String resolveIndexConfiguration(final Map<String, Object> parameters)
	{
		return Objects.toString(parameters.get(INDEX_CONFIGURATION));
	}

	protected int compareIndexTypes(final AsIndexTypeData indexType1, final AsIndexTypeData indexType2)
	{
		return indexType1.getCode().compareTo(indexType2.getCode());
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
