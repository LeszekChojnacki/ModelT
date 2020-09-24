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
package de.hybris.platform.warehousing.shipping.quantityhandlers;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.warehousing.consignmententry.service.ConsignmentEntryQuantityService;
import org.springframework.beans.factory.annotation.Required;


/**
 * Handler for {@link ConsignmentEntryModel#getQuantityShipped}
 */
public class ConsignmentEntryShippedQuantityHandler implements DynamicAttributeHandler<Long, ConsignmentEntryModel>
{
	private ConsignmentEntryQuantityService consignmentEntryQuantityService;

	@Override
	public Long get(final ConsignmentEntryModel consignmentEntry)
	{
		return getConsignmentEntryQuantityService().getQuantityShipped(consignmentEntry);
	}

	@Override
	public void set(final ConsignmentEntryModel consignmentEntry, final Long value)
	{
		throw new UnsupportedOperationException();
	}

	public ConsignmentEntryQuantityService getConsignmentEntryQuantityService()
	{
		return consignmentEntryQuantityService;
	}

	@Required
	public void setConsignmentEntryQuantityService(ConsignmentEntryQuantityService consignmentEntryQuantityService)
	{
		this.consignmentEntryQuantityService = consignmentEntryQuantityService;
	}
}
