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
package de.hybris.platform.ruleengine.event;

import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

/**
 * @deprecated since 1811
 * 
 * Event listener to handle {@link KieModuleSwappingEvent}
 */
@Deprecated
public class OnKieModuleSwappingEventListener extends AbstractEventListener<KieModuleSwappingEvent>
{
	@Override
	protected void onEvent(final KieModuleSwappingEvent kieModuleSwappingEvent)
	{
		// empty
	}
}
