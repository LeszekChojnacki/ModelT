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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.io.Serializable;


/**
 * Represents an index.
 */
public interface Index extends Serializable
{
	/**
	 * Returns the name of the index.
	 *
	 * @return the name of the index
	 */
	String getName();

	/**
	 * Returns the facet search configuration.
	 *
	 * @return the the facet search configuration
	 */
	FacetSearchConfig getFacetSearchConfig();

	/**
	 * Returns the indexed type.
	 *
	 * @return the indexed type
	 */
	IndexedType getIndexedType();

	/**
	 * Returns the qualifier.
	 *
	 * @return the qualifier
	 */
	String getQualifier();
}
