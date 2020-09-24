/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.adaptivesearchbackoffice.widgets;

import java.text.MessageFormat;

import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.core.util.impl.TypedSettingsMap;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Abstract view model for widgets.
 */
public abstract class AbstractWidgetViewModel
{
	@WireVariable
	private WidgetInstanceManager widgetInstanceManager;

	protected WidgetInstanceManager getWidgetInstanceManager()
	{
		return widgetInstanceManager;
	}

	protected Widgetslot getWidgetslot()
	{
		return widgetInstanceManager.getWidgetslot();
	}

	protected TypedSettingsMap getWidgetSettings()
	{
		return widgetInstanceManager.getWidgetSettings();
	}

	protected WidgetModel getModel()
	{
		return widgetInstanceManager.getModel();
	}

	protected void sendOutput(final String socketId, final Object data)
	{
		widgetInstanceManager.sendOutput(socketId, data);
	}

	public String format(final String pattern, final Object... arguments)
	{
		return MessageFormat.format(pattern, arguments);
	}
}
