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
import de.hybris.platform.ruleengineservices.rao.DiscountValueRAO;
import de.hybris.platform.util.DiscountValue;

import java.math.BigDecimal;


/**
 * Converter implementation for {@link DiscountValue} as source and {@link DiscountValueRAO} as target type.
 */
public class DiscountValueRaoFromDiscountValuePopulator implements Populator<DiscountValue, DiscountValueRAO>
{
	@Override
	public void populate(final DiscountValue source, final DiscountValueRAO target)
	{
		target.setValue(BigDecimal.valueOf(source.getAppliedValue()));
		target.setCurrencyIsoCode(source.getCurrencyIsoCode());
	}
}
