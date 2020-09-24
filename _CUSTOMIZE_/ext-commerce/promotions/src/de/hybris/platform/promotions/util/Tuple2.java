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

public class Tuple2<T1, T2> extends Tuple1<T1>
{
	private T2 t2;

	public Tuple2(final T1 t1, final T2 t2)
	{
		super(t1);
		this.t2 = t2;
	}

	public T2 getSecond()
	{
		return t2;
	}

	public void setSecond(final T2 t2)
	{
		this.t2 = t2;
	}
}
