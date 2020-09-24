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
package de.hybris.platform.datahubbackoffice.datahub.dashboard;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.datahub.dto.count.AbstractItemStatusCountData;
import com.hybris.datahub.dto.count.CanonicalPublicationStatusCountData;
import com.hybris.datahub.dto.pool.PoolData;

public class PublicationDashboardRowRenderer extends AbstractDashboardRowRenderer implements DashboardRowRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PublicationDashboardRowRenderer.class);
	private static final String PUBLICATION_ITEMS_COLUMN_LABEL = "datahub.dashboard.canonicalPublications";

	@Override
	public void renderDashboardRow(final Component parent, final WidgetInstanceManager context)
	{
		final PoolData contextPool = extractPoolDataFromModel(context);
		createDashboardColumn(parent, PUBLICATION_ITEMS_COLUMN_LABEL);
		final CanonicalPublicationStatusCountData count = getCountByStatus(contextPool);
		setDashboardColumns(parent, count);
		createSpacer(parent);
	}

	private void setDashboardColumns(final Component parent, final AbstractItemStatusCountData count)
	{
		setDashboardColumn(parent, count, "SUCCESS");
		setDashboardColumn(parent, count, "IGNORED");
		setDashboardColumn(parent, count, "INTERNAL_ERROR");
		setDashboardColumn(parent, count, "EXTERNAL_ERROR");
	}

	@Override
	protected int getNumberOfColumnsToShowInCounts()
	{
		return 5;
	}

	@Override
	protected void setCountForLabel(final String labelValue, final Label label, final AbstractItemStatusCountData count)
	{
		final CanonicalPublicationStatusCountData countData = (CanonicalPublicationStatusCountData) count;
		switch (labelValue)
		{
			case "IGNORED":
				label.setValue(String.format("%s", countData.getIgnoredCount()));
				break;
			case "INTERNAL_ERROR":
				label.setValue(String.format("%s", countData.getInternalErrorCount()));
				break;
			case "EXTERNAL_ERROR":
				label.setValue(String.format("%s", countData.getExternalErrorCount()));
				break;
			case "SUCCESS":
				label.setValue(String.format("%s", countData.getSuccessCount()));
				break;
			default:
				break;
		}
	}

	protected CanonicalPublicationStatusCountData getCountByStatus(final PoolData contextPool)
	{
		try
		{
			return contextPool == null ? statusCountClient.getCanonicalPublicationStatusCount()
					: statusCountClient.getCanonicalPublicationStatusCount(contextPool.getPoolName());
		}
		catch (final NoDataHubInstanceAvailableException e)
		{
			LOGGER.trace(e.getMessage(), e);
			return createErrorCount();
		}
		catch( final Exception e)
		{
			LOGGER.error(e.getMessage(), e);
			notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
			notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, "datahub.error.connecting.to.server",
					NotificationEvent.Level.FAILURE, e);
			return createErrorCount();
		}
	}

	private static CanonicalPublicationStatusCountData createErrorCount()
	{
		final CanonicalPublicationStatusCountData statusCount = new CanonicalPublicationStatusCountData();
		statusCount.setExternalErrorCount(-1L);
		statusCount.setIgnoredCount(-1L);
		statusCount.setSuccessCount(-1L);
		statusCount.setInternalErrorCount(-1L);
		return statusCount;
	}
}
