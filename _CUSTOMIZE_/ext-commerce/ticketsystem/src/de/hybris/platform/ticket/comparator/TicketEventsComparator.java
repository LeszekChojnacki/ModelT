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
package de.hybris.platform.ticket.comparator;

import de.hybris.platform.ticket.events.model.CsTicketEventModel;

import java.util.Comparator;


/**
 * This will be used to compare the last updated messages by the CS Agent and Customer.
 *
 */
public class TicketEventsComparator implements Comparator<CsTicketEventModel>
{

	@Override
	public int compare(final CsTicketEventModel csTicketEventModel1, final CsTicketEventModel csTicketEventModel2)
	{
		return csTicketEventModel2.getModifiedtime().compareTo(csTicketEventModel1.getModifiedtime());
	}
}
