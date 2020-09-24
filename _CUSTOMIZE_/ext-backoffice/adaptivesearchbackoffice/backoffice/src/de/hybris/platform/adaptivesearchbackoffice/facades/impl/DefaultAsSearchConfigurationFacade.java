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
package de.hybris.platform.adaptivesearchbackoffice.facades.impl;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchConfigurationService;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileService;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchConfigurationFacade;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchProfileContextFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.Optional;


/**
 * Default implementation for {@link AsSearchConfigurationFacade}
 */
public class DefaultAsSearchConfigurationFacade implements AsSearchConfigurationFacade
{
	private AsSearchProfileContextFacade asSearchProfileContextFacade;
	private AsSearchProfileService asSearchProfileService;
	private AsSearchConfigurationService asSearchConfigurationService;
	private CatalogVersionService catalogVersionService;

	@Override
	public AbstractAsConfigurableSearchConfigurationModel getOrCreateSearchConfiguration(
			final NavigationContextData navigationContext, final SearchContextData searchContext)
	{
		final AsSearchProfileContext searchProfileContext = asSearchProfileContextFacade
				.createSearchProfileContext(navigationContext, searchContext);

		final CatalogVersionModel catalogVersion = resolveCatalogVersion(navigationContext.getCatalogVersion());

		final Optional<AbstractAsSearchProfileModel> searchProfile = asSearchProfileService.getSearchProfileForCode(catalogVersion,
				navigationContext.getCurrentSearchProfile());

		return asSearchConfigurationService.getOrCreateSearchConfigurationForContext(searchProfileContext, searchProfile.get());
	}

	protected CatalogVersionModel resolveCatalogVersion(final CatalogVersionData catalogVersion)
	{
		if (catalogVersion == null)
		{
			return null;
		}

		return catalogVersionService.getCatalogVersion(catalogVersion.getCatalogId(), catalogVersion.getVersion());
	}

	public AsSearchProfileContextFacade getAsSearchProfileContextFacade()
	{
		return asSearchProfileContextFacade;
	}

	public void setAsSearchProfileContextFacade(final AsSearchProfileContextFacade asSearchProfileContextFacade)
	{
		this.asSearchProfileContextFacade = asSearchProfileContextFacade;
	}

	public AsSearchProfileService getAsSearchProfileService()
	{
		return asSearchProfileService;
	}

	public void setAsSearchProfileService(final AsSearchProfileService asSearchProfileService)
	{
		this.asSearchProfileService = asSearchProfileService;
	}

	public AsSearchConfigurationService getAsSearchConfigurationService()
	{
		return asSearchConfigurationService;
	}

	public void setAsSearchConfigurationService(final AsSearchConfigurationService asSearchConfigurationService)
	{
		this.asSearchConfigurationService = asSearchConfigurationService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

}
