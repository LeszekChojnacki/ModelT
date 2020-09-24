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
package de.hybris.platform.ruleengineservices.calculation;

import de.hybris.order.calculation.domain.LineItem;
import de.hybris.order.calculation.domain.LineItemDiscount;
import de.hybris.order.calculation.domain.Order;
import de.hybris.order.calculation.domain.OrderDiscount;


/**
 * The strategy validates if Discount is applicable or not checking lower price limit of Order and LineItem.
 *
 */
public interface MinimumAmountValidationStrategy
{
	/**
	 * Checks if the subtotal is valid after application of the Discount to the Cart.
	 *
	 * @param cart
	 *           Cart to check
	 * @param discount
	 *           Discount to apply
	 * @return true if Cart subtotal is not lower than valid limit, false - otherwise
	 */
	boolean isOrderLowerLimitValid(final Order cart, final OrderDiscount discount);

	/**
	 * Checks if the LineItem subtotal and the Order subtotal are valid after application of the Discount to the
	 * LineItem.
	 *
	 * @param lineItem
	 *           LineItem to check
	 * @param discount
	 *           Discount to apply
	 * @return true if LineItem and Order subtotal is not lower than valid limit, false - otherwise
	 */
	boolean isLineItemLowerLimitValid(final LineItem lineItem, final LineItemDiscount discount);
}
