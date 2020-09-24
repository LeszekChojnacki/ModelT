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
package de.hybris.order.calculation.domain;

import de.hybris.order.calculation.money.Money;


/**
 * Interface for all objects that can be target of {@link Tax}.
 */
public interface Taxable
{
	/**
	 * Called to fetch the taxable total for tax total calculation.
	 * 
	 * @param context
	 *           the order within the tax is being calculated
	 */
	Money getTotal(Order context);
}
