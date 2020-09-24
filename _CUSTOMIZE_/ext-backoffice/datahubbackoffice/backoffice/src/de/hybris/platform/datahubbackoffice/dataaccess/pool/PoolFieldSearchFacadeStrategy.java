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
package de.hybris.platform.datahubbackoffice.dataaccess.pool;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.dataaccess.DataHubPageable;
import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.DataHubClientException;
import com.hybris.datahub.client.DataPoolClient;
import com.hybris.datahub.dto.pool.PoolData;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class PoolFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<PoolData>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PoolFieldSearchFacadeStrategy.class);

	private DataPoolClient dataPoolClient;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final String code)
	{
		return PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE.equals(code);
	}

	@Override
	public Pageable<PoolData> search(final SearchQueryData searchQueryData)
	{
		return new PoolPageable(searchQueryData);
	}

	@Required
	public void setDataPoolClient(final DataPoolClient client)
	{
		dataPoolClient = client;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private class PoolPageable extends DataHubPageable<PoolData>
	{
		private final SearchQueryData query;

		public PoolPageable(final SearchQueryData queryData)
		{
			super(queryData);
			query = queryData;
		}

		@Override
		protected PagedDataHubResponse<PoolData> getPagedData(final int pageNumber, final int pageSize)
		{
			try
			{
				return isPoolNameConditionUsed() ? getSearchedPool() : dataPoolClient.getAllPools(pageNumber, pageSize);
			}
			catch (final NoDataHubInstanceAvailableException e)
			{
				LOGGER.trace(e.getMessage(), e);
				return new PagedDataHubResponse<>(0, Collections.emptyList());
			}
			catch (final Exception e)
			{
				LOGGER.error(e.getMessage(), e);
				notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
				notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, "datahub.error.connecting.to.server",
						NotificationEvent.Level.FAILURE, e);
				return new PagedDataHubResponse<>(0, Collections.emptyList());
			}
		}

		private boolean isPoolNameConditionUsed()
		{
			final SearchAttributeDescriptor descriptor = new SearchAttributeDescriptor("poolName");
			return query.getAttributes().contains(descriptor) &&
					! StringUtils.isEmpty(query.getAttributeValue(descriptor).toString());
		}

		private PagedDataHubResponse<PoolData> getSearchedPool()
		{
			final String poolName = toString(query.getAttributeValue(new SearchAttributeDescriptor("poolName")));
			return toPage(poolName);
		}

		private PagedDataHubResponse<PoolData> toPage(final String poolName)
		{
			final PoolData pool = findPool(poolName);
			final List<PoolData> pageData = pool != null ? Collections.singletonList(pool) : Collections.emptyList();
			return new PagedDataHubResponse<>(pageData.size(), pageData);
		}

		private PoolData findPool(final String poolName)
		{
			try
			{
				return dataPoolClient.getPool(poolName);
			}
			catch (final DataHubClientException e)
			{
				LOGGER.trace(e.getMessage(), e);
				return null; // not found (exception is thrown when the resource is not found)
			}
		}

		private String toString(final Object o)
		{
			return o != null ? o.toString() : null;
		}
	}
}
