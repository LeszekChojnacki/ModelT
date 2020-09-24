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
package de.hybris.platform.warehousing.orderentry.service.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.warehousing.consignmententry.service.ConsignmentEntryQuantityService;
import de.hybris.platform.warehousing.daos.WarehousingOrderEntryQuantityDao;
import de.hybris.platform.warehousing.orderentry.service.OrderEntryQuantityService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Provides the default implementation to get the values of the quantities for each stage.
 */
public class DefaultOrderEntryQuantityService implements OrderEntryQuantityService
{
	private WarehousingOrderEntryQuantityDao warehousingOrderEntryQuantityDao;
	private ConsignmentEntryQuantityService consignmentEntryQuantityService;

	@Override
	public Long getQuantityShipped(final OrderEntryModel orderEntryModel)
	{
		long shippedquantity = 0L;
		if (orderEntryModel.getConsignmentEntries() != null)
		{
			shippedquantity = orderEntryModel.getConsignmentEntries().stream().mapToLong(consEntry -> consEntry.getQuantityShipped())
					.sum();
		}
		return shippedquantity;
	}

	@Override
	public Long getQuantityCancelled(final OrderEntryModel orderEntryModel)
	{
		return getWarehousingOrderEntryQuantityDao().getCancelledQuantity(orderEntryModel);
	}

	@Override
	public Long getQuantityAllocated(final OrderEntryModel orderEntryModel)
	{
		if (orderEntryModel.getConsignmentEntries() == null)
		{
			return 0L;
		}

		return orderEntryModel.getConsignmentEntries().stream()
				.filter(consignmentEntry -> !consignmentEntry.getConsignment().getStatus().equals(ConsignmentStatus.CANCELLED))
				.mapToLong(consEntry -> consEntry.getQuantity()).sum();
	}

	@Override
	public Long getQuantityUnallocated(final OrderEntryModel orderEntryModel)
	{
		final long quantityUnallocated =
				orderEntryModel.getQuantity().longValue() - getQuantityAllocated(orderEntryModel).longValue();
		return Long.valueOf(quantityUnallocated >= 0L ? quantityUnallocated : 0L);
	}

	@Override
	public Long getQuantityPending(final OrderEntryModel orderEntryModel)
	{
		return Long.valueOf(orderEntryModel.getQuantity().longValue() - getQuantityShipped(orderEntryModel).longValue());
	}

	@Override
	public Long getQuantityReturned(final OrderEntryModel orderEntryModel)
	{
		return getWarehousingOrderEntryQuantityDao().getQuantityReturned(orderEntryModel);
	}

	@Override
	public Long getQuantityDeclined(final OrderEntryModel orderEntryModel)
	{
		long declinedQuantity = 0L;
		if (orderEntryModel.getConsignmentEntries() != null)
		{
			declinedQuantity = orderEntryModel.getConsignmentEntries().stream()
					.mapToLong(entry -> getConsignmentEntryQuantityService().getQuantityDeclined(entry)).sum();
		}
		return declinedQuantity;
	}

	protected WarehousingOrderEntryQuantityDao getWarehousingOrderEntryQuantityDao()
	{
		return warehousingOrderEntryQuantityDao;
	}

	@Required
	public void setWarehousingOrderEntryQuantityDao(final WarehousingOrderEntryQuantityDao warehousingOrderEntryQuantityDao)
	{
		this.warehousingOrderEntryQuantityDao = warehousingOrderEntryQuantityDao;
	}

	protected ConsignmentEntryQuantityService getConsignmentEntryQuantityService()
	{
		return consignmentEntryQuantityService;
	}

	@Required
	public void setConsignmentEntryQuantityService(final ConsignmentEntryQuantityService consignmentEntryQuantityService)
	{
		this.consignmentEntryQuantityService = consignmentEntryQuantityService;
	}
}
