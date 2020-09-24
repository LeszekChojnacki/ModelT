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
package de.hybris.platform.solrfacetsearch.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSortField;
import de.hybris.platform.solrfacetsearch.model.SolrSortFieldModel;


public class DefaultIndexedTypeSortFieldPopulator implements Populator<SolrSortFieldModel, IndexedTypeSortField>
{
	@Override
	public void populate(final SolrSortFieldModel source, final IndexedTypeSortField target)
	{
		target.setFieldName(source.getFieldName());
		target.setAscending(source.isAscending());
	}
}
