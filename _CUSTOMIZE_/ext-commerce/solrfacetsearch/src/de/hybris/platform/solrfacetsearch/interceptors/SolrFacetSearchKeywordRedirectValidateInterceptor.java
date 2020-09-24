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
package de.hybris.platform.solrfacetsearch.interceptors;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class SolrFacetSearchKeywordRedirectValidateInterceptor implements ValidateInterceptor
{

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (!(model instanceof SolrFacetSearchKeywordRedirectModel))
		{
			return;
		}

		final SolrFacetSearchKeywordRedirectModel solrFacetSearchKeywordRedirectModel = (SolrFacetSearchKeywordRedirectModel) model;
		if (solrFacetSearchKeywordRedirectModel.getMatchType() == KeywordRedirectMatchType.REGEX)
		{
			final String keyword = solrFacetSearchKeywordRedirectModel.getKeyword();
			try
			{
				Pattern.compile(keyword);
			}
			catch (final PatternSyntaxException e)
			{
				throw new InterceptorException("Given pattern is not a valid regular expression: " + keyword, e);
			}
		}
	}

}
