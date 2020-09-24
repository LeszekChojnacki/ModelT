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
package de.hybris.platform.datahubbackoffice.dataaccess.composition;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.dataaccess.DataHubPageable;
import de.hybris.platform.datahubbackoffice.dataaccess.OrderedBean;
import de.hybris.platform.datahubbackoffice.dataaccess.search.ComparisonOperatorMap;
import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.datahub.PagedDataHubResponse;
import com.hybris.datahub.client.PoolActionClient;
import com.hybris.datahub.dto.event.CompositionActionData;
import com.hybris.datahub.dto.filter.ComparisonOperator;
import com.hybris.datahub.dto.filter.CompositionFilterDto;
import com.hybris.datahub.dto.pool.PoolData;
import com.hybris.datahub.runtime.domain.CompositionActionStatusType;

import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class CompositionActionFieldSearchFacadeStrategy extends OrderedBean implements OrderedFieldSearchFacadeStrategy<CompositionActionData>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositionActionFieldSearchFacadeStrategy.class);

	private PoolActionClient poolActionClient;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return CompositionActionConstants.DATAHUB_COMPOSITIONS_IN_ERROR.equals(typeCode);
	}

	@Override
	public Pageable<CompositionActionData> search(final SearchQueryData searchQueryData)
	{
		return new CompositionActionPageable(searchQueryData);
	}

	@Required
	public void setPoolActionClient(final PoolActionClient poolActionClient)
	{
		this.poolActionClient = poolActionClient;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private class CompositionActionPageable extends DataHubPageable<CompositionActionData>
	{
		private final CompositionFilterDto compositionFilterDto;

		public CompositionActionPageable(final SearchQueryData searchQueryData)
		{
			super(searchQueryData);
			compositionFilterDto = createQueryFilter(searchQueryData);
		}

		@Override
		protected PagedDataHubResponse<CompositionActionData> getPagedData(final int pageNumber, final int pageSize)
		{
			try
			{
				return poolActionClient.getAllCompositions(pageNumber, pageSize, compositionFilterDto);
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

		private CompositionFilterDto createQueryFilter(final SearchQueryData searchQueryData)
		{
			final CompositionFilterDto.Builder filterBuilder = new CompositionFilterDto.Builder();

			filterBuilder.setStatuses(new String[] {CompositionActionStatusType.COMPLETE_W_ERRORS.name(),
					CompositionActionStatusType.FAILURE.name()});
			for (final SearchAttributeDescriptor attributeDescriptor : searchQueryData.getAttributes())
			{
				final String attribute = attributeDescriptor.getAttributeName();
				if( "status".equals(attribute))
				{
					final String status = (String) searchQueryData.getAttributeValue(attributeDescriptor);
					if (!StringUtils.EMPTY.equals(status))
					{
						filterBuilder.setStatuses(new String[] {status});
					}
				}
				else if("pool".equals(attribute))
				{
					final PoolData pool = (PoolData) searchQueryData.getAttributeValue(attributeDescriptor);
					filterBuilder.setPoolName(pool != null ? pool.getPoolName() : null);
				}
				else if("startTime".equals(attribute))
				{
					final Date startTime = (Date) searchQueryData.getAttributeValue(attributeDescriptor);
					final ComparisonOperator startTimeComparator = ComparisonOperatorMap
							.getLuceneComparator(searchQueryData.getValueComparisonOperator(attributeDescriptor));
					filterBuilder.setStartDate(startTime);
					filterBuilder.setStartDateOperator(startTimeComparator);
				}
				else if("endTime".equals(attribute))
				{
					final Date endTime = (Date) searchQueryData.getAttributeValue(attributeDescriptor);
					final ComparisonOperator endTimeComparator = ComparisonOperatorMap
							.getLuceneComparator(searchQueryData.getValueComparisonOperator(attributeDescriptor));
					filterBuilder.setEndDate(endTime);
					filterBuilder.setEndDateOperator(endTimeComparator);
				}
			}
			return filterBuilder.build();
		}
	}
}
