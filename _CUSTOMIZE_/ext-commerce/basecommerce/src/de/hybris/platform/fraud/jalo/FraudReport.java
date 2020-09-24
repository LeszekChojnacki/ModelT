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
package de.hybris.platform.fraud.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.type.ComposedType;

import java.util.Date;



public class FraudReport extends GeneratedFraudReport
{
	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{


		final Order owningOrder = (Order) allAttributes.get(ORDER);

		if (owningOrder == null)
		{
			throw new JaloInvalidParameterException("Missing " + ORDER + " for creating a new " + type.getCode(), 0);
		}

		if (allAttributes.get(TIMESTAMP) == null)
		{
			allAttributes.put(TIMESTAMP, new Date());
		}

		// perf: let all crucial attribute being written in INSERT
		allAttributes.setAttributeMode(ORDER, AttributeMode.INITIAL);

		allAttributes.setAttributeMode(TIMESTAMP, AttributeMode.INITIAL);

		allAttributes.setAttributeMode(EXPLANATION, AttributeMode.INITIAL);

		allAttributes.setAttributeMode(STATUS, AttributeMode.INITIAL);

		return super.createItem(ctx, type, allAttributes);
	}

}
