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
package de.hybris.platform.ruleengineservices.calculation.impl;

import de.hybris.order.calculation.domain.LineItem;
import de.hybris.order.calculation.domain.LineItemDiscount;
import de.hybris.order.calculation.domain.Order;
import de.hybris.order.calculation.domain.OrderDiscount;
import de.hybris.platform.ruleengineservices.calculation.MinimumAmountValidationStrategy;

import java.math.BigDecimal;


/**
 * Default implementation of {@link MinimumAmountValidationStrategy}.
 *
 */
public class DefaultMinimumAmountValidationStrategy implements MinimumAmountValidationStrategy
{
	private static final BigDecimal DEFAULT_ORDER_LOWER_LIMIT_AMOUNT = BigDecimal.ZERO;
	private static final BigDecimal DEFAULT_LINE_ITEM_LOWER_LIMIT_AMOUNT = BigDecimal.ZERO;

	private BigDecimal orderLowerLimitAmount = DEFAULT_ORDER_LOWER_LIMIT_AMOUNT;
	private BigDecimal lineItemLowerLimitAmount = DEFAULT_LINE_ITEM_LOWER_LIMIT_AMOUNT;

	@Override
	public boolean isOrderLowerLimitValid(final Order order, final OrderDiscount discount)
	{
		if (order.getDiscounts().contains(discount))
		{
			throw new IllegalArgumentException("The order already has the discount.");
		}
		try
		{
			order.addDiscount(discount);
			return isOrderLowerLimitValid(order);
		}
		finally
		{
			order.removeDiscount(discount);
		}
	}

	/**
	 * Checks if the Cart subtotal is valid.
	 */
	protected boolean isOrderLowerLimitValid(final Order order)
	{
		return order.getSubTotal().subtract(order.getTotalDiscount()).getAmount().compareTo(getOrderLowerLimitAmount()) >= 0;
	}

	@Override
	public boolean isLineItemLowerLimitValid(final LineItem lineItem, final LineItemDiscount discount)
	{
		if (lineItem.getDiscounts().contains(discount))
		{
			throw new IllegalArgumentException("The line item already has the discount.");
		}
		try
		{
			lineItem.addDiscount(discount);
			return isLineItemLowerLimitValid(lineItem) && isOrderLowerLimitValid(lineItem.getOrder());
		}
		finally
		{
			lineItem.removeDiscount(discount);
		}
	}

	/**
	 * Checks if the LineItem subtotal is valid.
	 */
	protected boolean isLineItemLowerLimitValid(final LineItem lineItem)
	{
		return lineItem.getSubTotal().subtract(lineItem.getTotalDiscount()).getAmount().compareTo(getLineItemLowerLimitAmount()) >= 0;
	}

	protected BigDecimal getOrderLowerLimitAmount()
	{
		return orderLowerLimitAmount;
	}

	public void setOrderLowerLimitAmount(final BigDecimal orderLowerLimitAmount)
	{
		this.orderLowerLimitAmount = orderLowerLimitAmount;
	}

	protected BigDecimal getLineItemLowerLimitAmount()
	{
		return lineItemLowerLimitAmount;
	}

	public void setLineItemLowerLimitAmount(final BigDecimal lineItemLowerLimitAmount)
	{
		this.lineItemLowerLimitAmount = lineItemLowerLimitAmount;
	}

}
