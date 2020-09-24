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

package de.hybris.platform.configurablebundlecockpits.services.label.impl;

import de.hybris.platform.cockpit.services.label.AbstractModelLabelProvider;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.configurablebundleservices.model.AbstractBundleRuleModel;

import java.util.Collection;
import java.util.List;


/**
 * Abstract Bundle Rule label provider
 */
public abstract class AbstractBundleRuleLabelProvider<R extends AbstractBundleRuleModel> extends AbstractModelLabelProvider<R>
{
	@Override
	protected String getIconPath(final R bundleRule)
	{

		return null;
	}

	@Override
	protected String getIconPath(final R bundleRule, final String languageIso)
	{

		return null;
	}

	@Override
	protected String getItemDescription(final R bundleRule)
	{
		return null;
	}

	@Override
	protected String getItemDescription(final R bundleRule, final String languageIso)
	{
		return null;
	}

	protected String getProductNames(final Collection<ProductModel> associatedProducts)
	{
		final StringBuilder productsBuffer = new StringBuilder();

		if (!((List<ProductModel>) associatedProducts).isEmpty())
		{
			for (final ProductModel curProduct : (List<ProductModel>) associatedProducts)
			{
				if (productsBuffer.length() == 0)
				{
					productsBuffer.append("'" + curProduct.getName() + "'");
				}
				else
				{
					productsBuffer.append(", '" + curProduct.getName() + "'");
				}
			}
		}

		return productsBuffer.toString();
	}

	@Override
	protected String getItemLabel(final R bundleRule, final String languageIso)
	{
		return getItemLabel(bundleRule);
	}
}
