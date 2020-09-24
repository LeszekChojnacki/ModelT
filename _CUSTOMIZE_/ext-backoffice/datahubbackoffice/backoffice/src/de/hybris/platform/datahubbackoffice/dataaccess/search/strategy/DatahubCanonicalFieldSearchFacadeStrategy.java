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

package de.hybris.platform.datahubbackoffice.dataaccess.search.strategy;

import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.client.DataItemClient;
import com.hybris.datahub.dto.filter.DataItemFilterDto;
import com.hybris.datahub.dto.item.ItemData;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;

public class DatahubCanonicalFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<ItemData>
{
	private static final String DATAHUB_CANONICAL = "Canonical";
	private static final String DATAHUB_TYPECODE_PREFIX = "Datahub_";
	private static final String DATAHUB_CANONICAL_ITEM = "Datahub_CanonicalItem";

	private DataItemClient dataItemClient;

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.contains(code, DATAHUB_CANONICAL);
	}

	private static <T> T getAttributeValue(final String attrName, final T defaultValue, final SearchQueryData searchQueryData)
	{
		T ret = defaultValue;

		final SearchAttributeDescriptor descriptor = new SearchAttributeDescriptor(attrName, 0);
		if (searchQueryData.getAttributes().contains(descriptor))
		{
			ret = (T) searchQueryData.getAttributeValue(descriptor);
		}
		return ret;
	}

	@Override
	public Pageable<ItemData> search(final SearchQueryData searchQueryData)
	{
		final PoolData datahubPool = getAttributeValue("pool", null, searchQueryData);
		final String datahubPoolName = datahubPool != null ? datahubPool.getPoolName() : "GLOBAL";
		final String status = getAttributeValue("status", StringUtils.EMPTY, searchQueryData);
		final String typeCode = getAttributeValue("type", StringUtils.EMPTY, searchQueryData);
		final String datahubTypeCode = String.format("%s%s", DATAHUB_TYPECODE_PREFIX, typeCode);

		final List<ItemData> objects = findCanonicalItems(datahubPoolName, typeCode, status);

		return new MyPageable(objects, searchQueryData.getPageSize(), typeCode, datahubTypeCode, datahubPoolName, status);
	}

	private List<ItemData> findCanonicalItems(final String pool, final String itemType, final String status)
	{
		final List<ItemData> canonicalItems = Lists.newArrayList();
		if (!DATAHUB_CANONICAL_ITEM.equals(itemType))
		{
			if (StringUtils.isBlank(status))
			{
				canonicalItems.addAll(dataItemClient.findItems(pool, itemType));
			}
			else
			{
				final DataItemFilterDto filter = new DataItemFilterDto.Builder().setStatuses(new String[] {status}).build();
				canonicalItems.addAll(dataItemClient.findItems(pool, itemType, filter));
			}
		}
		return canonicalItems;
	}

	@Required
	public void setDataItemClient(final DataItemClient dataItemClient)
	{
		this.dataItemClient = dataItemClient;
	}

	private class MyPageable implements Pageable<ItemData>
	{
		private List<ItemData> objects;
		private int pageSize;
		private String datahubTypeCode;
		private String typeCode;
		private String datahubPoolName;
		private String status;

		MyPageable(final List<ItemData> objects, final int pageSize, final String typeCode, final String datahubTypeCode,
				   final String datahubPoolName, final String status)
		{
			this.objects = objects;
			this.pageSize = pageSize;
			this.typeCode = typeCode;
			this.datahubTypeCode = datahubTypeCode;
			this.datahubPoolName = datahubPoolName;
			this.status = status;
		}

		@Override
		public List<ItemData> getCurrentPage()
		{
			return objects;
		}

		@Override
		public void refresh()
		{
			objects.clear();
			objects.addAll(findCanonicalItems(datahubPoolName, typeCode, status));
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
			return pageSize;
		}

		@Override
		public String getTypeCode()
		{
			return datahubTypeCode;
		}

		@Override
		public List<ItemData> setPageSize(final int i)
		{
			return Collections.emptyList();
		}

		@Override
		public int getTotalCount()
		{
			return objects.size();
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
			return objects;
		}
	}
}
