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
package de.hybris.platform.adaptivesearch.daos.impl;

import de.hybris.platform.adaptivesearch.daos.AsSearchProfileActivationSetDao;
import de.hybris.platform.adaptivesearch.model.AsSearchProfileActivationSetModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Default implementation of {@link AsSearchProfileActivationSetDao}.
 */
public class DefaultAsSearchProfileActivationSetDao extends AbstractAsGenericDao<AsSearchProfileActivationSetModel>
		implements AsSearchProfileActivationSetDao
{
	protected static final String BASE_QUERY = "SELECT {" + AsSearchProfileActivationSetModel.PK + "} FROM {"
			+ AsSearchProfileActivationSetModel._TYPECODE + "} WHERE ";

	/**
	 * Creates DAO for {@link AsSearchProfileActivationSetModel}.
	 */
	public DefaultAsSearchProfileActivationSetDao()
	{
		super(AsSearchProfileActivationSetModel._TYPECODE);
	}

	@Override
	public List<AsSearchProfileActivationSetModel> findAllSearchProfileActivationSets()
	{
		return find();
	}

	@Override
	public Optional<AsSearchProfileActivationSetModel> findSearchProfileActivationSetByIndexType(
			final CatalogVersionModel catalogVersion, final String indexType)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map parameters = new HashMap();

		appendClause(query, parameters, AsSearchProfileActivationSetModel.CATALOGVERSION, catalogVersion);
		appendAndClause(query);
		appendClause(query, parameters, AsSearchProfileActivationSetModel.INDEXTYPE, indexType);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		final List<AsSearchProfileActivationSetModel> searchProfileActivationsSets = getFlexibleSearchService()
				.<AsSearchProfileActivationSetModel> search(searchQuery).getResult();

		return searchProfileActivationsSets.isEmpty() ? Optional.empty() : Optional.of(searchProfileActivationsSets.get(0));
	}
}