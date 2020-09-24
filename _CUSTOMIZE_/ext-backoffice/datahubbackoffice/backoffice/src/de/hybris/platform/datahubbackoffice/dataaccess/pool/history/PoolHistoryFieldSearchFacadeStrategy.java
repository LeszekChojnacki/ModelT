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
package de.hybris.platform.datahubbackoffice.dataaccess.pool.history;

import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;

import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.client.PoolActionClient;
import com.hybris.datahub.dto.event.PoolActionData;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;

public class PoolHistoryFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<PoolActionData>
{
	private static final String DATAHUB_POOL_HISTORY_TYPECODE = "Datahub_DataHubPoolAction";
	private PoolActionClient poolActionClient;

	private static String getPoolName(final SearchQueryData searchQueryData)
	{
		final SearchAttributeDescriptor descriptor = new SearchAttributeDescriptor("pool", 0);
		if (searchQueryData.getAttributes().contains(descriptor))
		{
			final PoolData poolData = (PoolData) searchQueryData.getAttributeValue(descriptor);
			if (poolData != null)
			{
				return poolData.getPoolName();
			}
		}

		return "";
	}

	@Override
	public boolean canHandle(final String code)
	{
		return StringUtils.equals(code, DATAHUB_POOL_HISTORY_TYPECODE);
	}

	@Override
	public Pageable<PoolActionData> search(final SearchQueryData searchQueryData)
	{

		final String poolname = getPoolName(searchQueryData);
		final List<PoolActionData> objects = Lists.newArrayList();
		if (StringUtils.isNotEmpty(poolname))
		{
			objects.addAll(poolActionClient.getAllPoolActions(poolname));
		}

		return new MyPageable(objects);
	}

	@Required
	public void setPoolActionClient(final PoolActionClient client)
	{
		poolActionClient = client;
	}

	private static class MyPageable implements Pageable<PoolActionData>
	{
		final List<PoolActionData> objects;

		MyPageable(final List<PoolActionData> objects)
		{
			this.objects = objects;
		}

		@Override
		public List<PoolActionData> getCurrentPage()
		{
			return objects;
		}

		@Override
		public void refresh()
		{
			// not implemented
		}

		@Override
		public List<PoolActionData> nextPage()
		{
			return Collections.emptyList();
		}

		@Override
		public List<PoolActionData> previousPage()
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
			return DATAHUB_POOL_HISTORY_TYPECODE;
		}

		@Override
		public List<PoolActionData> setPageSize(final int i)
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
		public List<PoolActionData> getAllResults()
		{
			return objects;
		}
	}
}
