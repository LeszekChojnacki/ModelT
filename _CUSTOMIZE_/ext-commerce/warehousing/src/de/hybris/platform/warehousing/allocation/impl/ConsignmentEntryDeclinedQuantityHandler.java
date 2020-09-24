/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.allocation.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;


/**
 * Handler for {@link ConsignmentEntryModel#getQuantityDeclined()}
 */
public class ConsignmentEntryDeclinedQuantityHandler implements DynamicAttributeHandler<Long, ConsignmentEntryModel>
{
	@Override
	public Long get(final ConsignmentEntryModel consignmentEntry)
	{
		return Long.valueOf(consignmentEntry.getDeclineEntryEvents().stream().mapToLong(event -> event.getQuantity()).sum());
	}

	@Override
	public void set(final ConsignmentEntryModel consignmentEntry, final Long value)
	{
		throw new UnsupportedOperationException();
	}

}
