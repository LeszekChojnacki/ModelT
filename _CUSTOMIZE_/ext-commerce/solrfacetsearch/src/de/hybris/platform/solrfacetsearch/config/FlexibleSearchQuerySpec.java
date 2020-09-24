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
package de.hybris.platform.solrfacetsearch.config;

import java.util.Map;


/**
 * Represents a flexible search alongside with its parameters
 */
public interface FlexibleSearchQuerySpec
{
	/**
	 * Returns string representation of query stored in the solr configuration xml file.
	 * 
	 * @return String
	 */
	String getQuery();

	/**
	 * Creates a map representation of query parameters: <br>
	 * - injected by query attributes - injected by parameter provider specified by name in solr configuration xml file.
	 * 
	 * @return Map<String, Object>
	 */
	Map<String, Object> createParameters();

	/**
	 * Return user name used in solr configuration xml file to restrict flexible query.
	 */
	String getUser();

}
