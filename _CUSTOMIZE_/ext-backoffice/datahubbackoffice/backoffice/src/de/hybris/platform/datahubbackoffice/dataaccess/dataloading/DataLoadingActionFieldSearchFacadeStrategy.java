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
package de.hybris.platform.datahubbackoffice.dataaccess.dataloading;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.dataaccess.DataHubPageable;
import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.DataFeedClient;
import com.hybris.datahub.dto.dataloading.DataLoadingActionData;
import com.hybris.datahub.dto.filter.DataLoadingFilterDto;
import com.hybris.datahub.runtime.domain.DataLoadingActionStatusType;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class DataLoadingActionFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<DataLoadingActionData>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DataLoadingActionFieldSearchFacadeStrategy.class);

	private DataFeedClient dataFeedClient;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return DataLoadingActionConstants.DATA_LOADING_ACTIONS_IN_ERROR_TYPE_CODE.equals(typeCode);
	}

	@Override
	public Pageable<DataLoadingActionData> search(final SearchQueryData searchQueryData)
	{
		return new DataLoadingActionPageable(searchQueryData);
	}

	@Required
	public void setDataFeedClient(final DataFeedClient dataFeedClient)
	{
		this.dataFeedClient = dataFeedClient;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private class DataLoadingActionPageable extends DataHubPageable<DataLoadingActionData>
	{
		private final DataLoadingFilterDto dataLoadingFilterDto;

		public DataLoadingActionPageable(final SearchQueryData searchQueryData)
		{
			super(searchQueryData);
			dataLoadingFilterDto = createQueryFilter();
		}

		@Override
		protected PagedDataHubResponse<DataLoadingActionData> getPagedData(final int pageNumber, final int pageSize)
		{
			try
			{
				return dataFeedClient.getDataLoadingActions(pageNumber, pageSize, dataLoadingFilterDto);
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

		private DataLoadingFilterDto createQueryFilter()
		{
			final DataLoadingFilterDto.Builder filterBuilder = new DataLoadingFilterDto.Builder();

			filterBuilder.setStatuses(new String[] {DataLoadingActionStatusType.FAILURE.toString()});

			return filterBuilder.build();
		}
	}
}
