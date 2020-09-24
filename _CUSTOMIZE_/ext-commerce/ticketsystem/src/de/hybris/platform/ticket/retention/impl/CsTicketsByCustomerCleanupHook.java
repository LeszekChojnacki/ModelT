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

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.retention.hook.ItemCleanupHook;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * This Hook removes CustomerModel's CsTicketModels and related objects such as CsTicketEventModel and CsTicketChangeEventEntryModel
 */
public class CsTicketsByCustomerCleanupHook implements ItemCleanupHook<CustomerModel>
{
    private DefaultCsTicketCleanupStrategy csTicketCleanupStrategy;
    private ModelService modelService;

    @Override
    public void cleanupRelatedObjects(final CustomerModel customerModel)
    {
        validateParameterNotNullStandardMessage("customerModel", customerModel);
        if (CollectionUtils.isNotEmpty(customerModel.getTickets()))
        {
            customerModel.getTickets().forEach(csTicketModel ->
                    getCsTicketCleanupStrategy().cleanupRelatedObjects(csTicketModel));
            getModelService().removeAll(customerModel.getTickets());
        }
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

    protected ModelService getModelService()
    {
        return modelService;
    }

    @Required
    public void setModelService(ModelService modelService)
    {
        this.modelService = modelService;
    }
}
