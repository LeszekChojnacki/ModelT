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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRenderer;

import org.zkoss.zk.ui.Component;


/**
 * Default renderer for the item detail section.
 *
 * @param <D>
 *           - the type of the item data
 * @param <V>
 *           - the type of the item value
 */
public class DefaultAsMultiReferenceItemDetailRenderer<D extends AbstractEditorData, V>
		implements EditorRenderer<MultiReferenceEditorLogic<D, V>, D>
{
	@Override
	public boolean isEnabled(final MultiReferenceEditorLogic<D, V> logic)
	{
		return false;
	}

	@Override
	public boolean canRender(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		return false;
	}

	@Override
	public void render(final MultiReferenceEditorLogic<D, V> logic, final Component parent, final D data)
	{
		throw new UnsupportedOperationException("This renderer does not support rendering of items");
	}
}
