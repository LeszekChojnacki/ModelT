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
package de.hybris.platform.returns.jalo;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;


public class RefundEntry extends GeneratedRefundEntry //NOSONAR
{
	@Override
	public Currency getCurrency(final SessionContext ctx)
	{
		return getOrderEntry(ctx).getOrder(ctx).getCurrency(ctx);
	}
}
