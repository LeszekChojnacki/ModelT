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

import de.hybris.platform.directpersistence.audit.dao.WriteAuditRecordsDAO;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.ticket.events.model.CsTicketChangeEventEntryModel;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.retention.CsTicketCleanupStrategy;
import de.hybris.platform.ticket.service.TicketService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * Default impl of @{@link CsTicketCleanupStrategy}
 */
public class DefaultCsTicketCleanupStrategy implements CsTicketCleanupStrategy
{
	 private static final Logger LOG = Logger.getLogger(DefaultCsTicketCleanupStrategy.class);

    private TicketService ticketService;
    private ModelService modelService;
    private WriteAuditRecordsDAO writeAuditRecordsDAO;

    @Override
    public void cleanupRelatedObjects(CsTicketModel csTicketModel)
    {
        validateParameterNotNullStandardMessage("csTicketModel", csTicketModel);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Cleaning up ticket related objects for: " + csTicketModel);
        }

        final List<CsTicketEventModel> eventModels = getTicketService().getEventsForTicket(csTicketModel);

        eventModels.stream().forEach(event ->
        {
            event.getEntries().stream().forEach( entry ->
            {
                getModelService().remove(entry);
                getWriteAuditRecordsDAO().removeAuditRecordsForType(CsTicketChangeEventEntryModel._TYPECODE, entry.getPk());
            });
            getModelService().remove(event);
            getWriteAuditRecordsDAO().removeAuditRecordsForType(CsTicketEventModel._TYPECODE, event.getPk());
        });
    }

    protected ModelService getModelService()
    {
        return modelService;
    }

    @Required
    public void setModelService(ModelService modelService)
    {
        this.modelService = modelService;
    }

    protected TicketService getTicketService()
    {
        return ticketService;
    }

    @Required
    public void setTicketService(TicketService ticketService)
    {
        this.ticketService = ticketService;
    }

    protected WriteAuditRecordsDAO getWriteAuditRecordsDAO()
    {
        return writeAuditRecordsDAO;
    }

    @Required
    public void setWriteAuditRecordsDAO(WriteAuditRecordsDAO writeAuditRecordsDAO)
    {
        this.writeAuditRecordsDAO = writeAuditRecordsDAO;
    }
}
