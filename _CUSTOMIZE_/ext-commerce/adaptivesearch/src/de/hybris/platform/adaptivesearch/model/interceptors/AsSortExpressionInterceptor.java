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
package de.hybris.platform.adaptivesearch.model.interceptors;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SORT_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AsSortExpressionModel}.
 */
public class AsSortExpressionInterceptor extends AbstractAsInterceptor implements PrepareInterceptor<AsSortExpressionModel>,
		ValidateInterceptor<AsSortExpressionModel>, RemoveInterceptor<AsSortExpressionModel>
{
	@Override
	public void onPrepare(final AsSortExpressionModel sortExpression, final InterceptorContext context) throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(sortExpression, context);
		if (!StringUtils.equals(uniqueIdx, sortExpression.getUniqueIdx()))
		{
			sortExpression.setUniqueIdx(uniqueIdx);
		}

		markItemAsModified(context, sortExpression, SORT_CONFIGURATION_ATTRIBUTE, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected String generateUniqueIdx(final AsSortExpressionModel sortExpression, final InterceptorContext context)
	{
		final AbstractAsSortConfigurationModel sortConfiguration = resolveSortConfiguration(sortExpression);

		return generateItemIdentifier(sortConfiguration, context) + UNIQUE_IDX_SEPARATOR
				+ decorateIdentifier(sortExpression.getExpression());
	}

	@Override
	public void onValidate(final AsSortExpressionModel sortExpression, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = sortExpression.getCatalogVersion();
		final AbstractAsSortConfigurationModel sortConfiguration = resolveAndValidateSortConfiguration(sortExpression);

		if (!Objects.equals(catalogVersion, sortConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}

		final AbstractAsSearchConfigurationModel searchConfiguration = resolveAndValidateSearchConfiguration(sortConfiguration);
		final AbstractAsSearchProfileModel searchProfile = resolveAndValidateSearchProfile(searchConfiguration);
		final String indexType = searchProfile.getIndexType();
		final String expression = sortExpression.getExpression();

		final AsSearchProvider searchProvider = resolveSearchProvider();

		if (!searchProvider.isValidSortExpression(indexType, expression))
		{
			throw new InterceptorException("Expression cannot be used for sorts: " + expression);
		}
	}

	@Override
	public void onRemove(final AsSortExpressionModel sortExpression, final InterceptorContext context) throws InterceptorException
	{
		final AbstractAsSortConfigurationModel sortConfiguration = resolveSortConfiguration(sortExpression);
		markItemAsModified(context, sortConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected AbstractAsSortConfigurationModel resolveSortConfiguration(final ItemModel model)
	{
		return getModelService().getAttributeValue(model, SORT_CONFIGURATION_ATTRIBUTE);
	}

	protected AbstractAsSortConfigurationModel resolveAndValidateSortConfiguration(final ItemModel model)
			throws InterceptorException
	{
		final Object sortConfiguration = getModelService().getAttributeValue(model, SORT_CONFIGURATION_ATTRIBUTE);

		if (!(sortConfiguration instanceof AbstractAsSortConfigurationModel))
		{
			throw new InterceptorException("Invalid sort");
		}

		return (AbstractAsSortConfigurationModel) sortConfiguration;
	}
}
