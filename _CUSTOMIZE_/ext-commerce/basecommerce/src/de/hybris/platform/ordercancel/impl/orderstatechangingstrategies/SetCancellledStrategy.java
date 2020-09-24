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
package de.hybris.platform.ordercancel.impl.orderstatechangingstrategies;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.ordercancel.OrderStatusChangeStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 */
public class SetCancellledStrategy implements OrderStatusChangeStrategy
{
	private ModelService modelService;

	@Override
	public void changeOrderStatusAfterCancelOperation(final OrderCancelRecordEntryModel orderCancelRecordEntry,
			final boolean saveOrderModel)
	{
		orderCancelRecordEntry.getModificationRecord().getOrder().setStatus(OrderStatus.CANCELLED);
		if (saveOrderModel)
		{
			modelService.save(orderCancelRecordEntry.getModificationRecord().getOrder());
		}
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
