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
package de.hybris.platform.site.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.site.dao.BaseSiteDao;
import de.hybris.platform.basecommerce.strategies.ActivateBaseSiteInSessionStrategy;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;


/**
 * Default implementation of {@link BaseSiteService} interface.
 */
public class DefaultBaseSiteService implements BaseSiteService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBaseSiteService.class);
	protected static final String CURRENTSITE = "currentSite";

	@Resource
	private SessionService sessionService;
	@Resource
	private BaseSiteDao baseSiteDao;

	private ActivateBaseSiteInSessionStrategy<BaseSiteModel> activateBaseSiteInSessionStrategy;

	protected ActivateBaseSiteInSessionStrategy<BaseSiteModel> getActivateBaseSiteInSessionStrategy()
	{
		return activateBaseSiteInSessionStrategy;
	}

	public void setActivateBaseSiteInSessionStrategy(
			final ActivateBaseSiteInSessionStrategy<BaseSiteModel> activateBaseSiteInSessionStrategy)
	{
		this.activateBaseSiteInSessionStrategy = activateBaseSiteInSessionStrategy;
	}

	@Override
	public Collection<BaseSiteModel> getAllBaseSites()
	{
		return getBaseSiteDao().findAllBaseSites();
	}

	@Override
	public BaseSiteModel getBaseSiteForUID(final String siteUid)
	{
		return getBaseSiteDao().findBaseSiteByUID(siteUid);
	}

	protected BaseSiteModel getCurrentBaseSiteImpl()
	{
		try
		{
			return getSessionService().getAttribute(CURRENTSITE);
		}
		catch (final JaloObjectNoLongerValidException ex) // NOSONAR
		{
			if (LOG.isInfoEnabled())
			{
				LOG.info("Session Site no longer valid. Removing from session. getCurrentBaseSite will return null. {}",
						ex.getMessage());
			}
			getSessionService().setAttribute(CURRENTSITE, null);
		}
		return null;
	}

	@Override
	public BaseSiteModel getCurrentBaseSite()
	{
		final BaseSiteModel site = getCurrentBaseSiteImpl();
		if (LOG.isDebugEnabled() && site == null)
		{
			LOG.debug("No current site set");
		}
		return site;
	}

	@Override
	public List<CatalogModel> getProductCatalogs(final BaseSiteModel site)
	{
		final List<CatalogModel> result = new ArrayList<>();
		if (site == null)
		{
			return result;
		}

		final Collection<BaseStoreModel> stores = site.getStores();
		final Set<CatalogModel> collectedCatalogs = stores.stream().flatMap(store -> store.getCatalogs().stream())
				.filter(this::isPlainCatalogModel).collect(Collectors.toSet());
		result.addAll(collectedCatalogs);
		return result;
	}

	@Override
	public void setCurrentBaseSite(final String siteUid, final boolean activateAdditionalSessionAdjustments)
	{
		final BaseSiteModel baseSite = getBaseSiteForUID(siteUid);
		setCurrentBaseSite(baseSite, activateAdditionalSessionAdjustments);
	}

	@Override
	public void setCurrentBaseSite(final BaseSiteModel newBaseSite, final boolean activateAdditionalSessionAdjustments)
	{
		setCurrentBaseSiteImpl(newBaseSite);

		if (activateAdditionalSessionAdjustments)
		{
			getActivateBaseSiteInSessionStrategy().activate(newBaseSite);
		}
	}

	protected boolean isPlainCatalogModel(final CatalogModel catalog)
	{
		return catalog != null && catalog.getClass() == CatalogModel.class;
	}

	protected void setCurrentBaseSiteImpl(final BaseSiteModel newBaseSite)
	{
		getSessionService().setAttribute(CURRENTSITE, newBaseSite);
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected BaseSiteDao getBaseSiteDao()
	{
		return baseSiteDao;
	}

	@Required
	public void setBaseSiteDao(final BaseSiteDao baseSiteDao)
	{
		this.baseSiteDao = baseSiteDao;
	}

}
