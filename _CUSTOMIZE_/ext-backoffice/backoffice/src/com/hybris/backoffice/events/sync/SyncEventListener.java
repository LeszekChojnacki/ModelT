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
package com.hybris.backoffice.events.sync;


import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

import com.hybris.backoffice.events.DefaultBackofficeEventSender;

/**
 * @deprecated since 6.6, please use the {@link com.hybris.backoffice.events.processes.ProcessEventListener } instead.
 */
@Deprecated
public class SyncEventListener extends AbstractEventListener<AbstractSyncEvent>
{
	@Override
	protected void onEvent(final AbstractSyncEvent event)
	{
		getBackofficeEventSender().sendEvent(event);
	}

	public DefaultBackofficeEventSender getBackofficeEventSender()
	{
		throw new UnsupportedOperationException(
				"Please define in the spring configuration a <lookup-method> for getBackofficeEventSender().");
	}
}
