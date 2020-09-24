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
package de.hybris.platform.promotions.jalo.order;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.jalo.PromotionsManager;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.HashSet;
import java.util.Set;


public class PromotionCart extends Cart //NOSONAR
{

	@Override
	public Object getAttribute(final SessionContext ctx, final String qualifier) throws JaloSecurityException
	{
		Object retval = null;
		if (AbstractOrderModel.ALLPROMOTIONRESULTS.equals(qualifier))
		{
			final Set<PromotionResult> results = new HashSet<>();
			retval = results;

			final PromotionOrderResults por = PromotionsManager.getInstance().getPromotionResults(ctx, this);
			if (por != null)
			{
				results.addAll(por.getAllResults());
			}
		}
		else
		{
			retval = super.getAttribute(ctx, qualifier);
		}
		return retval;
	}
}
