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



import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.converters.AsSearchConfigurationConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsGenericSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsReference;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsCacheKey;
import de.hybris.platform.adaptivesearch.strategies.AsCacheScope;
import de.hybris.platform.adaptivesearch.strategies.AsCacheStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.servicelayer.model.ModelService;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for implementations of {@link AbstractAsSearchProfileLoadStrategy}.
 *
 * @param <T>
 *           - the type of search profile model
 * @param <R>
 *           - the type of search profile data
 */
public abstract class AbstractAsSearchProfileLoadStrategy<T extends AbstractAsSearchProfileModel, R extends AbstractAsSearchProfile>
		implements AsSearchProfileLoadStrategy<T, R>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAsSearchProfileLoadStrategy.class);

	private ModelService modelService;
	private AsCacheStrategy asCacheStrategy;
	private ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter;

	@Override
	public Serializable getCacheKeyFragment(final AsSearchProfileContext context, final T searchProfile)
	{
		return null;
	}

	protected Map<String, AsConfigurableSearchConfiguration> loadSearchConfigurations(final AsSearchProfileContext context,
			final AsGenericSearchProfile source)
	{
		final Map<String, AsReference> availableSearchConfigurations = source.getAvailableSearchConfigurations();
		if (MapUtils.isEmpty(availableSearchConfigurations))
		{
			return Collections.emptyMap();
		}

		final Map<String, AsConfigurableSearchConfiguration> searchConfigurations = new HashMap<>();

		final AsSearchConfigurationConverterContext converterContext = new AsSearchConfigurationConverterContext();
		converterContext.setSearchProfileCode(source.getCode());

		final AsReference defaultSearchConfigurationReference = availableSearchConfigurations
				.get(AdaptivesearchConstants.DEFAULT_QUALIFIER);
		if (defaultSearchConfigurationReference != null)
		{
			loadSearchConfiguration(searchConfigurations, AdaptivesearchConstants.DEFAULT_QUALIFIER,
					defaultSearchConfigurationReference, converterContext);
		}

		if (StringUtils.isNotBlank(source.getQualifierType()))
		{
			final List<String> qualifiers = context.getQualifiers().get(source.getQualifierType());
			for (final String qualifier : CollectionUtils.emptyIfNull(qualifiers))
			{
				final AsReference searchConfigurationReference = availableSearchConfigurations.get(qualifier);
				if (searchConfigurationReference != null)
				{
					loadSearchConfiguration(searchConfigurations, qualifier, searchConfigurationReference, converterContext);
				}
			}
		}

		return searchConfigurations;
	}

	protected void loadSearchConfiguration(final Map<String, AsConfigurableSearchConfiguration> searchConfigurations,
			final String qualifier, final AsReference searchConfigurationReference,
			final AsSearchConfigurationConverterContext converterContext)
	{
		try
		{
			final Long baseKey = searchConfigurationReference.getPk().getLong();
			final Long baseKeyVersion = searchConfigurationReference.getVersion();

			final AsCacheKey loadKey = new DefaultAsCacheKey(AsCacheScope.LOAD, baseKey, baseKeyVersion);

			final AsConfigurableSearchConfiguration searchConfiguration = asCacheStrategy.getWithLoader(loadKey,
					key -> doLoadSearchConfiguration(searchConfigurationReference, converterContext));
			if (searchConfiguration != null)
			{
				searchConfigurations.put(qualifier, searchConfiguration);
			}
		}
		catch (final Exception e)
		{
			// we ignore the search configuration if an error occurs
			LOG.error("Could not load search configuration with pk " + searchConfigurationReference.getPk(), e);
		}
	}

	protected AsConfigurableSearchConfiguration doLoadSearchConfiguration(final AsReference searchConfigurationReference,
			final AsSearchConfigurationConverterContext converterContext)
	{
		final AbstractAsConfigurableSearchConfigurationModel source = modelService.get(searchConfigurationReference.getPk());

		if (source.isCorrupted())
		{
			return null;
		}

		return asConfigurableSearchConfigurationConverter.convert(source, converterContext);
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

	public AsCacheStrategy getAsCacheStrategy()
	{
		return asCacheStrategy;
	}

	@Required
	public void setAsCacheStrategy(final AsCacheStrategy asCacheStrategy)
	{
		this.asCacheStrategy = asCacheStrategy;
	}

	public ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> getAsConfigurableSearchConfigurationConverter()
	{
		return asConfigurableSearchConfigurationConverter;
	}

	@Required
	public void setAsConfigurableSearchConfigurationConverter(
			final ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter)
	{
		this.asConfigurableSearchConfigurationConverter = asConfigurableSearchConfigurationConverter;
	}
}
