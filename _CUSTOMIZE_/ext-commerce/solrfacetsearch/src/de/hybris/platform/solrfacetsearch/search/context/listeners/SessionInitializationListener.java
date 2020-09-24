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
package de.hybris.platform.solrfacetsearch.search.context.listeners;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Listener that initializes the session.
 */
public class SessionInitializationListener implements FacetSearchListener
{
	private CommonI18NService commonI18NService;
	private CatalogVersionService catalogVersionService;

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	@Override
	public void beforeSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();

		if (searchQuery.getLanguage() != null)
		{
			final LanguageModel language = commonI18NService.getLanguage(searchQuery.getLanguage());
			commonI18NService.setCurrentLanguage(language);
		}

		if (searchQuery.getCurrency() != null)
		{
			final CurrencyModel currency = commonI18NService.getCurrency(searchQuery.getCurrency());
			commonI18NService.setCurrentCurrency(currency);
		}

		if (CollectionUtils.isNotEmpty(searchQuery.getCatalogVersions()))
		{
			catalogVersionService.setSessionCatalogVersions(searchQuery.getCatalogVersions());
		}
	}

	@Override
	public void afterSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		// NOOP
	}

	@Override
	public void afterSearchError(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		// NOOP
	}
}
