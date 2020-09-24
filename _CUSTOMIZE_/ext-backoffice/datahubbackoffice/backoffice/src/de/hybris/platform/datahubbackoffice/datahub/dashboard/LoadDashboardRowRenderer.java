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
import com.hybris.datahub.dto.count.RawItemStatusCountData;
import com.hybris.datahub.dto.pool.PoolData;

public class LoadDashboardRowRenderer extends AbstractDashboardRowRenderer implements DashboardRowRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LoadDashboardRowRenderer.class);
	private static final String RAW_ITEMS_COLUMN_LABEL = "datahub.dashboard.rawItems";

	@Override
	public void renderDashboardRow(final Component parent, final WidgetInstanceManager context)
	{
		final PoolData contextPool = extractPoolDataFromModel(context);
		createDashboardColumn(parent, RAW_ITEMS_COLUMN_LABEL);
		final RawItemStatusCountData count = getRawItemCountByStatus(contextPool);
		setDashboardColumns(parent, count);
		createSpacer(parent);
	}

	private void setDashboardColumns(final Component parent, final AbstractItemStatusCountData count)
	{
		setDashboardColumn(parent, count, "PENDING");
		setDashboardColumn(parent, count, "PROCESSED");
		setDashboardColumn(parent, count, "IGNORED");
	}

	@Override
	protected int getNumberOfColumnsToShowInCounts()
	{
		return 4;
	}

	@Override
	protected void setCountForLabel(final String labelValue, final Label label, final AbstractItemStatusCountData count)
	{
		final RawItemStatusCountData rawItemStatusCountData = (RawItemStatusCountData) count;
		switch (labelValue)
		{
			case "IGNORED":
				label.setValue(String.format("%s", rawItemStatusCountData.getIgnoredCount()));
				break;
			case "PENDING":
				label.setValue(String.format("%s", rawItemStatusCountData.getPendingCount()));
				break;
			case "PROCESSED":
				label.setValue(String.format("%s", rawItemStatusCountData.getProcessedCount()));
				break;
			default:
				break;
		}
	}

	private RawItemStatusCountData getRawItemCountByStatus(final PoolData contextPool)
	{
		try
		{
			return contextPool == null ? statusCountClient.getRawItemStatusCount()
					: statusCountClient.getRawItemStatusCount(contextPool.getPoolName());
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

	private static RawItemStatusCountData createErrorCount()
	{
		final RawItemStatusCountData statusCount = new RawItemStatusCountData();
		statusCount.setIgnoredCount(-1L);
		statusCount.setPendingCount(-1L);
		statusCount.setProcessedCount(-1L);
		return statusCount;
	}
}
