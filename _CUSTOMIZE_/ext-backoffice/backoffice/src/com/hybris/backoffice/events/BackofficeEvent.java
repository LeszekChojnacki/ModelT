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
package com.hybris.backoffice.events;

import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import java.io.Serializable;


/**
 * A generic application event to communicate with backoffice cockpit domain.
 *
 * @deprecated since 6.7, extend {@link de.hybris.platform.servicelayer.event.events.AbstractEvent AbstractEvent}
 *             instead.
 */
@Deprecated
public class BackofficeEvent extends AbstractEvent
{
	private final String name;
	private final Object data;

	public BackofficeEvent(final String code, final Serializable source)
	{
		super(source);
		this.name = code;
		this.data = null;
	}

	public BackofficeEvent(final String code, final Object data, final Serializable source)
	{
		super(source);
		this.name = code;
		this.data = data;
	}

	/**
	 * @return the code
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the data
	 */
	public Object getData()
	{
		return data;
	}
}
