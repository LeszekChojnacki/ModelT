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
package de.hybris.platform.ordersplitting.strategy.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.strategy.AbstractSplittingStrategy;

import java.util.Date;


/**
 *
 */
public class SplitByNamedDeliveryDate extends AbstractSplittingStrategy
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.ordersplitting.strategy.AbstractSplittingStrategy#getGroupingObject(de.hybris.platform.core
	 * .model.order.OrderEntryModel)
	 */
	@Override
	public Object getGroupingObject(final AbstractOrderEntryModel orderEntry)
	{

		return orderEntry.getNamedDeliveryDate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.ordersplitting.strategy.AbstractSplittingStrategy#afterSplitting(java.lang.Object,
	 * de.hybris.platform.ordersplitting.model.ConsignmentModel)
	 */
	@Override
	public void afterSplitting(final Object groupingObject, final ConsignmentModel createdOne)
	{
		createdOne.setNamedDeliveryDate((Date) groupingObject);

	}

}
