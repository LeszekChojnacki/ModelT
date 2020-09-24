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
 *
 */
package de.hybris.platform.warehousing.process;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.processengine.BusinessProcessService;


/**
 * Service to simplify working with the {@link BusinessProcessService}.
 *
 * @param <T>
 *           The type of item that this business process service is used for.
 */
public interface WarehousingBusinessProcessService<T extends ItemModel> extends BusinessProcessService
{

	/**
	 * Trigger a business process event to move a workflow out of a wait state.
	 *
	 * @param item
	 *           - the item being processed
	 * @param eventName
	 *           - the name of the event to trigger
	 */
	void triggerSimpleEvent(final T item, final String eventName);

	/**
	 * Trigger a business process event to move a workflow out of a wait state. Also allows to specify which outbound
	 * flow to take.
	 *
	 * @param item
	 *           - the item being processed
	 * @param eventName
	 *           - the name of the event to trigger
	 * @param choice
	 * 			- the choice of the event to trigger
	 * @throws BusinessProcessException
	 *            when the process cannot move to the requested state
	 */
	void triggerChoiceEvent(final T item, final String eventName, final String choice) throws BusinessProcessException;

	/**
	 * Get the business process associated with an item
	 *
	 * @param item
	 *           - the item model
	 * @return the business process code
	 * @throws BusinessProcessException
	 *            when no process can be found for the given item
	 */
	String getProcessCode(final T item) throws BusinessProcessException;

}
