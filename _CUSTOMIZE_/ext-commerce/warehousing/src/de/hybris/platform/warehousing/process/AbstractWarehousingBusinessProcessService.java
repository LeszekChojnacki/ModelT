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
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.impl.DefaultBusinessProcessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract implementation of the {@link WarehousingBusinessProcessService} provides implementations for event triggering and
 * only requires that subclasses provide an means to obtain the business process code associated with the item model
 * provided.
 *
 * @param <T>
 * 		The type of item that this business process service is used for.
 */
public abstract class AbstractWarehousingBusinessProcessService<T extends ItemModel> extends DefaultBusinessProcessService
		implements WarehousingBusinessProcessService<T>
{
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractWarehousingBusinessProcessService.class);

	@Override
	public abstract String getProcessCode(final T item) throws BusinessProcessException;

	@Override
	public void triggerSimpleEvent(final T item, final String eventName)
	{
		final String processCode = getProcessCode(item);
		LOGGER.info("Process: {} triggered event {}", processCode, eventName);
		triggerEvent(processCode + "_" + eventName);
	}

	@Override
	public void triggerChoiceEvent(final T item, final String eventName, final String choice) throws BusinessProcessException
	{
		final String processCode = getProcessCode(item);
		LOGGER.info("Process: {} triggered event {}", processCode, eventName);
		final BusinessProcessEvent businessProcessEvent = BusinessProcessEvent.builder(processCode + "_" + eventName)
				.withChoice(choice).build();

		boolean result = Boolean.TRUE;
		try
		{
			result = triggerEvent(businessProcessEvent);
		}
		catch (final IllegalStateException e) //NOSONAR
		{
			result = Boolean.FALSE; //NOSONAR
		}
		if (!result)
		{
			throw new BusinessProcessException(
					"Unable to process action. The process with code [" + processCode + "] is not currently waiting for action.");
		}
	}
}
