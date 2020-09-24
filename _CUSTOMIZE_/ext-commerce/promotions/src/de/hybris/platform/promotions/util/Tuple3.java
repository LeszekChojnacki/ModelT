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

public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2>
{
	private T3 t3;

	public Tuple3(final T1 t1, final T2 t2, final T3 t3)
	{
		super(t1, t2);
		this.t3 = t3;
	}

	public T3 getThird()
	{
		return t3;
	}

	public void setThird(final T3 t3)
	{
		this.t3 = t3;
	}
}
