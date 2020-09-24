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
package de.hybris.platform.warehousing.consignmententry.service.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.warehousing.consignmententry.service.ConsignmentEntryQuantityService;
import de.hybris.platform.warehousing.daos.WarehousingConsignmentEntryQuantityDao;
import org.springframework.beans.factory.annotation.Required;


/**
 * Provides the default implementation to get the values of the quantities for each stage.
 */
public class DefaultConsignmentEntryQuantityService implements ConsignmentEntryQuantityService
{
	private WarehousingConsignmentEntryQuantityDao warehousingConsignmentEntryQuantityDao;

	@Override
	public Long getQuantityShipped(final ConsignmentEntryModel consignmentEntryModel)
	{
		return getWarehousingConsignmentEntryQuantityDao().getQuantityShipped(consignmentEntryModel);
	}

	@Override
	public Long getQuantityPending(final ConsignmentEntryModel consignmentEntryModel)
	{
		Long pendingQty = Long.valueOf(0L);
		if (!consignmentEntryModel.getConsignment().getStatus().equals(ConsignmentStatus.CANCELLED))
		{
			pendingQty = Long.valueOf(consignmentEntryModel.getQuantity().longValue() - getQuantityShipped(consignmentEntryModel).longValue());
		}
		return pendingQty;
	}

	@Override
	public Long getQuantityDeclined(final ConsignmentEntryModel consignmentEntry)
	{
		Long result = Long.valueOf(0L);
		if (!consignmentEntry.getConsignment().getStatus().equals(ConsignmentStatus.CANCELLED))
		{
			result = getWarehousingConsignmentEntryQuantityDao().getQuantityDeclined(consignmentEntry);
		}
		return result;
	}

	protected WarehousingConsignmentEntryQuantityDao getWarehousingConsignmentEntryQuantityDao()
	{
		return warehousingConsignmentEntryQuantityDao;
	}

	@Required
	public void setWarehousingConsignmentEntryQuantityDao(
			final WarehousingConsignmentEntryQuantityDao warehousingConsignmentEntryQuantityDao)
	{
		this.warehousingConsignmentEntryQuantityDao = warehousingConsignmentEntryQuantityDao;
	}
}
