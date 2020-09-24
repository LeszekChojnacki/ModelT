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
package de.hybris.platform.solrfacetsearch.loader;

import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrDocument;


/**
 * Provides transition from SOLR understandable indexedItems format to predefined model types.
 * 
 *
 */
public interface ModelLoader<T>
{
	/**
	 * Retrieves information on models that are contained within the collection of {@link SolrDocument}s
	 */
	List<T> loadModels(Collection<SolrDocument> documents) throws ModelLoadingException;

	/**
	 * Retrieves codes of items contained within the collection of {@link SolrDocument}s
	 * 
	 * @param documents
	 * @return Codes of items as Strings
	 * @throws ModelLoadingException
	 */
	List<String> loadCodes(final Collection<SolrDocument> documents) throws ModelLoadingException;

}
