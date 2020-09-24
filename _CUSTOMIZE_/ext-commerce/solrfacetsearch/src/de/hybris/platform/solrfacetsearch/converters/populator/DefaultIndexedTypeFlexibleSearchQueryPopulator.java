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
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexerQueryModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexerQueryParameterModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;


public class DefaultIndexedTypeFlexibleSearchQueryPopulator
		implements Populator<SolrIndexerQueryModel, IndexedTypeFlexibleSearchQuery>
{

	private static final String ANONYMOUS = "anonymous";

	@Override
	public void populate(final SolrIndexerQueryModel source, final IndexedTypeFlexibleSearchQuery target)
	{
		target.setQuery(source.getQuery());
		final UserModel user = source.getUser();
		target.setUserId(user == null ? ANONYMOUS : user.getUid());
		target.setInjectCurrentDate(source.isInjectCurrentDate());
		target.setInjectCurrentTime(source.isInjectCurrentTime());
		target.setInjectLastIndexTime(source.isInjectLastIndexTime());
		target.setParameters(initializeFSQParameters(source.getSolrIndexerQueryParameters()));
		target.setParameterProviderId(source.getParameterProvider());
		if (source.getType() != null)
		{
			target.setType(IndexOperation.valueOf(source.getType().toString()));
		}
	}

	/**
	 * Initializes the Flexible Search Query parameters for the {@link IndexedTypeFlexibleSearchQuery}. They are populated
	 * with the static parameters now, but the runtime parameters should be populated at index time.
	 *
	 * @param list
	 */
	protected Map<String, Object> initializeFSQParameters(final List<SolrIndexerQueryParameterModel> list)
	{
		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(list))
		{
			for (final SolrIndexerQueryParameterModel parameterModel : list)
			{
				parameters.put(parameterModel.getName(), parameterModel.getValue());
			}
		}
		return parameters;
	}
}
