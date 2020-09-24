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
package com.hybris.backoffice.solrsearch.events;

import de.hybris.platform.servicelayer.event.events.AfterInitializationStartEvent;

import com.hybris.backoffice.events.AbstractBackofficeEventListener;

/**
 * @deprecated since 1811, not used anymore
 */
@Deprecated
public class AfterInitializationStartBackofficeSearchListener
		extends AbstractBackofficeEventListener<AfterInitializationStartEvent>
{

	public AfterInitializationStartBackofficeSearchListener()
	{
		super();
	}

}
