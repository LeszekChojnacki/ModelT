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
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;


/**
 * PromotionGroup.
 */
public class PromotionGroup extends GeneratedPromotionGroup //NOSONAR
{

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final String id = (String) allAttributes.get(PromotionGroup.IDENTIFIER);
		if (id != null)
		{
			final PromotionGroup group = PromotionsManager.getInstance().getPromotionGroup(id);
			if (group != null)
			{
				final String msg = "A PromotionGroup with the id " + id + " already exists!";
				throw new ConsistencyCheckException(msg, 100);
			}
		}

		return super.createItem(ctx, type, allAttributes);
	}

}
