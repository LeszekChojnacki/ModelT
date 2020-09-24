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
package de.hybris.platform.datahubbackoffice.dataaccess.rawdata;

import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.client.DataItemClient;
import com.hybris.datahub.dto.item.ItemData;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class RawItemFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<ItemData>
{
	private static final String RAW_ITEM_TYPE_CODE = "Raw";
	private static final String DATAHUB_TYPE_CODE_PREFIX = "Datahub_";

	private DataItemClient dataItemClient;

	@Override
	public boolean canHandle(final String s)
	{
		return StringUtils.contains(s, RAW_ITEM_TYPE_CODE);
	}

	@Override
	public Pageable<ItemData> search(final SearchQueryData searchQueryData)
	{
		final String datahubTypeCode = StringUtils.replace(searchQueryData.getSearchType(), DATAHUB_TYPE_CODE_PREFIX, "");
		final List<ItemData> rawItems = findRawItems(datahubTypeCode);
		return new MyPageable(rawItems, datahubTypeCode, searchQueryData.getSearchType());
	}

	private List<ItemData> findRawItems(final String code)
	{
		final List<ItemData> canonicalItems = Lists.newArrayList();
		if (!StringUtils.equals(code, "RawItems"))
		{
			canonicalItems.addAll(dataItemClient.findItems("GLOBAL", code));
		}
		return canonicalItems;
	}

	public void setDataItemClient(final DataItemClient dataItemClient)
	{
		this.dataItemClient = dataItemClient;
	}

	private class MyPageable implements Pageable<ItemData>
	{
		private List<ItemData> rawItems;
		private String datahubTypeCode;
		private String typeCode;

		MyPageable(final List<ItemData> rawItems, final String datahubTypeCode, final String typeCode)
		{
			this.rawItems = rawItems;
			this.typeCode = typeCode;
			this.datahubTypeCode = datahubTypeCode;
		}

		@Override
		public List<ItemData> getCurrentPage()
		{
			return rawItems;
		}

		@Override
		public void refresh()
		{
			rawItems.clear();
			rawItems.addAll(findRawItems(datahubTypeCode));
		}

		@Override
		public List<ItemData> nextPage()
		{
			return Collections.emptyList();
		}

		@Override
		public List<ItemData> previousPage()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean hasNextPage()
		{
			return false;
		}

		@Override
		public boolean hasPreviousPage()
		{
			return false;
		}

		@Override
		public void setPageNumber(final int i)
		{
			// not implemented
		}

		@Override
		public int getPageSize()
		{
			return 10;
		}

		@Override
		public String getTypeCode()
		{
			return typeCode;
		}

		@Override
		public List<ItemData> setPageSize(final int i)
		{
			return Collections.emptyList();
		}

		@Override
		public int getTotalCount()
		{
			return rawItems.size();
		}

		@Override
		public int getPageNumber()
		{
			return 0;
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
		public List<ItemData> getAllResults()
		{
			return rawItems;
		}
	}
}
