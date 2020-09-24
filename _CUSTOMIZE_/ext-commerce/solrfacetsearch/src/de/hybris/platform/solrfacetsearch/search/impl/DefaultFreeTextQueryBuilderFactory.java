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
package de.hybris.platform.solrfacetsearch.search.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.solrfacetsearch.search.FreeTextQueryBuilder;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryBuilderFactory;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


/**
 * Default implementation for the {@FreeTextQueryBuilderFactory} interface which returns an implementation for the
 * {@FreeTextQueryBuilder} based on the query builder attribute of the index type configuration
 */
public class DefaultFreeTextQueryBuilderFactory implements FreeTextQueryBuilderFactory, BeanFactoryAware
{
	private static final Logger LOG = Logger.getLogger(DefaultFreeTextQueryBuilderFactory.class);

	private static final String DEFAULT_QUERY_BUILDER = "defaultFreeTextQueryBuilder";

	private BeanFactory beanFactory;

	@Override
	public FreeTextQueryBuilder createQueryBuilder(final SearchQuery searchQuery)
	{
		validateParameterNotNull(searchQuery, "Parameter 'search query' can not be null!");
		validateParameterNotNull(searchQuery.getIndexedType(), "Parameter 'index type' can not be null!");

		String queryBuilderBeanId = searchQuery.getFreeTextQueryBuilder();
		if (StringUtils.isBlank(queryBuilderBeanId))
		{
			queryBuilderBeanId = DEFAULT_QUERY_BUILDER;
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Using query builder " + queryBuilderBeanId);
		}

		return beanFactory.getBean(queryBuilderBeanId, FreeTextQueryBuilder.class);
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}
}
