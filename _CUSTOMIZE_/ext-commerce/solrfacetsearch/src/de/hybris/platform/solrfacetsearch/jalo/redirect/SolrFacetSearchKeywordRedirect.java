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
package de.hybris.platform.solrfacetsearch.jalo.redirect;

import de.hybris.platform.jalo.SessionContext;


public class SolrFacetSearchKeywordRedirect extends GeneratedSolrFacetSearchKeywordRedirect
{
	@Override
	public void setKeyword(final SessionContext ctx, final String value)
	{
		super.setKeyword(ctx, value.trim());
	}

	@Override
	public void setKeyword(final String value)
	{
		super.setKeyword(value.trim());
	}
}
