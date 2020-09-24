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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SORT_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearch.data.AsExpressionData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRuntimeException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Objects;


/**
 * Implementation of {@link DataProvider} for sort expressions.
 */
public class AsSortExpressionDataProvider implements DataProvider<AsExpressionData, String>
{
	protected static final String INDEX_TYPE = "indexType";

	private AsSearchProviderFactory asSearchProviderFactory;
	private ModelService modelService;

	@Override
	public List<AsExpressionData> getData(final Map<String, Object> parameters)
	{
		final String indexType = resolveIndexType(parameters);

		if (StringUtils.isBlank(indexType))
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<AsExpressionData> expressions = searchProvider.getSupportedSortExpressions(indexType);

		final AsSortExpressionModel sortExpression = resolveSortExpression(parameters);
		final AbstractAsSortConfigurationModel sortConfiguration = resolveSortConfiguration(sortExpression);
		final Set<String> usedExpressions = collectUsedExpressions(sortConfiguration);

		// removes the current expression from the used expressions so that we can select it again
		if (StringUtils.isNotBlank(sortExpression.getExpression()))
		{
			usedExpressions.remove(sortExpression.getExpression());
		}

		return expressions.stream().filter(expression -> !usedExpressions.contains(expression.getExpression()))
				.collect(Collectors.toList());
	}

	protected Set<String> collectUsedExpressions(final AbstractAsSortConfigurationModel sortConfiguration)
	{
		final Set<String> expressions = new HashSet<>();

		if (CollectionUtils.isNotEmpty(sortConfiguration.getExpressions()))
		{
			expressions.addAll(sortConfiguration.getExpressions().stream().map(AsSortExpressionModel::getExpression)
					.collect(Collectors.toList()));
		}

		return expressions;
	}

	@Override
	public String getValue(final AsExpressionData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return null;
		}

		return data.getExpression();
	}

	@Override
	public String getLabel(final AsExpressionData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return StringUtils.EMPTY;
		}

		return data.getExpression();
	}

	protected String resolveIndexType(final Map<String, Object> parameters)
	{
		return Objects.toString(parameters.get(INDEX_TYPE));
	}

	protected AsSortExpressionModel resolveSortExpression(final Map<String, Object> parameters)
	{
		final Object sortExpression = parameters.get(PARENT_OBJECT_KEY);

		if (!(sortExpression instanceof AsSortExpressionModel))
		{
			throw new EditorRuntimeException("Sort expression not valid");
		}

		return (AsSortExpressionModel) sortExpression;
	}

	protected AbstractAsSortConfigurationModel resolveSortConfiguration(final AsSortExpressionModel sortExpression)
	{
		final Object sortConfiguration = modelService.getAttributeValue(sortExpression, SORT_CONFIGURATION_ATTRIBUTE);

		if (!(sortConfiguration instanceof AbstractAsSortConfigurationModel))
		{
			throw new EditorRuntimeException("Sort configuration not valid");
		}

		return (AbstractAsSortConfigurationModel) sortConfiguration;
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

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
