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
package de.hybris.platform.orderprocessing.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.OrderFulfillmentProcessService;
import de.hybris.platform.orderprocessing.exception.FullfilmentProcessStaringException;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link OrderFulfillmentProcessService}
 */
public class DefaultOrderFulfillmentProcessService implements OrderFulfillmentProcessService
{

	private BusinessProcessService businessProcessService;
	private KeyGenerator fulfillmentProcessCodeGenerator;
	private ModelService modelService;

	@Override
	public OrderProcessModel startFulfillmentProcessForOrder(final OrderModel order)
	{
		if (order == null)
		{
			throw new IllegalArgumentException("Order cannot be null");
		}
		final String processDefinitionName = Config.getString("basecommerce.fulfillmentprocess.name", null);
		if (processDefinitionName == null || StringUtils.isBlank(processDefinitionName))
		{
			throw new FullfilmentProcessStaringException(order.getCode(), "", "No definition name found");
		}
		final OrderProcessModel process = getBusinessProcessService()
				.createProcess(getFulfillmentProcessCodeGenerator().generate().toString(), processDefinitionName);
		process.setOrder(order);
		getModelService().save(order);
		getBusinessProcessService().startProcess(process);
		return process;

	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	protected KeyGenerator getFulfillmentProcessCodeGenerator()
	{
		return fulfillmentProcessCodeGenerator;
	}

	@Required
	public void setFulfillmentProcessCodeGenerator(final KeyGenerator fulfillmentProcessCodeGenerator)
	{
		this.fulfillmentProcessCodeGenerator = fulfillmentProcessCodeGenerator;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
