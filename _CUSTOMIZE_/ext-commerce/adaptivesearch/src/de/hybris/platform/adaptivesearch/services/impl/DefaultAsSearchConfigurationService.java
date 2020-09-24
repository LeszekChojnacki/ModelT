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
package de.hybris.platform.adaptivesearch.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.daos.AsSearchConfigurationDao;
import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchConfigurationService;
import de.hybris.platform.adaptivesearch.strategies.AsCloneStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchConfigurationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileRegistry;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchConfigurationService}.
 */
public class DefaultAsSearchConfigurationService implements AsSearchConfigurationService
{
	protected static final String UID_PARAM = "uid";
	protected static final String CONTEXT_PARAM = "context";
	protected static final String SEARCH_PROFILE_PARAM = "searchProfile";

	private AsSearchConfigurationDao asSearchConfigurationDao;
	private AsSearchProfileRegistry asSearchProfileRegistry;
	private AsCloneStrategy asCloneStrategy;

	@Override
	public List<AbstractAsSearchConfigurationModel> getAllSearchConfigurations()
	{
		return asSearchConfigurationDao.findAllSearchConfigurations();
	}

	@Override
	public List<AbstractAsSearchConfigurationModel> getSearchConfigurationsForCatalogVersion(
			final CatalogVersionModel catalogVersion)
	{
		return asSearchConfigurationDao.findSearchConfigurationsByCatalogVersion(catalogVersion);
	}

	@Override
	public <T extends AbstractAsSearchConfigurationModel> Optional<T> getSearchConfigurationForUid(
			final CatalogVersionModel catalogVersion, final String uid)
	{
		validateParameterNotNullStandardMessage(UID_PARAM, uid);

		return asSearchConfigurationDao.findSearchConfigurationByUid(catalogVersion, uid);
	}

	@Override
	public Optional<AbstractAsSearchConfigurationModel> getSearchConfigurationForContext(final AsSearchProfileContext context,
			final AbstractAsSearchProfileModel searchProfile)
	{
		validateParameterNotNullStandardMessage(CONTEXT_PARAM, context);
		validateParameterNotNullStandardMessage(SEARCH_PROFILE_PARAM, searchProfile);

		final AsSearchProfileMapping strategyMapping = asSearchProfileRegistry.getSearchProfileMapping(searchProfile);
		final AsSearchConfigurationStrategy searchConfigurationStrategy = strategyMapping.getSearchConfigurationStrategy();
		return searchConfigurationStrategy.getForContext(context, searchProfile);
	}

	@Override
	public AbstractAsSearchConfigurationModel getOrCreateSearchConfigurationForContext(final AsSearchProfileContext context,
			final AbstractAsSearchProfileModel searchProfile)
	{
		validateParameterNotNullStandardMessage(CONTEXT_PARAM, context);
		validateParameterNotNullStandardMessage(SEARCH_PROFILE_PARAM, searchProfile);

		final AsSearchProfileMapping strategyMapping = asSearchProfileRegistry.getSearchProfileMapping(searchProfile);
		final AsSearchConfigurationStrategy searchConfigurationStrategy = strategyMapping.getSearchConfigurationStrategy();
		return searchConfigurationStrategy.getOrCreateForContext(context, searchProfile);
	}

	@Override
	public AsSearchConfigurationInfoData getSearchConfigurationInfoForContext(final AsSearchProfileContext context,
			final AbstractAsSearchProfileModel searchProfile)
	{
		validateParameterNotNullStandardMessage(CONTEXT_PARAM, context);
		validateParameterNotNullStandardMessage(SEARCH_PROFILE_PARAM, searchProfile);

		final AsSearchProfileMapping strategyMapping = asSearchProfileRegistry.getSearchProfileMapping(searchProfile);
		final AsSearchConfigurationStrategy searchConfigurationStrategy = strategyMapping.getSearchConfigurationStrategy();
		return searchConfigurationStrategy.getInfoForContext(context, searchProfile);
	}

	@Override
	public <T extends AbstractAsSearchConfigurationModel> T cloneSearchConfiguration(final T searchConfiguration)
	{
		return asCloneStrategy.clone(searchConfiguration);
	}


	public AsSearchConfigurationDao getAsSearchConfigurationDao()
	{
		return asSearchConfigurationDao;
	}

	@Required
	public void setAsSearchConfigurationDao(final AsSearchConfigurationDao asSearchConfigurationDao)
	{
		this.asSearchConfigurationDao = asSearchConfigurationDao;
	}

	public AsSearchProfileRegistry getAsSearchProfileRegistry()
	{
		return asSearchProfileRegistry;
	}

	@Required
	public void setAsSearchProfileRegistry(final AsSearchProfileRegistry asSearchProfileRegistry)
	{
		this.asSearchProfileRegistry = asSearchProfileRegistry;
	}

	public AsCloneStrategy getAsCloneStrategy()
	{
		return asCloneStrategy;
	}

	@Required
	public void setAsCloneStrategy(final AsCloneStrategy asCloneStrategy)
	{
		this.asCloneStrategy = asCloneStrategy;
	}

	@Override
	public Set<String> getSearchConfigurationQualifiers(final AbstractAsSearchProfileModel searchProfile)
	{
		validateParameterNotNullStandardMessage(SEARCH_PROFILE_PARAM, searchProfile);
		final AsSearchProfileMapping strategyMapping = asSearchProfileRegistry.getSearchProfileMapping(searchProfile);
		final AsSearchConfigurationStrategy searchConfigurationStrategy = strategyMapping.getSearchConfigurationStrategy();

		return searchConfigurationStrategy.getQualifiers(searchProfile);
	}
}
