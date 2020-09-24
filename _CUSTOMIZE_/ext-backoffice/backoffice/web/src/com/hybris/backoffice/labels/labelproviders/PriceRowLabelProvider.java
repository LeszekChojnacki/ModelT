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
package com.hybris.backoffice.labels.labelproviders;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.europe1.model.PriceRowModel;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;

import com.hybris.backoffice.labels.LabelHandler;
import com.hybris.cockpitng.labels.LabelProvider;
import com.hybris.cockpitng.labels.LabelService;


public class PriceRowLabelProvider implements LabelProvider<PriceRowModel>
{

	private static final String SEPARATOR_SPACE = " ";
	private static final String SEPARATOR_DASH = " - ";

	private LabelHandler<Double, CurrencyModel> priceLabelHandler;
	private LabelService labelService;

	@Override
	public String getLabel(final PriceRowModel priceRow)
	{
		if (priceRow == null)
		{
			return StringUtils.EMPTY;
		}

		final StringBuilder sb = new StringBuilder();

		sb.append(getProductLabel(priceRow));
		sb.append(SEPARATOR_DASH);
		sb.append(getUserLabel(priceRow));
		sb.append(SEPARATOR_DASH);
		sb.append(priceRow.getMinqtd());
		final UnitModel unit = priceRow.getUnit();
		if (unit != null)
		{
			sb.append(SEPARATOR_SPACE);
			sb.append(unit.getCode());
		}
		sb.append(SEPARATOR_DASH);
		sb.append(getPriceLabel(priceRow));
		sb.append(SEPARATOR_SPACE);
		sb.append(getPriceTypeLabel(priceRow));
		if (priceRow.getCatalogVersion() != null)
		{
			sb.append(SEPARATOR_DASH);
			sb.append(getLabelService().getObjectLabel(priceRow.getCatalogVersion()));
		}
		return sb.toString();
	}

	protected String getPriceTypeLabel(final PriceRowModel priceRow)
	{
		return BooleanUtils.isTrue(priceRow.getNet()) ? Labels.getLabel("backoffice.price.row.label.provider.net")
				: Labels.getLabel("backoffice.price.row.label.provider.gross");
	}

	protected String getPriceLabel(final PriceRowModel priceRow)
	{
		final CurrencyModel currencyModel = priceRow.getCurrency();
		return getPriceLabelHandler().getLabel(priceRow.getPrice(), currencyModel);
	}

	protected String getUserLabel(final PriceRowModel priceRow)
	{
		if (priceRow.getUser() != null)
		{
			return getLabelService().getObjectLabel(priceRow.getUser());
		}
		else if (priceRow.getUg() != null)
		{
			return getLabelService().getObjectLabel(priceRow.getUg());
		}
		else
		{
			return Labels.getLabel("backoffice.price.row.label.provider.any");
		}
	}

	protected String getProductLabel(final PriceRowModel priceRow)
	{
		final ProductModel product = priceRow.getProduct();
		if (product != null)
		{
			return getLabelService().getShortObjectLabel(product);
		}
		else if (priceRow.getPg() != null)
		{
			return getLabelService().getObjectLabel(priceRow.getPg());
		}
		else
		{
			return Labels.getLabel("backoffice.price.row.label.provider.any");
		}
	}

	@Override
	public String getDescription(final PriceRowModel price)
	{
		return null;
	}

	@Override
	public String getIconPath(final PriceRowModel price)
	{
		return null;
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

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
