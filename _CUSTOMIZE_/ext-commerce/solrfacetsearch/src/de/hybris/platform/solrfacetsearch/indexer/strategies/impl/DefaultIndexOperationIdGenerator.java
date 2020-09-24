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
package de.hybris.platform.solrfacetsearch.indexer.strategies.impl;

import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexOperationIdGenerator;
import de.hybris.platform.solrfacetsearch.solr.Index;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link IndexOperationIdGenerator}.
 */
public class DefaultIndexOperationIdGenerator implements IndexOperationIdGenerator
{
	private KeyGenerator indexOperationIdKeyGenerator;

	public KeyGenerator getIndexOperationIdKeyGenerator()
	{
		return indexOperationIdKeyGenerator;
	}

	@Required
	public void setIndexOperationIdKeyGenerator(final KeyGenerator indexOperationIdKeyGenerator)
	{
		this.indexOperationIdKeyGenerator = indexOperationIdKeyGenerator;
	}

	@Override
	public long generate(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final Index index)
	{
		final long timestamp = System.currentTimeMillis();
		final long sequence = Long.parseLong((String) indexOperationIdKeyGenerator.generate());

		return (timestamp << 16) + sequence;
	}
}
