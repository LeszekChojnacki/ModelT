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
package de.hybris.platform.warehousing.labels.strategy.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.warehousing.labels.strategy.ExportFormPriceStrategy;

import java.math.BigDecimal;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link ExportFormPriceStrategy}
 */
public class DefaultExportFormPriceStrategy implements ExportFormPriceStrategy
{
	@Override
	public BigDecimal calculateProductPrice(final ConsignmentEntryModel consignmentEntry)
	{
		validateParameterNotNullStandardMessage("consignmentEntry", consignmentEntry);

		return (consignmentEntry.getOrderEntry().getBasePrice() != null)
				? BigDecimal.valueOf(consignmentEntry.getOrderEntry().getBasePrice()) : BigDecimal.ZERO;
	}

	@Override
	public BigDecimal calculateTotalPrice(final BigDecimal productPrice, final ConsignmentEntryModel consignmentEntry)
	{
		validateParameterNotNullStandardMessage("consignmentEntry", consignmentEntry);

		return productPrice.multiply(BigDecimal.valueOf(consignmentEntry.getQuantity()));
	}
}
