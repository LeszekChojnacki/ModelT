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
import de.hybris.platform.core.order.EntryGroup;
import de.hybris.platform.ruleengineservices.rao.OrderEntryGroupRAO;


/**
 * Populator implementation for {@link EntryGroup} as source and {@link OrderEntryGroupRAO} as target type.
 */
public class OrderEntryGroupRaoPopulator implements Populator<EntryGroup, OrderEntryGroupRAO>
{
	@Override
	public void populate(final EntryGroup source, final OrderEntryGroupRAO target)
	{
		target.setEntryGroupId(source.getGroupNumber());
		target.setExternalReferenceId(source.getExternalReferenceId());
		target.setGroupType(source.getGroupType().getCode());
	}
}
