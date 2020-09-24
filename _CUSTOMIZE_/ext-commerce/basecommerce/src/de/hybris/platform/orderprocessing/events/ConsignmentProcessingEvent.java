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
package de.hybris.platform.orderprocessing.events;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * Abstract base class for all OrderProcess events
 */
public class ConsignmentProcessingEvent extends AbstractEvent
{
	private final ConsignmentProcessModel process;
	private final ConsignmentStatus consignmentStatus;

	public ConsignmentProcessingEvent(final ConsignmentProcessModel process)
	{
		this.process = process;

		// Extract the current order status
		if (process != null)
		{
			final ConsignmentModel consignment = process.getConsignment();
			consignmentStatus = consignment == null ? null : consignment.getStatus();
		}
		else
		{
			consignmentStatus = null;
		}
	}

	public ConsignmentProcessModel getProcess()
	{
		return process;
	}

	public ConsignmentStatus getConsignmentStatus()
	{
		return consignmentStatus;
	}
}
