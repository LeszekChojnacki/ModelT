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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.common.impl;

import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.enums.AsBoostOperator;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.labels.LabelService;


/**
 * Implementation of {@link DataProvider} for boost operator.
 */
public class AsBoostOperatorDataProvider implements DataProvider<AsBoostOperator, AsBoostOperator>
{
	protected static final String INDEX_PROPERTY_PARAM = "indexProperty";
	protected static final String INDEX_TYPE_PARAM = "indexType";

	private LabelService labelService;
	private AsSearchProviderFactory asSearchProviderFactory;

	@Override
	public List<AsBoostOperator> getData(final Map<String, Object> parameters)
	{
		final Object indexPropertyParam = parameters.get(INDEX_PROPERTY_PARAM);
		final Object indexTypeParam = parameters.get(INDEX_TYPE_PARAM);
		if (indexPropertyParam != null && indexTypeParam != null)
		{
			final String indexProperty = (String) indexPropertyParam;
			final String indexType = (String) indexTypeParam;

			final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
			final Optional<AsIndexPropertyData> indexPropertyData = searchProvider.getIndexPropertyForCode(indexType, indexProperty);

			if (!indexPropertyData.isPresent())
			{
				return Collections.emptyList();
			}

			return new ArrayList(indexPropertyData.get().getSupportedBoostOperators());
		}
		else
		{
			return Arrays.asList(AsBoostOperator.values());
		}
	}

	@Override
	public AsBoostOperator getValue(final AsBoostOperator data, final Map<String, Object> parameters)
	{
		return data;
	}

	@Override
	public String getLabel(final AsBoostOperator data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return StringUtils.EMPTY;
		}

		return labelService.getObjectLabel(data);
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
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