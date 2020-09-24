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
package de.hybris.platform.returns.strategy.impl;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.returns.strategy.ReturnableCheck;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;


/**
 * This class is used by {@link de.hybris.platform.returns.impl.DefaultReturnService} to provide information, if
 * specified product is returnable.
 */
public class DefaultConsignmentBasedReturnableCheck implements ReturnableCheck
{
	@Resource
	private ModelService modelService;

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DefaultConsignmentBasedReturnableCheck.class.getName());

	/**
	 * Determines if the product is 'returnable'. <br>
	 * The following checks are implemented:
	 * <ul>
	 * <li>FALSE: if the 'return quantity' is lesser 1 OR bigger than the total 'order' quantity
	 * <li>FALSE: if there are no consignments at all
	 * <li>TRUE: if the status is SHIPEPD and the specified 'return quantity' is lower or equal than the 'shipped
	 * quantity'.
	 * </ul>
	 *
	 * @param order
	 *           the related original order
	 * @param orderentry
	 *           the ordered product which will be returned
	 * @param returnQuantity
	 *           the quantity of entries to be returned
	 * @return true if product is 'returnable'
	 */
	@Override
	public boolean perform(final OrderModel order, final AbstractOrderEntryModel orderentry, final long returnQuantity)
	{
		// FALSE, in case of invalid quantity
		if (returnQuantity < 1 || orderentry.getQuantity().longValue() < returnQuantity)
		{
			return false;
		}

		// any existing consignments out there?
		final Set<ConsignmentModel> consignments = order.getConsignments();
		if (isEmpty(consignments))
		{
			// ... let'S FAIL if there were no consignments!
			return false;
		}

		// the order entry is only returnable if the consignment status is SHIPPED, a matching consignment entry is found and the return quantity is lower or equal than the shipped quantity
		final Optional<ConsignmentEntryModel> matchingConsignmentEntry = consignments.stream()
				.filter(c -> c.getStatus().getCode().equals(ConsignmentStatus.SHIPPED.getCode()))
				.flatMap(c -> c.getConsignmentEntries().stream()).filter(ce -> ce.getOrderEntry().equals(orderentry)).findFirst();
		return matchingConsignmentEntry.isPresent()
				&& matchingConsignmentEntry.get().getShippedQuantity().longValue() >= returnQuantity;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
