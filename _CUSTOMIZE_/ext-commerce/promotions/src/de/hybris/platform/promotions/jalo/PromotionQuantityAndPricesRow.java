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
package de.hybris.platform.promotions.jalo;

import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.SessionContext;

import org.apache.log4j.Logger;


/**
 * PromotionQuantityAndPricesRow. Holds a quantity and a collection of PromotionPriceRow objects. This is used by the
 * {@link ProductSteppedMultiBuyPromotion} to hold the settings for a single step.
 * 
 * 
 */
public class PromotionQuantityAndPricesRow extends GeneratedPromotionQuantityAndPricesRow
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PromotionQuantityAndPricesRow.class.getName());

	/**
	 * Remove the item. When this item is removed the PromotionPriceRows stored in the Prices collection will also be
	 * removed.
	 */
	@Override
	public void remove(final SessionContext ctx) throws ConsistencyCheckException
	{
		// Remove any linked price rows
		AbstractPromotion.deletePromotionPriceRows(ctx, getPrices(ctx));

		// then create the item
		super.remove(ctx);
	}
}
