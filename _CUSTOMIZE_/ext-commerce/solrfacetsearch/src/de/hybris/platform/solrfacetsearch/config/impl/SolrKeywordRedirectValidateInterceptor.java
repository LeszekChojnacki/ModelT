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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;
import de.hybris.platform.solrfacetsearch.search.SolrFacetSearchKeywordDao;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Validates if a keyword redirect for specified language, configuration and match type exist in db.
 */
public class SolrKeywordRedirectValidateInterceptor implements ValidateInterceptor
{
	private SolrFacetSearchKeywordDao solrFacetSearchKeywordDao;

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrFacetSearchKeywordRedirectModel)
		{
			final SolrFacetSearchKeywordRedirectModel keyword = (SolrFacetSearchKeywordRedirectModel) model;
			final SolrFacetSearchConfigModel config = keyword.getFacetSearchConfig();


			if (config == null)
			{
				throw new InterceptorException("Keyword redirects need to have facet search config.");
			}


			final List<SolrFacetSearchKeywordRedirectModel> list = solrFacetSearchKeywordDao.findKeywords(keyword.getKeyword(),
					keyword.getMatchType(), config.getName(), keyword.getLanguage().getIsocode());
			for (final SolrFacetSearchKeywordRedirectModel ind : list)
			{
				if (!ind.equals(keyword))
				{
					throw new InterceptorException("Keyword redirects with the same keyword and match type already exist.");
				}
			}


		}
	}

	@Required
	public void setSolrFacetSearchKeywordDao(final SolrFacetSearchKeywordDao solrFacetSearchKeywordDao)
	{
		this.solrFacetSearchKeywordDao = solrFacetSearchKeywordDao;
	}
}
