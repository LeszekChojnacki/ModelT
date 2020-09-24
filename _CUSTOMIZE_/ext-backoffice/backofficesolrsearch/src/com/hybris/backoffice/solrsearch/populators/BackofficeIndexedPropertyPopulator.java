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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;


/**
 * Populates indexed properties added by backofficesolrsearch.
 */
public class BackofficeIndexedPropertyPopulator implements Populator<SolrIndexedPropertyModel, IndexedProperty>
{
	@Override
	public void populate(final SolrIndexedPropertyModel solrIndexedPropertyModel, final IndexedProperty indexedProperty)
			throws ConversionException
	{
		indexedProperty.setBackofficeDisplayName(solrIndexedPropertyModel.getBackofficeDisplayName());
	}
}
