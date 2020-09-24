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
package de.hybris.platform.deeplink.dao.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.deeplink.dao.DeeplinkUrlDao;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlModel;
import de.hybris.platform.deeplink.model.rules.DeeplinkUrlRuleModel;
import de.hybris.platform.servicelayer.exceptions.ModelLoadingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * The Class DeeplinkUrlDaoImpl. Default implementation of {@link DeeplinkUrlDao}
 *
 * @spring.bean deeplinUrlDao
 *
 */
public class DeeplinkUrlDaoImpl implements DeeplinkUrlDao
{
	private static final Logger LOG = Logger.getLogger(DeeplinkUrlDaoImpl.class);
	private FlexibleSearchService searchService;

	private ModelService modelService;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.deeplink.dao.DeeplinkUrlDao#findDeeplinkUrlModel(java.lang.String)
	 */
	@Override
	public DeeplinkUrlModel findDeeplinkUrlModel(final String code)
	{
		DeeplinkUrlModel result = null;
		final Map params = new HashMap();
		params.put("code", code);
		final String query = "SELECT {" + DeeplinkUrlRuleModel.PK + "} FROM {" + DeeplinkUrlModel._TYPECODE + "} WHERE {"
				+ DeeplinkUrlModel.CODE + "} = ?code";
		final SearchResult<DeeplinkUrlModel> searchResult = getSearchService().search(query, params);
		if (!searchResult.getResult().isEmpty())
		{
			result = searchResult.getResult().get(0);
		}
		else
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("No DeeplinkUrl item was found with search code: " + sanitize(code));
			}
		}
		return result;
	}

	@Override
	public List<DeeplinkUrlRuleModel> findDeeplinkUrlRules()
	{
		final String query = "SELECT {" + DeeplinkUrlRuleModel.PK + "} FROM {" + DeeplinkUrlRuleModel._TYPECODE + "} ORDER BY {"
				+ DeeplinkUrlRuleModel.PRIORITY + "}";

		final SearchResult<DeeplinkUrlRuleModel> result = getSearchService().search(query);
		return result.getResult();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.deeplink.dao.DeeplinkUrlDao#findObject(java.lang.String)
	 */
	@Override
	public Object findObject(final String pkString)
	{
		Object result = null;
		try
		{
			result = getModelService().get(PK.parse(pkString));
		}
		catch (final ModelLoadingException e)
		{
			LOG.warn("Item with PK: " + sanitize(pkString) + " not found in the system" + e);
		}
		return result;
	}

	protected static String sanitize(final String input)
	{
		// clean input
		String output = StringUtils.defaultString(input).trim();
		// remove CRLF injection
		output = output.replaceAll("(\\r\\n|\\r|\\n)+", " ");
		// escape html
		output = StringEscapeUtils.escapeHtml(output);
		return output;
	}

	/**
	 * Gets the model service.
	 *
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * Gets the search service.
	 *
	 * @return the searchService
	 */
	public FlexibleSearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * Sets the model service.
	 *
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * Sets the search service.
	 *
	 * @param searchService
	 *           the searchService to set
	 */
	public void setSearchService(final FlexibleSearchService searchService)
	{
		this.searchService = searchService;
	}

}
