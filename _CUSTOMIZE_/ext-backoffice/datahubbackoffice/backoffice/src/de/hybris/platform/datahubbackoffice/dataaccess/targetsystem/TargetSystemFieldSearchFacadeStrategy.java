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
package de.hybris.platform.datahubbackoffice.dataaccess.targetsystem;

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
import com.hybris.datahub.client.TargetSystemClient;
import com.hybris.datahub.dto.metadata.TargetSystemData;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class TargetSystemFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<TargetSystemData>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TargetSystemFieldSearchFacadeStrategy.class);

	private TargetSystemClient targetSystemClient;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return "Datahub_TargetSystem".equals(typeCode);
	}

	@Override
	public Pageable<TargetSystemData> search(final SearchQueryData searchQueryData)
	{
		return new TargetSystemPageable(searchQueryData);
	}

	@Required
	public void setTargetSystemClient(final TargetSystemClient targetSystemClient)
	{
		this.targetSystemClient = targetSystemClient;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private class TargetSystemPageable extends DataHubPageable<TargetSystemData>
	{
		TargetSystemPageable(final SearchQueryData queryData)
		{
			super(queryData);
		}

		@Override
		protected PagedDataHubResponse<TargetSystemData> getPagedData(final int pageNumber, final int pageSize)
		{
			try
			{
				return targetSystemClient.getAllTargetSystems(pageNumber, pageSize);
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
	}
}
