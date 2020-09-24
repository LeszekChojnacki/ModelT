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
package de.hybris.platform.ticket.model;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.ticket.service.TicketService;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;


/**
 * Special handler to fetch tickets into a CustomerModel.tickets attribute and vise versa
 */
public class TicketsAttributeHandler implements DynamicAttributeHandler<List<CsTicketModel>, CustomerModel>
{
	@Resource(name = "defaultTicketService")
	private TicketService ticketService;
	@Resource(name = "defaultModelService")
	private ModelService modelService;

	@Override
	public List<CsTicketModel> get(final CustomerModel model)
	{
		if (modelService.isNew(model))
		{
			return Collections.emptyList();
		}
		return ticketService.getTicketsForCustomer(model);
	}

	/**
	 *
	 * @param model
	 * @param csTicketModel
	 */
	@Override
	public void set(final CustomerModel model, final List<CsTicketModel> csTicketModel)
	{
		for (final CsTicketModel ticket: csTicketModel)
		{
			ticket.setCustomer(model);
			modelService.save(ticket);
		}
	}
}
