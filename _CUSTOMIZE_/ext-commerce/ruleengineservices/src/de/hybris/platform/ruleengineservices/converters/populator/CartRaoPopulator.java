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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ruleengineservices.rao.CartRAO;

import java.util.LinkedHashSet;


/**
 * Converter implementation for {@link AbstractOrderModel} as source and {@link CartRAO} as target type.
 */
public class CartRaoPopulator extends AbstractOrderRaoPopulator<AbstractOrderModel, CartRAO>
{
	@Override
	public void populate(final AbstractOrderModel source, final CartRAO target)
	{
		super.populate(source, target);
		target.setActions(new LinkedHashSet<>());
		target.setOriginalTotal(target.getTotal());
	}
}
