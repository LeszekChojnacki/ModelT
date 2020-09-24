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
import com.hybris.datahub.dto.count.CanonicalItemStatusCountData;
import com.hybris.datahub.dto.pool.PoolData;

public class ComposeDashboardRowRenderer extends AbstractDashboardRowRenderer implements DashboardRowRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ComposeDashboardRowRenderer.class);
	private static final String CANONICAL_ITEMS_COLUMN_LABEL = "datahub.dashboard.canonicalItems";

	@Override
	public void renderDashboardRow(final Component parent, final WidgetInstanceManager context)
	{
		final PoolData contextPool = extractPoolDataFromModel(context);
		createDashboardColumn(parent, CANONICAL_ITEMS_COLUMN_LABEL);
		final CanonicalItemStatusCountData count = getCanonicalItemCountByStatus(contextPool);
		setDashboardColumnCounts(parent, count);
		createSpacer(parent);
	}

	private void setDashboardColumnCounts(final Component parent, final CanonicalItemStatusCountData count)
	{
		setDashboardColumn(parent, count, "SUCCESS");
		setDashboardColumn(parent, count, "ERROR");
		setDashboardColumn(parent, count, "ARCHIVED");
		setDashboardColumn(parent, count, "DELETED");
	}

	@Override
	protected int getNumberOfColumnsToShowInCounts()
	{
		return 5;
	}

	@Override
	protected void setCountForLabel(final String labelValue, final Label label, final AbstractItemStatusCountData count)
	{
		final CanonicalItemStatusCountData canonicalItemStatusCountData = (CanonicalItemStatusCountData) count;
		switch (labelValue)
		{
			case "SUCCESS":
				label.setValue(String.format("%s", canonicalItemStatusCountData.getSuccessCount()));
				break;
			case "ERROR":
				label.setValue(String.format("%s", canonicalItemStatusCountData.getErrorCount()));
				break;
			case "ARCHIVED":
				label.setValue(String.format("%s", canonicalItemStatusCountData.getArchivedCount()));
				break;
			case "DELETED":
				label.setValue(String.format("%s", canonicalItemStatusCountData.getDeletedCount()));
				break;
			default:
				break;
		}
	}

	private CanonicalItemStatusCountData getCanonicalItemCountByStatus(final PoolData contextPool)
	{
		try
		{
			return contextPool == null ? statusCountClient.getCanonicalItemStatusCount()
					: statusCountClient.getCanonicalItemStatusCount(contextPool.getPoolName());
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

	private static CanonicalItemStatusCountData createErrorCount()
	{
		final CanonicalItemStatusCountData statusCount = new CanonicalItemStatusCountData();
		statusCount.setArchivedCount(-1L);
		statusCount.setDeletedCount(-1L);
		statusCount.setErrorCount(-1L);
		statusCount.setSuccessCount(-1L);
		return statusCount;
	}
}
