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
package de.hybris.platform.ticket.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

public class CsTicketPopulator <SOURCE extends CsTicketParameter, TARGET extends CsTicketModel> implements Populator<SOURCE, TARGET> {

    @Override
    public void populate(SOURCE source, TARGET target) throws ConversionException {

        target.setHeadline(source.getHeadline());
        target.setOrder(source.getAssociatedTo());
        target.setPriority(source.getPriority());
        target.setCategory(source.getCategory());
        target.setAssignedAgent(source.getAssignedAgent());
        target.setAssignedGroup(source.getAssignedGroup());
        target.setCustomer(source.getCustomer());

    }
}
