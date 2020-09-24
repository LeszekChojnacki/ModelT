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
package de.hybris.platform.ticket.strategies;

import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.Set;


/**
 * Interface to support the strategy of collecting the attributes changed on a ticket when it is updated.
 * 
 * @author Rick Hobbs (rick@neoworks.com)
 */
public interface TicketAttributeChangeEventStrategy
{
	/**
	 * Retrieve a list list of change entries for the specified ticket. It is down to the implementation to identify
	 * which attributes are changed and to create the appropriate corresponding entries for this attributes.
	 * 
	 * @param ticket
	 *           The ticket to get the changes for
	 * @return A set of change entries for the ticket
	 */
	Set<CsTicketChangeEventEntryModel> getEntriesForChangedAttributes(CsTicketModel ticket);
}
