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
package de.hybris.platform.adaptivesearchbackoffice.actions.configurablemultireferenceeditor;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ACTION_WIDGET_INSTANCE_MANAGER_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.CURRENT_OBJECT_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.SEARCH_REQUEST_SOCKET;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.VALUE_CHANGED_KEY;

import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchRequestData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.MultiReferenceEditorLogic;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public final class ReferenceActionUtils
{
	protected static final String EDITOR_LOGIC_KEY = "editorLogic";

	private ReferenceActionUtils()
	{
		// empty constructor
	}

	public static WidgetInstanceManager resolveWidgetInstanceManager(final ActionContext<? extends AbstractEditorData> ctx)
	{
		return (WidgetInstanceManager) ctx.getParameter(ACTION_WIDGET_INSTANCE_MANAGER_KEY);
	}

	public static <D extends AbstractEditorData, V> MultiReferenceEditorLogic<D, V> resolveEditorLogic(
			final ActionContext<? extends AbstractEditorData> ctx)
	{
		return (MultiReferenceEditorLogic<D, V>) ctx.getParameter(EDITOR_LOGIC_KEY);
	}

	public static <T> T resolveCurrentObject(final ActionContext<? extends AbstractEditorData> ctx, final Class<T> type)
	{
		final WidgetInstanceManager widgetInstanceManager = resolveWidgetInstanceManager(ctx);

		return widgetInstanceManager.getModel().getValue(CURRENT_OBJECT_KEY, type);
	}

	public static void updateCurrentObject(final ActionContext<? extends AbstractEditorData> ctx, final Object currentObject)
	{
		final WidgetInstanceManager widgetInstanceManager = resolveWidgetInstanceManager(ctx);

		widgetInstanceManager.getModel().setValue(CURRENT_OBJECT_KEY, currentObject);
		widgetInstanceManager.getModel().setValue(VALUE_CHANGED_KEY, Boolean.TRUE);
	}

	public static void refreshCurrentObject(final ActionContext<? extends AbstractEditorData> ctx)
	{
		final WidgetInstanceManager widgetInstanceManager = resolveWidgetInstanceManager(ctx);

		widgetInstanceManager.sendOutput(SEARCH_REQUEST_SOCKET, new SearchRequestData());
	}
}
