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

import de.hybris.platform.adaptivesearch.daos.AsSearchProfileDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link AsSearchProfileDao}.
 */
public class DefaultAsSearchProfileDao extends AbstractAsGenericDao<AbstractAsSearchProfileModel> implements AsSearchProfileDao
{
	protected static final String BASE_QUERY = "SELECT {" + AbstractAsSearchProfileModel.PK + "} FROM {"
			+ AbstractAsSearchProfileModel._TYPECODE + "} WHERE";

	/**
	 * Creates DAO for {@link AbstractAsSearchProfileModel}.
	 */
	public DefaultAsSearchProfileDao()
	{
		super(AbstractAsSearchProfileModel._TYPECODE);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T>  findAllSearchProfiles()
	{
		return (List<T>) find();
	}

	public <T extends AbstractAsSearchProfileModel> List<T> findSearchProfilesByIndexTypesAndCatalogVersions(final List<String> indexTypes,
		final List<CatalogVersionModel> catalogVersions)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap();

		if (CollectionUtils.isNotEmpty(indexTypes))
		{
			appendClause(query, parameters, AbstractAsSearchProfileModel.INDEXTYPE, indexTypes);
		}

		if (CollectionUtils.isNotEmpty(indexTypes) && CollectionUtils.isNotEmpty(catalogVersions))
		{
			appendAndClause(query);
		}

		if (CollectionUtils.isNotEmpty(catalogVersions))
		{
			appendClause(query, parameters, AbstractAsSearchProfileModel.CATALOGVERSION, catalogVersions);
		}

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		return getFlexibleSearchService().<T>search(searchQuery).getResult();
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T>  findSearchProfilesByCatalogVersion(final CatalogVersionModel catalogVersion)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap<>();

		appendClause(query, parameters, AbstractAsSearchProfileModel.CATALOGVERSION, catalogVersion);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		return getFlexibleSearchService().<T> search(searchQuery).getResult();
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> Optional<T>  findSearchProfileByCode(final CatalogVersionModel catalogVersion,
																						 final String code)
	{
		final StringBuilder query = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap<>();

		appendClause(query, parameters, AbstractAsSearchProfileModel.CATALOGVERSION, catalogVersion);
		appendAndClause(query);
		appendClause(query, parameters, AbstractAsSearchProfileModel.CODE, code);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString(), parameters);

		final List<T> searchProfiles = getFlexibleSearchService().<T> search(searchQuery).getResult();

		return searchProfiles.isEmpty() ? Optional.empty() : Optional.of(searchProfiles.get(0));
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> List<T> getSearchProfiles(final String query, final Map<String, Object> filters)
	{
		final StringBuilder fsQuery = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap();

		if(StringUtils.isNotBlank(query))
		{
			appendLikeClause(fsQuery, parameters, AbstractAsSearchProfileModel.CODE, "%" + query + "%");
		}

		filters.forEach( (key, value) -> {
			if(MapUtils.isNotEmpty(parameters)){
				appendAndClause(fsQuery);
			}
			appendClause(fsQuery, parameters, key, value);
		});

		appendOrderByClause(fsQuery, AbstractAsSearchProfileModel.CODE, true);

		return queryList(fsQuery.toString(), filters);
	}

	@Override
	public <T extends AbstractAsSearchProfileModel> SearchPageData<T> getSearchProfiles(final String query, final Map<String, Object> filters,
     	final SearchPageData<?> pagination)
	{
		final StringBuilder fsQuery = new StringBuilder(BASE_QUERY);
		final Map<String, Object> parameters = new HashMap();

		if(StringUtils.isNotBlank(query))
		{
			appendLikeClause(fsQuery, parameters, AbstractAsSearchProfileModel.CODE, "%" + query + "%");
		}

		filters.forEach( (key, value) -> {
			if(MapUtils.isNotEmpty(parameters)){
				appendAndClause(fsQuery);
			}
			appendClause(fsQuery, parameters, key, value);
		});

		appendOrderByClause(fsQuery, AbstractAsSearchProfileModel.CODE, true);

		return queryList(fsQuery.toString(), parameters, pagination.getPagination());
	}
}
