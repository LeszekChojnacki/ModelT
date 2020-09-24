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
package de.hybris.platform.solrfacetsearch.indexer.spi;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;


/**
 * @deprecated since 18.08, functionality is moved to {@link Indexer}
 */
@Deprecated
public interface SolrDocumentFactory
{
	/**
	 * Converts from {@link ItemModel} to {@link SolrInputDocument}.
	 *
	 * @throws FieldValueProviderException
	 *            thrown in case of problem during properties translation
	 */
	SolrInputDocument createInputDocument(ItemModel item, IndexConfig indexConfig, IndexedType indexedType)
			throws FieldValueProviderException;

	/**
	 * Converts from {@link ItemModel} to {@link SolrInputDocument} (for partial updates).
	 *
	 * @throws FieldValueProviderException
	 *            thrown in case of problem during properties translation
	 */
	SolrInputDocument createInputDocument(ItemModel item, IndexConfig indexConfig, IndexedType indexedType,
			Collection<IndexedProperty> indexedProperties) throws FieldValueProviderException;
}
