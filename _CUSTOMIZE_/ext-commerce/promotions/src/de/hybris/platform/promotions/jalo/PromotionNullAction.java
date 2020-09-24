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


/**
 * PromotionNullAction. Action that does nothing, but remembers its applied state.
 */
public class PromotionNullAction extends GeneratedPromotionNullAction //NOSONAR
{

	@Override
	public boolean apply(final SessionContext ctx)
	{
		setMarkedApplied(ctx, true);
		return false;
	}

	@Override
	public boolean undo(final SessionContext ctx)
	{
		setMarkedApplied(ctx, false);
		return false;
	}

	@Override
	public boolean isAppliedToOrder(final SessionContext ctx)
	{
		return true;
	}

	@Override
	public double getValue(final SessionContext ctx)
	{
		return 0.0D;
	}

}
