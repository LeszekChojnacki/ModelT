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
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.config.jaxb.listview.Parameter;
import de.hybris.platform.cockpit.services.meta.TypeService;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;


/**
 * Simple cell renderer that renders a string representation of the value of interest.
 */
public class DefaultProductPriceCellRenderer implements CellRenderer
{
	private static final Logger LOG = Logger.getLogger(DefaultProductPriceCellRenderer.class);

	private TypeService typeService;


	@Override
	public void render(final TableModel model, final int colIndex, final int rowIndex, final Component parent)
	{
		if (model == null || parent == null)
		{
			throw new IllegalArgumentException("Model and parent can not be null.");
		}

		String text = "";
		Object value = null;
		try
		{
			value = model.getValueAt(colIndex, rowIndex);
		}
		catch (final IllegalArgumentException iae)
		{
			LOG.warn("Could not render cell (Reason: '" + iae.getMessage() + "').", iae);
		}

		final Div div = new Div();
		div.setStyle("overflow: hidden;height: 100%;");

		if (ValueHandler.NOT_READABLE_VALUE.equals(value))
		{
			div.setSclass("listview_notreadable_cell");
			text = Labels.getLabel("listview.cell.readprotected");
		}
		else
		{
			if (text.isEmpty())
			{

				final Collection products = (Collection) value;
				text = getProductNamesShortened(products);
			}
		}


		final Label label = new Label(text);
		div.appendChild(label);
		parent.appendChild(div);
	}

	protected String getProductNamesShortened(final Collection<TypedObject> associatedProducts)
	{

		final StringBuilder productsBuilder = new StringBuilder();


		if (!(associatedProducts).isEmpty())
		{
			final Iterator pmit = associatedProducts.iterator();
			final ProductModel pm = (ProductModel) ((TypedObject) pmit.next()).getObject();

			if (pmit.hasNext())
			{
				productsBuilder.append(pm.getName()).append(",...");
			}
			else
			{
				productsBuilder.append(pm.getName());
			}
		}

		return productsBuilder.toString();

	}

	protected String getBasePrice(final ProductModel pm) //NOSONAR
	{
		return null;
	}

	protected TypeService getTypeService()
	{
		if (this.typeService == null)
		{
			this.typeService = UISessionUtils.getCurrentSession().getTypeService();
		}

		return this.typeService;
	}
}
