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
package de.hybris.platform.solrfacetsearch.indexer.listeners;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener that initializes the session.
 */
public class SessionInitializationListener implements IndexerQueryListener, IndexerListener, IndexerBatchListener
{
	private UserService userService;
	private I18NService i18nService;
	private CatalogVersionService catalogVersionService;

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

	public I18NService getI18nService()
	{
		return i18nService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	@Override
	public void beforeQuery(final IndexerQueryContext queryContext) throws IndexerException
	{
		initializeSession(queryContext.getFacetSearchConfig(), queryContext.getIndexedType());
	}

	@Override
	public void afterQuery(final IndexerQueryContext queryContext) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterQueryError(final IndexerQueryContext queryContext) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void beforeIndex(final IndexerContext context) throws IndexerException
	{
		initializeSession(context.getFacetSearchConfig(), context.getIndexedType());
	}

	@Override
	public void afterIndex(final IndexerContext context) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterIndexError(final IndexerContext context) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void beforeBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		initializeSession(batchContext.getFacetSearchConfig(), batchContext.getIndexedType());
	}

	@Override
	public void afterBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterBatchError(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	protected void initializeSession(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();

		i18nService.setLocalizationFallbackEnabled(indexConfig.isEnabledLanguageFallbackMechanism());
		catalogVersionService.setSessionCatalogVersions(indexConfig.getCatalogVersions());
	}
}
