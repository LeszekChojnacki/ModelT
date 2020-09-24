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
package de.hybris.platform.subscriptioncockpits.model.editor.impl;

import de.hybris.platform.cockpit.model.editor.impl.DefaultSelectUIEditor;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import java.util.ArrayList;
import java.util.List;

/**
 * Customization of {@link DefaultSelectUIEditor}, that allows to select {@code null}.
 */
public class NullableSelectUIEditor extends DefaultSelectUIEditor
{
	@Override
	public void setAvailableValues(final List<? extends Object> availableValues)
	{
		final List<Object> listWithNull = new ArrayList<>(availableValues.size() + 1);
		listWithNull.add(null);
		listWithNull.addAll(availableValues);
		super.setAvailableValues(listWithNull);
	}

	@Override
	protected void addObjectToCombo(final Object value, final Combobox box)
	{
		if (value == null)
		{
			Comboitem comboitem = new Comboitem();
			comboitem.setLabel(Labels.getLabel("subscriptioncockpits.general.null"));
			comboitem.setValue(null);
			box.appendChild(comboitem);
		}
		else
		{
			super.addObjectToCombo(value, box);
		}
	}
}
