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
package de.hybris.platform.solrfacetsearch.provider.impl;

/**
 * Provides constant for parsing and building solr fields names.
 */
public class SolrFieldNameConstants
{
	public static final String DEFAULT_SEPARATOR = "_";
	public static final String MV_TYPE = "mv";
	public static final String TEXT_TYPE = "text";
	public static final String SORTABLE = "sortable";
	public static final String RANGE_TYPE = "string";
	public static final String SPELLCHECK_FIELD = "spellcheck";
	public static final String AUTOSUGGEST_FIELD = "autosuggest";

	private SolrFieldNameConstants() {
	}
}
