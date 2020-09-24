/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.widgets;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;


public class CsCreateWizardBaseHandler
{

	private CockpitEventQueue cockpitEventQueue;

	protected void publishEvent(final String eventName, final Object object, final Context ctx)
	{
		if (!isCockpitEventNotificationDisabledInCtx(ctx))
		{
			final DefaultCockpitEvent event = new DefaultCockpitEvent(eventName, object, null);
			populateEventContext(ctx, event);
			cockpitEventQueue.publishEvent(event);
		}
	}

	protected void populateEventContext(final Context source, final DefaultCockpitEvent destination)
	{
		if (source != null)
		{
			source.getAttributeNames().stream().forEach(a -> destination.getContext().put(a, source.getAttribute(a)));
		}
	}

	protected boolean isCockpitEventNotificationDisabledInCtx(final Context ctx)
	{
		return ctx != null
				&& BooleanUtils.isTrue((Boolean) ctx.getAttribute(ObjectFacade.CTX_PARAM_SUPPRESS_EVENT));
	}

	/**
	 * @return the cockpitEventQueue
	 */
	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	/**
	 * @param cockpitEventQueue
	 *           the cockpitEventQueue to set
	 */
	@Required
	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}
}
