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
 */
package de.hybris.platform.datahubbackoffice.dataaccess;

import org.springframework.core.Ordered;

/**
 * A common superclass for the backoffice component, which need to provide ordered access support. This means the beans will be
 * loaded/accessed based on the order configured in the Spring application context.
 */
public abstract class OrderedBean
{
	private int order = Ordered.HIGHEST_PRECEDENCE;

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int number)
	{
		order = number;
	}
}
