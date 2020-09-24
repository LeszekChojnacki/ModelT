/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.process.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.warehousing.process.AbstractWarehousingBusinessProcessService;
import de.hybris.platform.warehousing.process.BusinessProcessException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Business process service for handling orders.
 */
public class DefaultOrderProcessService extends AbstractWarehousingBusinessProcessService<OrderModel>
{
	private transient BaseStoreService baseStoreService;

	@Override
	public String getProcessCode(final OrderModel order)
	{
		if (CollectionUtils.isEmpty(order.getOrderProcess()))
		{
			throw new BusinessProcessException("Unable to process event for order [" + order.getCode()
					+ "]. No processes associated to the order.");
		}

		// Get base store for order
		final BaseStoreModel store = Optional.ofNullable(Optional.ofNullable(order.getStore()).orElseGet(() -> getBaseStoreService().getCurrentBaseStore()))
.orElseThrow(
				() -> new BusinessProcessException("Unable to process event for order [" + order.getCode()
						+ "]. No base store associated to the order."));

		// Get fulfillment process name
		final String fulfilmentProcessDefinitionName = Optional.ofNullable(store.getSubmitOrderProcessCode()).orElseThrow(
				() -> new BusinessProcessException("Unable to process event for order [" + order.getCode()
						+ "]. No fulfillment process definition for base store."));

		final String expectedCodePrefix = fulfilmentProcessDefinitionName;
		final Collection<String> codes = order.getOrderProcess().stream().map(OrderProcessModel::getCode)
				.filter(code -> code.startsWith(expectedCodePrefix)).collect(Collectors.toList());

		// Validate that we have 1 valid process
		if (CollectionUtils.isEmpty(codes))
		{
			throw new BusinessProcessException("Unable to process event for order [" + order.getCode()
					+ "]. No fulfillment processes associated to the order with prefix [" + fulfilmentProcessDefinitionName + "].");
		}
		if (codes.size() > 1)
		{
			throw new BusinessProcessException("Unable to process event for order [" + order.getCode()
					+ "]. Expected only 1 process with prefix [" + fulfilmentProcessDefinitionName + "] but there were "
					+ codes.size() + ".");
		}

		return codes.iterator().next();
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

}
