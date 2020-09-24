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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.daos.SolrFieldSearchDAO;


public class DefaultSolrFieldSearchDAO implements SolrFieldSearchDAO
{
	private static final String FIND_CONTAINING_PKS = "SELECT {t:pk} FROM {%s AS t} WHERE {t:pk} in (?pks)";

	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<ItemModel> findAll(final String typeCode,final List<Long> itemsPks)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(String.format(FIND_CONTAINING_PKS,typeCode));
		query.addQueryParameter("pks", itemsPks);
		final List<ItemModel> items = flexibleSearchService.<ItemModel> search(query).getResult();

		return CollectionUtils.isNotEmpty(items) ? orderItemsByPkList(items, itemsPks) : Lists.newArrayList();
	}

	protected List<ItemModel> orderItemsByPkList(final List<ItemModel> items, final List<Long> itemsPks)
	{
		final Comparator<ItemModel> pkComparator = (left, right) -> Long.compare(itemsPks.indexOf(left.getPk().getLong()),
				itemsPks.indexOf(right.getPk().getLong()));
		return items.stream().sorted(pkComparator).collect(Collectors.toList());
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}
}
