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

import org.zkoss.zk.ui.Component;


/**
 * Renderer for editors.
 *
 * @param <L>
 *           - the type of the editor logic
 * @param <D>
 *           - the type of the item data
 *
 * @since 6.7
 */
public interface EditorRenderer<L extends EditorLogic, D>
{
	/**
	 * Checks if this renderer is enabled for a specific context.
	 *
	 * @param logic
	 *           - the editor logic
	 *
	 * @return <code>true</code> if the renderer is enabled, <code>false</code> otherwise
	 */
	default boolean isEnabled(final L logic)
	{
		return true;
	}

	/**
	 * Checks if a specific item can be rendered.
	 *
	 * @param logic
	 *           - the editor logic
	 * @param parent
	 *           - the parent component
	 * @param data
	 *           - the item data
	 *
	 * @return <code>true</code> if the item can be rendered, <code>false</code> otherwise
	 */
	default boolean canRender(final L logic, final Component parent, final D data)
	{
		return true;
	}


	/**
	 * This method is called before the {@link #render(EditorLogic, Component, Object)} method is called. This method is
	 * always called even when using lazy rendering.
	 *
	 * @param logic
	 *           - the editor logic
	 * @param parent
	 *           - the parent component
	 * @param data
	 *           - the item data
	 */
	default void beforeRender(final L logic, final Component parent, final D data)
	{
		// empty
	}

	/**
	 * Creates a new editor item component and attaches it to the specified parent component.
	 *
	 * @param logic
	 *           - the editor logic
	 * @param parent
	 *           - the parent component
	 * @param data
	 *           - the item data
	 */
	void render(L logic, Component parent, D data);
}
