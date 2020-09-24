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
package de.hybris.platform.solrfacetsearchbackoffice.wizards;

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


/**
 *
 */
public abstract class AbstractCustomWizardStep extends DefaultCustomViewRenderer
{
	private static final String WIDGET_CONTROLLER_PARAM = "widgetController";

	private ConfigurableFlowController widgetController;
	private String currentObjectPrefix;

	protected String normalizeAttribute(final String attribute)
	{
		final String prefix = getCurrentObjectPrefix();
		return String.format("%s%s%s", prefix, ".", attribute);
	}

	protected void setAttribute(final Component comp, final String attribute, final Object value)
	{
		final String normalizedAttribute = normalizeAttribute(attribute);
		getWidgetController().setValue(normalizedAttribute, value);
	}

	protected <T> T getAttribute(final Component comp, final String attribute, final Class<T> clazz)
	{
		final String normalizedAttribute = normalizeAttribute(attribute);
		return getWidgetController().getValue(normalizedAttribute, clazz);
	}

	protected <T> T getCurrentObject(final Component comp, final Class<T> clazz)
	{
		return getWidgetController().getValue(getCurrentObjectPrefix(), clazz);
	}

	protected void setWidgetController(final WidgetInstanceManager widgetInstanceManager)
	{
		if (widgetController == null)
		{
			widgetController = (ConfigurableFlowController) widgetInstanceManager.getWidgetslot()
					.getAttribute(WIDGET_CONTROLLER_PARAM);
		}

		Validate.notNull(widgetController, "Cannot find associated controller!");
	}

	protected ConfigurableFlowController getWidgetController()
	{
		return widgetController;
	}

	public String getCurrentObjectPrefix()
	{
		return currentObjectPrefix;
	}

	public void setCurrentObjectPrefix(final String currentObjectPrefix)
	{
		this.currentObjectPrefix = currentObjectPrefix;
	}
}
