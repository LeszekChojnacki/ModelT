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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.ruleengineservices.rao.DiscountValueRAO;

import java.math.BigDecimal;


/**
 * Converter implementation for {@link DiscountModel} as source and {@link DiscountValueRAO} as target type.
 */
public class DiscountValueRaoPopulator implements Populator<DiscountModel, DiscountValueRAO>
{
	@Override
	public void populate(final DiscountModel source, final DiscountValueRAO target)
	{
		if (source.getValue() != null)
		{
			target.setValue(BigDecimal.valueOf(source.getValue().doubleValue()));
		}
		if (source.getCurrency() != null)
		{
			target.setCurrencyIsoCode(source.getCurrency().getIsocode());
		}
	}
}
