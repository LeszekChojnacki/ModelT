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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.AsRuntimeException;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsExpressionData;
import de.hybris.platform.adaptivesearch.data.AsIndexConfigurationData;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.data.AsSearchQueryData;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation for {@link AsSearchProviderFactory}
 */
public class DefaultAsSearchProviderFactory implements AsSearchProviderFactory, ApplicationContextAware
{
	private ApplicationContext applicationContext;

	@Override
	public AsSearchProvider getSearchProvider()
	{
		final Map<String, AsSearchProvider> searchProviders = applicationContext.getBeansOfType(AsSearchProvider.class);

		if (MapUtils.isEmpty(searchProviders))
		{
			throw new AsRuntimeException("No search provider found");
		}

		if (searchProviders.size() == 1)
		{
			return searchProviders.values().iterator().next();
		}
		else
		{
			return new CombinedSearchProvider(searchProviders.values());
		}
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	protected static class CombinedSearchProvider implements AsSearchProvider
	{
		private final Collection<AsSearchProvider> searchProviders;

		public CombinedSearchProvider(final Collection<AsSearchProvider> searchProviders)
		{
			this.searchProviders = searchProviders;
		}

		protected Collection<AsSearchProvider> getSearchProviders()
		{
			return searchProviders;
		}

		protected AsSearchProvider resolveSearchProviderForIndexConfiguration(final String indexConfiguration)
		{
			for (final AsSearchProvider searchProvider : searchProviders)
			{
				final Optional<AsIndexConfigurationData> indexConfigurationOptional = searchProvider
						.getIndexConfigurationForCode(indexConfiguration);
				if (indexConfigurationOptional.isPresent())
				{
					return searchProvider;
				}
			}

			throw new AsRuntimeException("No search provider found that can handle index configuration: " + indexConfiguration);
		}

		protected AsSearchProvider resolveSearchProviderForIndexType(final String indexType)
		{
			for (final AsSearchProvider searchProvider : searchProviders)
			{
				final Optional<AsIndexTypeData> indexTypeOptional = searchProvider.getIndexTypeForCode(indexType);
				if (indexTypeOptional.isPresent())
				{
					return searchProvider;
				}
			}

			throw new AsRuntimeException("No search provider found that can handle index type: " + indexType);
		}

		@Override
		public List<AsIndexConfigurationData> getIndexConfigurations()
		{
			final List<AsIndexConfigurationData> indexConfigurations = new ArrayList<>();

			for (final AsSearchProvider searchProvider : searchProviders)
			{
				indexConfigurations.addAll(searchProvider.getIndexConfigurations());
			}

			return indexConfigurations;
		}

		@Override
		public Optional<AsIndexConfigurationData> getIndexConfigurationForCode(final String code)
		{
			for (final AsSearchProvider searchProvider : searchProviders)
			{
				final Optional<AsIndexConfigurationData> indexConfigurationOptional = searchProvider
						.getIndexConfigurationForCode(code);
				if (indexConfigurationOptional.isPresent())
				{
					return indexConfigurationOptional;
				}
			}

			return Optional.empty();
		}

		@Override
		public List<AsIndexTypeData> getIndexTypes()
		{
			final List<AsIndexTypeData> indexTypes = new ArrayList<>();

			for (final AsSearchProvider searchProvider : searchProviders)
			{
				indexTypes.addAll(searchProvider.getIndexTypes());
			}

			return indexTypes;
		}

		@Override
		public List<AsIndexTypeData> getIndexTypes(final String indexConfiguration)
		{
			return resolveSearchProviderForIndexConfiguration(indexConfiguration).getIndexTypes();
		}

		@Override
		public Optional<AsIndexTypeData> getIndexTypeForCode(final String code)
		{
			for (final AsSearchProvider searchProvider : searchProviders)
			{
				final Optional<AsIndexTypeData> indexTypeOptional = searchProvider.getIndexTypeForCode(code);
				if (indexTypeOptional.isPresent())
				{
					return indexTypeOptional;
				}
			}

			return Optional.empty();
		}

		@Override
		public List<AsIndexPropertyData> getIndexProperties(final String indexType)
		{
			return resolveSearchProviderForIndexType(indexType).getIndexProperties(indexType);
		}

		@Override
		public Optional<AsIndexPropertyData> getIndexPropertyForCode(final String indexType, final String code)
		{
			return resolveSearchProviderForIndexType(indexType).getIndexPropertyForCode(indexType, code);
		}

		@Override
		public List<CatalogVersionModel> getSupportedCatalogVersions(final String indexConfiguration, final String indexType)
		{
			return resolveSearchProviderForIndexConfiguration(indexConfiguration).getSupportedCatalogVersions(indexConfiguration,
					indexType);
		}

		@Override
		public List<LanguageModel> getSupportedLanguages(final String indexConfiguration, final String indexType)
		{
			return resolveSearchProviderForIndexConfiguration(indexConfiguration).getSupportedLanguages(indexConfiguration,
					indexType);
		}

		@Override
		public List<CurrencyModel> getSupportedCurrencies(final String indexConfiguration, final String indexType)
		{
			return resolveSearchProviderForIndexConfiguration(indexConfiguration).getSupportedCurrencies(indexConfiguration,
					indexType);
		}

		@Override
		public List<AsIndexPropertyData> getSupportedFacetIndexProperties(final String indexType)
		{
			return resolveSearchProviderForIndexType(indexType).getSupportedFacetIndexProperties(indexType);
		}

		@Override
		public boolean isValidFacetIndexProperty(final String indexType, final String code)
		{
			return resolveSearchProviderForIndexType(indexType).isValidFacetIndexProperty(indexType, code);
		}

		@Override
		public List<AsExpressionData> getSupportedSortExpressions(final String indexType)
		{
			return resolveSearchProviderForIndexType(indexType).getSupportedSortExpressions(indexType);
		}

		@Override
		public boolean isValidSortExpression(final String indexType, final String expression)
		{
			return resolveSearchProviderForIndexType(indexType).isValidSortExpression(indexType, expression);
		}

		@Override
		public AsSearchResultData search(final AsSearchProfileContext context, final AsSearchQueryData searchQuery)
				throws AsException
		{
			final String indexConfiguration = context.getIndexConfiguration();
			return resolveSearchProviderForIndexConfiguration(indexConfiguration).search(context, searchQuery);
		}
	}
}
