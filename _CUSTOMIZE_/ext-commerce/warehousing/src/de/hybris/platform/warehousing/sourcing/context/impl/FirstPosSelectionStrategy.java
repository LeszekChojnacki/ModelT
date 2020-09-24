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
package de.hybris.platform.warehousing.sourcing.context.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.sourcing.context.PosSelectionStrategy;

import java.util.NoSuchElementException;


/**
 * POS selection strategy that picks the first point of service for a specific warehouse
 */
public class FirstPosSelectionStrategy implements PosSelectionStrategy {

	@Override
	public PointOfServiceModel getPointOfService(final AbstractOrderModel orderModel, final WarehouseModel warehouse)
	{
		try
		{
			return warehouse.getPointsOfService().iterator().next();

		}
		catch (NoSuchElementException exception)  //NOSONAR
		{
			return null;  //NOSONAR
		}

	}
}
