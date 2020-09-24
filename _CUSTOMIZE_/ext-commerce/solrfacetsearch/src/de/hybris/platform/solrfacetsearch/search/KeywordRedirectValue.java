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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrAbstractKeywordRedirectModel;

import java.io.Serializable;


public class KeywordRedirectValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String keyword;
	private final KeywordRedirectMatchType matchType;
	private final SolrAbstractKeywordRedirectModel redirect;

	/**
	 * Keyword redirect DTO
	 *
	 * @param keyword
	 *           keyword (required)
	 * @param matchType
	 *           matchType (required)
	 * @param redirect
	 *           redirect (required)
	 * @throws IllegalArgumentException
	 *            if any of the parameters is null
	 */
	public KeywordRedirectValue(final String keyword, final KeywordRedirectMatchType matchType,
			final SolrAbstractKeywordRedirectModel redirect)
	{
		ServicesUtil.validateParameterNotNull(keyword, "Keyword required");
		ServicesUtil.validateParameterNotNull(matchType, "Match type required");
		ServicesUtil.validateParameterNotNull(redirect, "Redirect required");
		this.keyword = keyword;
		this.matchType = matchType;
		this.redirect = redirect;
	}

	public String getKeyword()
	{
		return keyword;
	}

	public KeywordRedirectMatchType getMatchType()
	{
		return matchType;
	}

	public SolrAbstractKeywordRedirectModel getRedirect()
	{
		return redirect;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + keyword.hashCode();
		result = prime * result + matchType.hashCode();
		result = prime * result + redirect.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final KeywordRedirectValue other = (KeywordRedirectValue) obj;
		if (!keyword.equals(other.keyword))
		{
			return false;
		}
		if (matchType != other.matchType)
		{
			return false;
		}
		if (!redirect.equals(other.redirect))
		{
			return false;
		}
		return true;
	}

}
