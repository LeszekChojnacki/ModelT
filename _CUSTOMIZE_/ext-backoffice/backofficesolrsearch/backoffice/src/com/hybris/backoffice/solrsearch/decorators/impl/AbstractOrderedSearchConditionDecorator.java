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
package com.hybris.backoffice.solrsearch.decorators.impl;

import com.hybris.backoffice.solrsearch.decorators.SearchConditionDecorator;


public abstract class AbstractOrderedSearchConditionDecorator implements SearchConditionDecorator
{
	private int order;

	public void setOrder(final int order)
	{
		this.order = order;
	}

	@Override
	public int getOrder()
	{
		return order;
	}
}
