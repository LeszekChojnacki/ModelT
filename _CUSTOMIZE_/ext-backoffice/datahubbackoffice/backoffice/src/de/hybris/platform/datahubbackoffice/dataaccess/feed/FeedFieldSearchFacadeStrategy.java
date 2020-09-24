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
package de.hybris.platform.datahubbackoffice.dataaccess.feed;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;
import com.hybris.datahub.client.DataFeedClient;
import com.hybris.datahub.dto.feed.FeedData;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

public class FeedFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<FeedData>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FeedFieldSearchFacadeStrategy.class);

	private static final String DATAHUB_FEED_TYPECODE = "Datahub_Feed";
	private DataFeedClient feedClient;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final String code)
	{
		return DATAHUB_FEED_TYPECODE.equals(code);
	}

	@Override
	public Pageable<FeedData> search(final SearchQueryData searchQueryData)
	{
		String feedNameAttribute = null;

		for (final SearchAttributeDescriptor descriptor : searchQueryData.getAttributes())
		{
			if (FeedTypeFacadeStrategy.NAME_ATTR.equals(descriptor.getAttributeName()))
			{
				feedNameAttribute = (String) searchQueryData
						.getAttributeValue(new SearchAttributeDescriptor(FeedTypeFacadeStrategy.NAME_ATTR));
				break;
			}
		}

		try
		{
			final List<FeedData> feeds;
			if (StringUtils.isEmpty(feedNameAttribute))
			{
				feeds = feedClient.getAllDataFeeds();
			}
			else
			{
				feeds = Collections.singletonList(feedClient.getDataFeed(feedNameAttribute));
			}
			return new MyPageable(feeds, searchQueryData.getPageSize());
		}
		catch (final NoDataHubInstanceAvailableException e)
		{
			LOGGER.trace(e.getMessage(), e);
			return new PageableList<>(Collections.emptyList(), 10);
		}
		catch (final Exception e)
		{
			LOGGER.error(e.getMessage(), e);
			notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
			notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, "datahub.error.connecting.to.server",
					NotificationEvent.Level.FAILURE, e);
			return new PageableList<>(Collections.emptyList(), 10);
		}
	}

	@Required
	public void setDataFeedClient(final DataFeedClient client)
	{
		feedClient = client;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private static class MyPageable implements Pageable<FeedData>
	{
		private int pageNumber = 0;
		private final int pageSize;
		private final List<FeedData> feeds;

		public MyPageable(final List<FeedData> feeds, final int pageSize)
		{
			this.feeds = feeds;
			this.pageSize = pageSize;
		}

		@Override
		public List<FeedData> getCurrentPage()
		{
			final int pageEdge = Math.min((pageNumber + 1) * pageSize, feeds.size());
			return feeds.subList(pageNumber * pageSize, pageEdge);
		}

		@Override
		public void refresh()
		{
			// not implemented
		}

		@Override
		public List<FeedData> nextPage()
		{
			return feeds.subList((pageNumber + 1) * pageSize, (pageNumber + 2) * pageSize);
		}

		@Override
		public List<FeedData> previousPage()
		{
			return feeds.subList((pageNumber - 1) * pageSize, pageSize * pageSize);
		}

		@Override
		public boolean hasNextPage()
		{
			return feeds.size() > pageSize;
		}

		@Override
		public boolean hasPreviousPage()
		{
			return pageNumber > 0;
		}

		@Override
		public void setPageNumber(final int i)
		{
			pageNumber = i;
		}

		@Override
		public int getPageSize()
		{
			return 10;
		}

		@Override
		public String getTypeCode()
		{
			return DATAHUB_FEED_TYPECODE;
		}

		@Override
		public List<FeedData> setPageSize(final int i)
		{
			return Collections.emptyList();
		}

		@Override
		public int getTotalCount()
		{
			return feeds.size();
		}

		@Override
		public int getPageNumber()
		{
			return pageNumber;
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
		public List<FeedData> getAllResults()
		{
			return feeds;
		}
	}
}
