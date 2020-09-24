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
package de.hybris.platform.stock.strategy.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.stock.strategy.BestMatchStrategy;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;


/**
 *
 */
public class DefaultBestMatchStrategy implements BestMatchStrategy
{
	private Date farFarWayDate = null;

	@Override
	public WarehouseModel getBestMatchOfQuantity(final Map<WarehouseModel, Integer> map)
	{
		WarehouseModel bestOne = null;
		int highestSoFar = 0;
		for (final Map.Entry<WarehouseModel, Integer> entry : map.entrySet())
		{
			final WarehouseModel warehouse = entry.getKey();
			final int quantity = entry.getValue().intValue();

			if (quantity > highestSoFar)
			{
				highestSoFar = quantity;
				bestOne = warehouse;
			}
		}
		return bestOne;
	}

	@Override
	public WarehouseModel getBestMatchOfAvailability(final Map<WarehouseModel, Date> map)
	{
		WarehouseModel bestOne = null;
		Date earliestSoFar = farFarWay();
		for (final Map.Entry<WarehouseModel, Date> entry : map.entrySet())
		{
			final WarehouseModel warehouse = entry.getKey();
			final Date date = entry.getValue();

			if (date.before(earliestSoFar))
			{
				earliestSoFar = date;
				bestOne = warehouse;
			}

		}
		return bestOne;
	}

	private Date farFarWay()
	{
		if (farFarWayDate == null)
		{
			final int year = 3000;
			final int month = 0; //January
			final int date = 1;

			final Calendar cal = Calendar.getInstance();

			cal.clear();

			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DATE, date);

			farFarWayDate = cal.getTime();
		}
		return farFarWayDate;
	}
}
