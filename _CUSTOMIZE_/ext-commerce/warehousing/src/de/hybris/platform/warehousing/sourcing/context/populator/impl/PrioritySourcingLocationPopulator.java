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
package de.hybris.platform.warehousing.sourcing.context.populator.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.context.populator.SourcingLocationPopulator;

import com.google.common.base.Preconditions;


/**
 * Populate a sourcing location's priority.
 */
public class PrioritySourcingLocationPopulator implements SourcingLocationPopulator
{
	@Override
	public void populate(final WarehouseModel source, final SourcingLocation target)
	{
		Preconditions.checkArgument(source != null, "warehouse model (source) cannot be null.");
		Preconditions.checkArgument(target != null, "Sourcing location (target) cannot be null.");

		target.setPriority(source.getPriority());
	}
}
