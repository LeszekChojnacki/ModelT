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
package de.hybris.platform.ticket.retention;

import de.hybris.platform.ticket.model.CsTicketModel;

/**
 * That strategy is called in {@link de.hybris.platform.ticket.retention.impl.CsTicketsByCustomerCleanupHook}
 * and in {@link de.hybris.platform.ticket.retention.impl.CsTicketCleanupHook}
 * to clean the {@link CsTicketModel}'s {@link de.hybris.platform.ticket.events.model.CsTicketEventModel} and event' entries
 */
public interface CsTicketCleanupStrategy
{
    public void cleanupRelatedObjects(CsTicketModel csTicketModel);
}
