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
package de.hybris.platform.warehousing.returns.strategy.impl;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.strategy.ReturnableCheck;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Set;


/**
 * Checks whether or not an orderEntry is returnable, by looking at how many items were shipped or already returned.
 * Items in pending returns and items refunded should not be available to create other returns.
 */
public class WarehousingOrderEntryBasedReturnableCheck implements ReturnableCheck
{
	private ReturnService returnService;
	private Set<ReturnStatus> invalidReturnStatusForIncompleteReturns;

	/*
	 * Verifies that the return quantity is lower than the quantity shipped - quantity returned
	 * 
	 * @see de.hybris.platform.returns.strategy.ReturnableCheck#performStrategy(de.hybris.platform.core.model.order.OrderModel,
	 * de.hybris.platform.core.model.order.AbstractOrderEntryModel, long)
	 */
	@Override
	public boolean perform(final OrderModel order, final AbstractOrderEntryModel orderEntry, final long returnQuantity)
	{
		if (returnQuantity < 1 || !order.getEntries().contains(orderEntry))
		{
			return false;
		}

		final List<ReturnEntryModel> returnEntries = getReturnService().getReturnEntry(orderEntry);

		final long incompleteReturns = returnEntries.stream()
				.filter(entry -> !(getInvalidReturnStatusForIncompleteReturns().contains(entry.getStatus())))
				.mapToLong(entry -> entry.getExpectedQuantity() != null ? entry.getExpectedQuantity().longValue() : 0L).sum();

		final long completeReturns = ((OrderEntryModel) orderEntry).getQuantityReturned().longValue();

		final long quantityAvailable =
				((OrderEntryModel) orderEntry).getQuantityShipped().longValue() - incompleteReturns - completeReturns;

		return quantityAvailable >= returnQuantity;
	}

	protected ReturnService getReturnService()
	{
		return returnService;
	}

	@Required
	public void setReturnService(final ReturnService returnService)
	{
		this.returnService = returnService;
	}

	protected Set<ReturnStatus> getInvalidReturnStatusForIncompleteReturns()
	{
		return invalidReturnStatusForIncompleteReturns;
	}

	@Required
	public void setInvalidReturnStatusForIncompleteReturns(final Set<ReturnStatus> invalidReturnStatusForIncompleteReturns)
	{
		this.invalidReturnStatusForIncompleteReturns = invalidReturnStatusForIncompleteReturns;
	}

}
