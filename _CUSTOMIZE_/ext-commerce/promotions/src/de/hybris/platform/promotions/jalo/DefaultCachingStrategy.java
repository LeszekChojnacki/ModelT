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
package de.hybris.platform.promotions.jalo;

import de.hybris.platform.servicelayer.session.SessionService;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import net.sf.ehcache.util.concurrent.ConcurrentHashMap;


/**
 * Created with IntelliJ IDEA. User: gary Date: 24/06/2013 Time: 12:54 To change this template use File | Settings |
 * File Templates.
 */
public class DefaultCachingStrategy implements CachingStrategy
{
	public static final String SESSION_CACHE_ATTRIBUTE = "promotion.cache";
	private SessionService sessionService;

	@Override
	public void put(final String code, final List<PromotionResult> results)
	{
		getCache().put(code, results);
	}

	@Override
	public List<PromotionResult> get(final String code)
	{
		final List<PromotionResult> results = getCache().get(code);
		return results == null ? Collections.emptyList() : results;
	}

	@Override
	public void remove(final String code)
	{
		getCache().remove(code);
	}

	protected Map<String, List<PromotionResult>> getCache()
	{
		return getSessionService().getOrLoadAttribute(SESSION_CACHE_ATTRIBUTE, SessionCacheContainer::new).getCache();
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected class SessionCacheContainer implements Serializable // NOSONAR
	{
		private final transient Map<String, List<PromotionResult>> cache;

		private SessionCacheContainer()
		{
			cache = new ConcurrentHashMap<>();
		}

		public Map<String, List<PromotionResult>> getCache()
		{
			return cache;
		}

	}


}
