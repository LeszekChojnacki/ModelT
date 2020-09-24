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

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.datahub.client.StatusCountClient;
import com.hybris.datahub.dto.count.AbstractItemStatusCountData;
import com.hybris.datahub.dto.pool.PoolData;


public abstract class AbstractDashboardRowRenderer
{
	private static final String MODEL_VALUE_POOL = "currentPool";
	protected StatusCountClient statusCountClient;
	protected NotificationService notificationService;

	protected Div createDashboardColumnForStatus(final String labelClass, final String labelValue,
			final AbstractItemStatusCountData count)
	{
		final String columnWidthClass = getNumberOfColumnsToShowInCounts() == 5 ? " yw-fivecol" : "";

		final Div dashboardColumn = new Div();
		dashboardColumn.setSclass("yw-dashboarditem-column" + columnWidthClass);

		final Div dashboardCell = new Div();
		dashboardCell.setSclass(labelClass);

		final Label completeLabel = new Label();
		completeLabel.setSclass("yw-stat-label");
		completeLabel.setValue(Labels.getLabel("i18n." + labelValue));
		dashboardCell.appendChild(completeLabel);

		final Label itemStatusTypeLabel = new Label();
		setCountForLabel(labelValue, itemStatusTypeLabel, count);
		dashboardCell.appendChild(new Div());
		dashboardCell.appendChild(itemStatusTypeLabel);
		dashboardColumn.appendChild(dashboardCell);

		return dashboardColumn;
	}

	protected void createDashboardColumn(final Component parent, final String columnLabel)
	{
		final String columnWidthClass = getNumberOfColumnsToShowInCounts() == 5 ? " yw-fivecol" : "";

		final Div infoColumn = new Div();
		infoColumn.setSclass("yw-dashboarditem-column yw-dashboarditem-info-column" + columnWidthClass);

		final Label label = new Label();
		label.setValue(Labels.getLabel(columnLabel));
		infoColumn.appendChild(label);
		infoColumn.setParent(parent);
	}

	/**
	 * Returns number of columns shown in the counts statistics area.
	 * 
	 * @return total number of columns including possible header columns.
	 */
	protected abstract int getNumberOfColumnsToShowInCounts();

	protected void setDashboardColumn(final Component parent, final AbstractItemStatusCountData count, final String status)
	{
		final Div statusColumn = createDashboardColumnForStatus("yw-dashboarditem-content", status, count);
		statusColumn.setParent(parent);
	}

	protected void createSpacer(final Component parent)
	{
		final Div spacer = new Div();
		spacer.setSclass("yw-dashboard-horizontal-space");
		parent.appendChild(spacer);
	}

	protected PoolData extractPoolDataFromModel(final WidgetInstanceManager context)
	{
		return context.getModel().getValue(MODEL_VALUE_POOL, PoolData.class);
	}

	protected abstract void setCountForLabel(final String labelValue, final Label label, final AbstractItemStatusCountData count);

	@Required
	public void setStatusCountClient(final StatusCountClient statusCountClient)
	{
		this.statusCountClient = statusCountClient;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
