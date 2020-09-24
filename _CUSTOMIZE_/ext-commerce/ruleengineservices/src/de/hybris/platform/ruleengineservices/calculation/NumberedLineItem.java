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
import de.hybris.order.calculation.domain.Order;
import de.hybris.order.calculation.money.Money;


/**
 * NumberedLineItem adds the entryNumber property to its parent {@code LineItem} class, thereby allowing to lookup a
 * specific order entry within the enclosing {@link Order} by its entry number.
 */
public class NumberedLineItem extends LineItem
{
	private Integer entryNumber;

	public NumberedLineItem(final Money basePrice)
	{
		super(basePrice);
	}

	public NumberedLineItem(final Money basePrice, final int numberOfUnits)
	{
		super(basePrice, numberOfUnits);
	}

	public Integer getEntryNumber()
	{
		return entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber)
	{
		this.entryNumber = entryNumber;
	}

}
