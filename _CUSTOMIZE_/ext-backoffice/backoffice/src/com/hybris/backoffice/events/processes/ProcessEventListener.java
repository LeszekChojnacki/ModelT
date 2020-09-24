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
package com.hybris.backoffice.events.processes;


import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

import com.hybris.backoffice.events.DefaultBackofficeEventSender;


public class ProcessEventListener extends AbstractEventListener<AbstractProcessEvent>
{
	@Override
	protected void onEvent(final AbstractProcessEvent event)
	{
		getBackofficeEventSender().sendEvent(event);
	}

	public DefaultBackofficeEventSender getBackofficeEventSender()
	{
		throw new UnsupportedOperationException(
				"Please define in the spring configuration a <lookup-method> for getBackofficeEventSender().");
	}
}
