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
package com.hybris.backoffice.solrsearch.daos.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.daos.SolrModifiedItemDAO;
import com.hybris.backoffice.solrsearch.enums.SolrItemModificationType;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;

/**
 * @deprecated since 1808 {@link SolrModifiedItemModel} is no longer used in solr index update strategy
 */
@Deprecated
public class DefaultSolrModifiedItemDAO implements SolrModifiedItemDAO
{

	protected static final String FIND_REMOVED_ITEMS_BY_MODIFICATION_TYPE = String.format(
			"SELECT {pk} from {%s} WHERE  {%s}=?modificationType",
			SolrModifiedItemModel._TYPECODE,
			SolrModifiedItemModel.MODIFICATIONTYPE);

	private FlexibleSearchService flexibleSearchService;

	@Override
	public Collection<SolrModifiedItemModel> findByModificationType(final SolrItemModificationType modificationType)
	{
		FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_REMOVED_ITEMS_BY_MODIFICATION_TYPE);
		query.setResultClassList(Arrays.asList(new Class[] { SolrModifiedItemModel.class }));
		query.addQueryParameter("modificationType", modificationType);

		return executeQuery(query);
	}

	protected List<SolrModifiedItemModel> executeQuery(final FlexibleSearchQuery query)
	{
		final List<SolrModifiedItemModel> items = flexibleSearchService.<SolrModifiedItemModel> search(query).getResult();
		return CollectionUtils.isNotEmpty(items) ? items : Collections.emptyList();
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}
}
