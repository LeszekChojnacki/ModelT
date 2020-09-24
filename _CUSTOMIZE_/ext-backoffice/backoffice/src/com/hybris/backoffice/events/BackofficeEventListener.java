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

import de.hybris.platform.servicelayer.event.EventSender;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;


/**
 * Publishes {@link BackofficeEvent} sent from platform to the cockpit domain.
 *
 * @deprecated since 6.7, extend {@link de.hybris.platform.servicelayer.event.impl.AbstractEventListener
 *             AbstractEventListener} instead.
 */
@Deprecated
public class BackofficeEventListener extends AbstractEventListener<BackofficeEvent>
{
	@Override
	protected void onEvent(final BackofficeEvent event)
	{
		getBackofficeEventSender().sendEvent(event);
	}

	/**
	 * @return the backofficeEventSender
	 */
	protected EventSender getBackofficeEventSender()
	{
		throw new UnsupportedOperationException(
				"Please define in the spring configuration a <lookup-method> for getBackofficeEventSender().");
	}
}
