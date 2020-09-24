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

/**
 * Registry for getting information on indexed property type.
 */
public interface SolrIndexedPropertyTypeRegistry
{
	/**
	 * Returns information on indexed property type such as allowed query operators, is eligible for facet and Java type
	 *
	 * @param propertyType
	 *           - indexed property type
	 * @return information on indexed property type
	 */
	IndexedPropertyTypeInfo getIndexPropertyTypeInfo(final String propertyType);
}
