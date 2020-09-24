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

import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.engine.WidgetInstanceManager;

/**
 * A component responsible for rendering a Dashboard counts information.
 */
public interface DashboardRowRenderer
{
	void renderDashboardRow(Component parent, WidgetInstanceManager widgetInstanceManager);
}
