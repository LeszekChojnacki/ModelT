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
package de.hybris.platform.promotions.util;

public class Tuple1<T1>
{
	private T1 t1;

	public Tuple1(final T1 t1)
	{
		this.t1 = t1;
	}

	public T1 getFirst()
	{
		return t1;
	}

	public void setFirst(final T1 t1)
	{
		this.t1 = t1;
	}
}
