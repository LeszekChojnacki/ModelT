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

package de.hybris.platform.configurablebundlecockpits.productcockpit.model.listview.impl;

import de.hybris.platform.cockpit.model.listview.CellRenderer;
import de.hybris.platform.cockpit.model.listview.TableModel;
import de.hybris.platform.cockpit.model.listview.ValueHandler;

import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;


/**
 * Simple cell renderer that renders a string representation of the value of interest.
 */
public class DefaultDecimalPriceCellRenderer implements CellRenderer
{
	private static final Logger LOGGER = Logger.getLogger(DefaultDecimalPriceCellRenderer.class);

	@Override
	public void render(final TableModel model, final int colIndex, final int rowIndex, final Component parent)
	{
		if (model == null || parent == null)
		{
			throw new IllegalArgumentException("Model and parent can not be null.");
		}

		String strValue = "";
		Object value = null;
		try
		{
			value = model.getValueAt(colIndex, rowIndex);
		}
		catch (final IllegalArgumentException iae)
		{
			LOGGER.warn("Could not render cell (Reason: '" + iae.getMessage() + "').", iae);
		}

		final Div div = new Div();
		div.setStyle("overflow: hidden;height: 100%;");

		if (ValueHandler.NOT_READABLE_VALUE.equals(value))
		{
			div.setSclass("listview_notreadable_cell");
			strValue = Labels.getLabel("listview.cell.readprotected");
		}
		else
		{
			if (strValue.isEmpty())
			{
				final BigDecimal price = (BigDecimal) value;
				ServicesUtil.validateParameterNotNullStandardMessage("price", price);

				if (BigDecimal.ZERO.compareTo(price) == 0)
				{
					strValue = BigDecimal.ZERO.toString();
				}
				else
				{
					strValue = String.valueOf(price);
				}
			}
		}
		final Label label = new Label(strValue);
		div.appendChild(label);
		parent.appendChild(div);
	}
}
