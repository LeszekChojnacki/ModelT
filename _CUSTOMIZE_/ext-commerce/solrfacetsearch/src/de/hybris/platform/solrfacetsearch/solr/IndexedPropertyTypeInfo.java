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
package de.hybris.platform.solrfacetsearch.solr;

import de.hybris.platform.solrfacetsearch.search.SearchQuery.QueryOperator;

import java.io.Serializable;
import java.util.Set;


/**
 * The class that holds information about the indexed property type.
 */
public class IndexedPropertyTypeInfo implements Serializable
{
	private static final long serialVersionUID = 6778030067767357571L;

	private Class<?> javaType;
	private boolean allowFacet;
	private Set<QueryOperator> supportedQueryOperators;

	public Class<?> getJavaType()
	{
		return javaType;
	}

	public void setJavaType(final Class<?> javaType)
	{
		this.javaType = javaType;
	}

	public boolean isAllowFacet()
	{
		return allowFacet;
	}

	public void setAllowFacet(final boolean allowFacet)
	{
		this.allowFacet = allowFacet;
	}

	public Set<QueryOperator> getSupportedQueryOperators()
	{
		return supportedQueryOperators;
	}

	public void setSupportedQueryOperators(final Set<QueryOperator> supportedQueryOperators)
	{
		this.supportedQueryOperators = supportedQueryOperators;
	}
}
