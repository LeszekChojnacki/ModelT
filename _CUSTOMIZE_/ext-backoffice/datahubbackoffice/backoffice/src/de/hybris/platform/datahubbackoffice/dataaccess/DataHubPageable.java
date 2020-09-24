/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.dataaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;

/**
 * Base class for any Pageable type returned for a FieldSearchFacadeStrategy implementation
 *
 * @param <T>.
 *           The default page size is 10 if not specified differently through cockpitNG widget configuration
 */
public abstract class DataHubPageable<T> implements Pageable<T>
{
	private int pageSize = 10;
	private int pageNumber = 0;
	private final String typeCode;
	private Pair<Integer, PagedDataHubResponse<T>> currentPage;

	public DataHubPageable(final SearchQueryData searchQueryData)
	{
		Preconditions.checkArgument(searchQueryData != null, "Search query data cannot be null");

		if (searchQueryData.getPageSize() > 0)
		{
			pageSize = searchQueryData.getPageSize();
		}
		typeCode = searchQueryData.getSearchType();
		currentPage = new ImmutablePair<>(-1, null);
	}

	@Override
	public List<T> getCurrentPage()
	{
		return getItems(pageNumber, pageSize);
	}

	@Override
	public List<T> nextPage()
	{
		pageNumber += 1;
		return getItems(pageNumber, pageSize);

	}

	@Override
	public List<T> previousPage()
	{
		pageNumber -= 1;
		return getItems(pageNumber, pageSize);
	}

	@Override
	public boolean hasNextPage()
	{
		return (pageNumber + 1) * pageSize < currentPage.getValue().getTotalCount();
	}

	@Override
	public boolean hasPreviousPage()
	{
		return pageNumber > 0;
	}

	@Override
	public int getTotalCount()
	{
		int ret = 0;

		if (!(currentPage.getKey().equals(pageNumber)))
		{
			currentPage = new ImmutablePair<>(pageNumber, getPagedData(pageNumber, pageSize));
		}
		if (currentPage.getValue() != null)
		{
			ret = Integer.parseInt(Long.toString(currentPage.getValue().getTotalCount()));
		}
		return ret;
	}

	@Override
	public void refresh()
	{
		// not implemented
	}

	@Override
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public List<T> setPageSize(final int pageSize)
	{
		return Collections.emptyList();
	}

	@Override
	public int getPageNumber()
	{
		return pageNumber;
	}

	@Override
	public void setPageNumber(final int pageNo)
	{
		pageNumber = pageNo;
	}

	@Override
	public SortData getSortData()
	{
		return null;
	}

	@Override
	public void setSortData(final SortData sortData)
	{
		// not implemented
	}

	@Override
	public List<T> getAllResults()
	{
		return Collections.emptyList();
	}

	@Override
	public String getTypeCode()
	{
		return typeCode;
	}

	protected abstract PagedDataHubResponse<T> getPagedData(final int pageNumber, final int pageSize);

	private List<T> getItems(final int pageNumber, final int pageSize)
	{
		if (!currentPage.getKey().equals(pageNumber))
		{
			currentPage = new ImmutablePair<>(pageNumber, getPagedData(pageNumber, pageSize));
		}
		if (currentPage.getValue() != null)
		{
			return currentPage.getValue().getItems();
		}
		return new ArrayList<>();
	}
}
