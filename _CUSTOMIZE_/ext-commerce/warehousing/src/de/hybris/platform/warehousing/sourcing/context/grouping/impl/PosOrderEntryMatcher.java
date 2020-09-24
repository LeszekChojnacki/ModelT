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
package de.hybris.platform.warehousing.sourcing.context.grouping.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryMatcher;


/**
 * Matcher to match order entries based on similar delivery point of service used for pickup/click & collect.
 */
public class PosOrderEntryMatcher implements OrderEntryMatcher<PointOfServiceModel>
{

	@Override
	public PointOfServiceModel getMatchingObject(final AbstractOrderEntryModel orderEntry)
	{
		return orderEntry.getDeliveryPointOfService();
	}
}
