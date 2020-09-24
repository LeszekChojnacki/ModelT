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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.returns.impl.DefaultReturnService;
import de.hybris.platform.returns.jalo.ReturnEntry;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.strategy.ReturnableCheck;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;


/**
 * This class is used by {@link de.hybris.platform.returns.impl.DefaultReturnService} to provide information, if
 * specified product is returnable. The implemented algorithm will check the corresponding 'returns entries' .
 */
public class DefaultReturnEntryBasedReturnableCheck implements ReturnableCheck
{
	@Resource
	private FlexibleSearchService flexibleSearchService;

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DefaultReturnService.class.getName());

	/**
	 * Determines if the product is 'returnable' on base of existing returns entries entries. 'True' will be returned, if
	 * there are no related 'returns entries' of if the quantity of the related 'returns entries' plus the specified
	 * 'return quantity' is less or equals to the total quantity of the ordered items so far.
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
		// false, in case of invalid quantity
		if (returnQuantity < 1 || orderentry.getQuantity().longValue() < returnQuantity)
		{
			return false;
		}

		// any existing returns entries out there?
		final List<ReturnEntryModel> returnsEntries = getReturnEntry(orderentry);
		// fine if there are no entries at all
		if (returnsEntries.isEmpty())
		{
			return true;
		}
		else
		{
			// check 'quantity'
			long returnedEntriesSoFar = 0;
			for (final ReturnEntryModel returnsEntry : returnsEntries)
			{
				returnedEntriesSoFar += returnsEntry.getExpectedQuantity().longValue();
			}
			// quantity of ordered items should be less or equal to the quantity of the already returned items plus the quantity of the new ones
			return orderentry.getQuantity().longValue() >= (returnedEntriesSoFar + returnQuantity);
		}
	}

	protected List<ReturnEntryModel> getReturnEntry(final AbstractOrderEntryModel entry)
	{
		final Map<String, Object> params = new HashMap();
		params.put("entry", entry);
		final String query = "SELECT {ret." + Item.PK + "} FROM { " + ReturnEntryModel._TYPECODE + " AS ret} WHERE {"
				+ ReturnEntry.ORDERENTRY + "} = ?entry ORDER BY {ret." + Item.PK + "} ASC";
		return (List) getFlexibleSearchService().search(query, params).getResult();
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

}
