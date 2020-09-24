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
package de.hybris.platform.adaptivesearchbackoffice.editors;

import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Logic for editors.
 *
 * @param <T>
 *           - the type of the value
 *
 * @since 6.7
 */
public interface EditorLogic<T>
{
	/**
	 * Returns the widget instance manager.
	 *
	 * @return the widget instance manager
	 */
	WidgetInstanceManager getWidgetInstanceManager();

	/**
	 * Returns the editor context.
	 *
	 * @return the editor context
	 */
	EditorContext<T> getEditorContext();

	/**
	 * Returns the editor listener.
	 *
	 * @return the editor listener
	 */
	EditorListener<T> getEditorListener();
}
