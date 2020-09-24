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
package de.hybris.platform.basecommerce.strategies.impl;



import de.hybris.platform.basecommerce.exceptions.BaseSiteActivationException;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.strategies.ActivateBaseSiteInSessionStrategy;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Base implementation for the activating attributes in session. Attributes collected
 */
public class DefaultActivateBaseSiteInSessionStrategy<T extends BaseSiteModel> implements ActivateBaseSiteInSessionStrategy<T>
{

	private static final Logger LOG = Logger.getLogger(DefaultActivateBaseSiteInSessionStrategy.class);

	private CatalogVersionService catalogVersionService;

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}


	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Override
	public void activate(final T site)
	{
		try
		{
			getCatalogVersionService().setSessionCatalogVersions(collectCatalogVersions(site));
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Set session catalog version for site " + site + " failed " + e.getMessage(), e);
			}
			throw new BaseSiteActivationException(e);
		}
	}


	/**
	 * Collects a {@link CatalogVersionModel}s for a given site.
	 */
	protected Collection<CatalogVersionModel> collectCatalogVersions(final T site)
	{
		final Set<CatalogModel> ret = collectContentCatalogs(site);
		final Set<CatalogVersionModel> catalogVersions = new HashSet<CatalogVersionModel>();
		for (final CatalogModel catalog : ret)
		{
			final CatalogVersionModel activeVersion = catalog.getActiveCatalogVersion();
			if (activeVersion == null)
			{
				throw new ModelNotFoundException("catalog [" + catalog.getId() + "] " + catalog.getName()
						+ " has no active catalog version.");
			}
			catalogVersions.add(catalog.getActiveCatalogVersion());
		}
		return catalogVersions;
	}

	/**
	 * Collects a {@link CatalogModel}s using a base information for {@link BaseStoreModel#getCatalogs()}
	 */
	protected Set<CatalogModel> collectContentCatalogs(final T site)
	{
		final Set<CatalogModel> ret = new HashSet<CatalogModel>();
		if (site == null)
		{
			throw new IllegalArgumentException("No site specified.");
		}
		else
		{
			for (final BaseStoreModel baseStore : site.getStores())
			{
				ret.addAll(baseStore.getCatalogs());
			}
		}
		return ret;
	}

}
