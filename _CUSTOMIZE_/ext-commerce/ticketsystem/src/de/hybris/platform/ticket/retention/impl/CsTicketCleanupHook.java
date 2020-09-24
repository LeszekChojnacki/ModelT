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
package de.hybris.platform.ticket.retention.impl;

import de.hybris.platform.retention.hook.ItemCleanupHook;
import de.hybris.platform.ticket.model.CsTicketModel;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * This Hook removes CsTicket related objects such as CsTicketEventModel and CsTicketChangeEventEntryModel
 */
public class CsTicketCleanupHook implements ItemCleanupHook<CsTicketModel>
{
    private DefaultCsTicketCleanupStrategy csTicketCleanupStrategy;

    @Override
    public void cleanupRelatedObjects(CsTicketModel csTicketModel)
    {
        validateParameterNotNullStandardMessage("csTicketModel", csTicketModel);

        getCsTicketCleanupStrategy().cleanupRelatedObjects(csTicketModel);
    }

    protected DefaultCsTicketCleanupStrategy getCsTicketCleanupStrategy()
    {
        return csTicketCleanupStrategy;
    }

    @Required
    public void setCsTicketCleanupStrategy(DefaultCsTicketCleanupStrategy csTicketCleanupStrategy)
    {
        this.csTicketCleanupStrategy = csTicketCleanupStrategy;
    }
}
