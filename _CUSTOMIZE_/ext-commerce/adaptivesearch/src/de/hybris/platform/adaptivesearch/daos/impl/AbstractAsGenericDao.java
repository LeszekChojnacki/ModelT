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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Base generic DAO that allows to build queries for null values.
 */
public abstract class AbstractAsGenericDao<T extends ItemModel> extends DefaultGenericDao<T>
{
	public AbstractAsGenericDao(final String typecode)
	{
		super(typecode);
	}

	protected StringBuilder createQuery(final String typeCode)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT {").append(ItemModel.PK).append("} FROM {").append(typeCode).append('}');
		return builder;
	}

	protected void appendWhereClause(final StringBuilder query)
	{
		query.append(" WHERE");
	}

	protected void appendAndClause(final StringBuilder query)
	{
		query.append(" AND");
	}

	protected void appendClause(final StringBuilder query, final Map<String, Object> parameters, final String paramName,
			final Object paramValue)
	{
		if (paramValue == null)
		{
			query.append('{').append(paramName).append("} IS NULL");
		}
		else if (paramValue instanceof Collection)
		{
			query.append(" {").append(paramName).append("} in (?").append(paramName).append(")");
			parameters.put(paramName, paramValue);
		}
		else
		{
			query.append(" {").append(paramName).append("}=?").append(paramName);
			parameters.put(paramName, paramValue);
		}
	}

	protected void appendLikeClause(final StringBuilder query, final Map<String, Object> parameters, final String paramName,
			final String paramValue)
	{
		if (paramValue == null)
		{
			query.append('{').append(paramName).append("} IS NULL");
		}
		else
		{
			query.append(" LOWER({").append(paramName).append("}) LIKE LOWER(?").append(paramName).append(")");
			parameters.put(paramName, paramValue);
		}
	}

	protected void appendOrderByClause(final StringBuilder query, final String paramName, final boolean asc)
	{
		query.append(" ORDER BY{").append(paramName).append("} ").append(asc ? "ASC" : "DESC");
	}

	public FlexibleSearchQuery buildQuery(final String query, final Map<String, Object> params)
	{
		final FlexibleSearchQuery result = new FlexibleSearchQuery(query);
		if (params != null)
		{
			result.addQueryParameters(params);
		}
		return result;
	}

	public FlexibleSearchQuery buildQuery(final String query, final Map<String, Object> params, final PaginationData pagination)
	{
		final FlexibleSearchQuery result = buildQuery(query, params);
		final int start = pagination.getCurrentPage() * pagination.getPageSize();
		final int count = pagination.getPageSize();

		result.setStart(start);
		result.setCount(count);
		result.setNeedTotal(pagination.isNeedsTotal());
		return result;
	}

	protected <T> List<T> queryList(final String baseQuery, final Map<String, Object> params)
	{
		final FlexibleSearchQuery fsQuery = buildQuery(baseQuery, params);
		final SearchResult<T> searchResult = getFlexibleSearchService().search(fsQuery);
		final List<T> result = searchResult.getResult();
		return result == null ? Collections.emptyList() : result;
	}

	protected <T> SearchPageData<T> queryList(final String baseQuery, final Map<String, Object> params,
			final PaginationData pagination)
	{
		final FlexibleSearchQuery fsQuery = buildQuery(baseQuery, params, pagination);
		final SearchResult<T> searchResult = getFlexibleSearchService().search(fsQuery);

		return buildSearchPageData(searchResult, pagination);
	}


	protected <T> SearchPageData<T> buildSearchPageData(final SearchResult<T> searchResult, final PaginationData requestPagination)
	{
		final SearchPageData<T> result = new SearchPageData<>();
		final PaginationData pagination = buildPagination(searchResult);
		pagination.setNeedsTotal(requestPagination.isNeedsTotal());
		if (!requestPagination.isNeedsTotal())
		{
			pagination.setNumberOfPages(0);
			pagination.setTotalNumberOfResults(0);
		}

		result.setResults(searchResult.getResult());
		result.setPagination(pagination);
		result.setSorts(Collections.emptyList());
		return result;
	}

	protected PaginationData buildPagination(final SearchResult<?> search)
	{
		final PaginationData result = new PaginationData();

		result.setTotalNumberOfResults(search.getTotalCount());
		result.setPageSize(search.getCount());

		final int reqCount = search.getRequestedCount();
		if (reqCount > 0)
		{
			result.setCurrentPage(search.getRequestedStart() / search.getRequestedCount());
		}
		else
		{
			result.setCurrentPage(0);
		}
		final double totalPages = Math.ceil(1.0 * search.getTotalCount() / search.getRequestedCount());
		if (Double.isFinite(totalPages))
		{
			result.setNumberOfPages((int) totalPages);
		}
		else
		{
			result.setNumberOfPages(0);
		}
		return result;
	}
}
