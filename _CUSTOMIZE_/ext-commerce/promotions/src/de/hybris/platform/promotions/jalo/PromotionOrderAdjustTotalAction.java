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

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.promotions.util.Helper;
import de.hybris.platform.util.DiscountValue;


/**
 * PromotionOrderAdjustTotalAction. Action to create a fixed value adjustment to the order. Applying this action creates
 * a global discount value in the order to adjust for the amount specified. Undoing this action removes the global
 * discount value from the order.
 * 
 * 
 */
public class PromotionOrderAdjustTotalAction extends GeneratedPromotionOrderAdjustTotalAction
{
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PromotionOrderAdjustTotalAction.class.getName());

	@Override
	public boolean apply(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		final String code = this.getGuid(ctx);

		if (log.isDebugEnabled())
		{
			log.debug("(" + getPK() + ") apply: Order total is currently: " + order.getTotal(ctx)); // NOSONAR
		}

		final DiscountValue dv = new DiscountValue(code, this.getAmount(ctx).doubleValue() * -1, true, order.getCurrency(ctx)
				.getIsoCode(ctx)); // NOSONAR
		insertFirstGlobalDiscountValue(ctx, order, dv);

		if (log.isDebugEnabled())
		{
			log.debug("(" + getPK() + ") apply: Generated discount with name '" + code + "' for " + this.getAmount(ctx));
		}

		setMarkedApplied(ctx, true);

		return true;
	}

	@Override
	public boolean undo(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);
		boolean calculateTotals = false;

		final DiscountValue myDiscount = Helper.findGlobalDiscountValue(ctx, order, this.getGuid(ctx));
		if (myDiscount != null)
		{
			order.removeGlobalDiscountValue(ctx, myDiscount); // NOSONAR
			calculateTotals = true;
		}

		setMarkedApplied(ctx, false);

		return calculateTotals;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		final AbstractOrder order = getPromotionResult(ctx).getOrder(ctx);

		return Helper.findGlobalDiscountValue(ctx, order, this.getGuid(ctx)) != null;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		return -1.0D * this.getAmount(ctx).doubleValue();
	}
}
