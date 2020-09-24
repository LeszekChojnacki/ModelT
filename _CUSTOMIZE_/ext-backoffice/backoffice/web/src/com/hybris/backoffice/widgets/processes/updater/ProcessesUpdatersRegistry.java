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
package com.hybris.backoffice.widgets.processes.updater;

import java.util.function.Consumer;

import com.hybris.cockpitng.components.Widgetslot;


/**
 * @deprecated since 6.6 - not used anymore
 */
@Deprecated
public interface ProcessesUpdatersRegistry
{
	/**
	 * Registers global event listeners on given widgetSlot.
	 * 
	 * @param widgetslot
	 *           widget slot on which event listeners will be registered.
	 * @param updateCronJob
	 *           consumer which updates a cronJob on the list
	 */
	void registerGlobalEventListeners(Widgetslot widgetslot, Consumer<String> updateCronJob);
}
