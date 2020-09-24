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

import com.google.common.collect.Lists;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


/**
 * Populator implementation for {@link AbstractOrderEntryModel} as source and {@link OrderEntryRAO} as target type.
 */
public class OrderEntryRaoEntryGroupNumbersPopulator implements Populator<AbstractOrderEntryModel, OrderEntryRAO>
{
	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryRAO target)
	{
		if (isNotEmpty(source.getEntryGroupNumbers()))
		{
			target.setEntryGroupNumbers(Lists.newArrayList(source.getEntryGroupNumbers()));
		}
	}
}
