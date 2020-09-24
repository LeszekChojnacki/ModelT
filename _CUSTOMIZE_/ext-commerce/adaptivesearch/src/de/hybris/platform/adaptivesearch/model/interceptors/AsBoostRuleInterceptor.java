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

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.util.ObjectConverter;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;
import java.util.Optional;


/**
 * Interceptor for {@link AsBoostRuleModel}.
 */
public class AsBoostRuleInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AsBoostRuleModel>, ValidateInterceptor<AsBoostRuleModel>, RemoveInterceptor<AsBoostRuleModel>
{
	@Override
	public void onPrepare(final AsBoostRuleModel boostRule, final InterceptorContext context) throws InterceptorException
	{
		markItemAsModified(context, boostRule, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	@Override
	public void onValidate(final AsBoostRuleModel boostRule, final InterceptorContext context) throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = boostRule.getCatalogVersion();
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveAndValidateSearchConfiguration(boostRule);

		if (searchConfiguration != null && !Objects.equals(catalogVersion, searchConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}

		final AbstractAsSearchProfileModel searchProfile = resolveAndValidateSearchProfile(searchConfiguration);
		final String indexType = searchProfile.getIndexType();
		final String indexProperty = boostRule.getIndexProperty();

		final AsSearchProvider searchProvider = resolveSearchProvider();
		final Optional<AsIndexPropertyData> indexPropertyDataOptional = searchProvider.getIndexPropertyForCode(indexType,
				indexProperty);

		if (!indexPropertyDataOptional.isPresent())
		{
			throw new InterceptorException("Index property does not exist: " + indexProperty);
		}

		final AsIndexPropertyData indexPropertyData = indexPropertyDataOptional.get();
		if (boostRule.getOperator() != null && !indexPropertyData.getSupportedBoostOperators().contains(boostRule.getOperator()))
		{
			throw new InterceptorException(
					"Operator " + boostRule.getOperator().getCode() + " is not supported for index property: " + indexProperty);
		}

		try
		{
			ObjectConverter.convert(boostRule.getValue(), indexPropertyData.getType());
		}
		catch (final AsException e)
		{
			throw new InterceptorException(
					"Value " + boostRule.getValue() + " is not of type: " + indexPropertyData.getType().getName(), e);
		}
	}

	@Override
	public void onRemove(final AsBoostRuleModel boostRule, final InterceptorContext context) throws InterceptorException
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(boostRule);
		markItemAsModified(context, searchConfiguration, SEARCH_PROFILE_ATTRIBUTE);
	}
}
