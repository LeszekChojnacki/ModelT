/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.editor;

import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.editor.defaultenum.DefaultEnumEditor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;


/**
 * Editor that has optional = false by default
 */
// We had to this trick, because there's no XML config for that.
public class NonOptionalEnumEditor extends DefaultEnumEditor
{
	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		context.setOptional(false);
		super.render(parent, context, listener);
	}
}
