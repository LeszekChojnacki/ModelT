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
package com.hybris.backoffice.labels.renderers;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.price.TaxModel;
import de.hybris.platform.europe1.model.AbstractDiscountRowModel;
import de.hybris.platform.europe1.model.PriceRowModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;

import com.hybris.backoffice.labels.LabelHandler;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.collectionbrowser.mold.impl.common.AbstractMoldStrategy;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public class StandardCurrencyListViewRenderer extends AbstractWidgetComponentRenderer<Listcell, ListColumn, Object>
{

	private static final String CSS_CELL_LABEL = "yw-listview-cell-label";
	private static final Logger LOG = LoggerFactory.getLogger(StandardCurrencyListViewRenderer.class);
	private LabelHandler<Double, CurrencyModel> priceLabelHandler;

	@Override
	public void render(final Listcell parent, final ListColumn configuration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{

		boolean initialised = false;
		CurrencyModel currency = null;
		Double totalPrice = null;
		if (object instanceof AbstractDiscountRowModel)
		{
			final AbstractDiscountRowModel discount = (AbstractDiscountRowModel) object;
			currency = discount.getCurrency();
			totalPrice = discount.getValue();
			initialised = true;
		}
		else if (object instanceof TaxModel)
		{
			final TaxModel tax = (TaxModel) object;
			currency = tax.getCurrency();
			totalPrice = tax.getValue();
			initialised = true;
		}

		else if (object instanceof PriceRowModel)
		{
			final PriceRowModel price = (PriceRowModel) object;
			currency = price.getCurrency();
			totalPrice = price.getPrice();
			initialised = true;
		}
		else if (object instanceof AbstractOrderModel)
		{
			final AbstractOrderModel orderModel = (AbstractOrderModel) object;
			currency = orderModel.getCurrency();
			totalPrice = orderModel.getTotalPrice();
			initialised = true;
		}
		else if (object instanceof AbstractOrderEntryModel)
		{
			final AbstractOrderEntryModel orderEntryModel = (AbstractOrderEntryModel) object;
			currency = orderEntryModel.getOrder().getCurrency();
			totalPrice = orderEntryModel.getProperty(configuration.getQualifier());
			initialised = true;
		}
		else
		{
			LOG.warn("Passed object: [{}] is not of supported type", object);
		}

		renderComponents(parent, configuration, object, initialised, currency, totalPrice);

	}

	protected void renderComponents(final Listcell parent, final ListColumn configuration, final Object object,
			final boolean initialised, final CurrencyModel currency, final Double totalPrice)
	{
		final String labelText = initialised ? getPriceLabelHandler().getLabel(totalPrice, currency) : StringUtils.EMPTY;

		final Label label = new Label(labelText);
		UITools.modifySClass(label, CSS_CELL_LABEL, true);
		label.setAttribute(AbstractMoldStrategy.ATTRIBUTE_HYPERLINK_CANDIDATE, Boolean.TRUE);
		parent.appendChild(label);

		fireComponentRendered(label, parent, configuration, object);
		fireComponentRendered(parent, configuration, object);
	}

	public LabelHandler<Double, CurrencyModel> getPriceLabelHandler()
	{
		return priceLabelHandler;
	}

	@Required
	public void setPriceLabelHandler(final LabelHandler<Double, CurrencyModel> priceLabelHandler)
	{
		this.priceLabelHandler = priceLabelHandler;
	}
}
