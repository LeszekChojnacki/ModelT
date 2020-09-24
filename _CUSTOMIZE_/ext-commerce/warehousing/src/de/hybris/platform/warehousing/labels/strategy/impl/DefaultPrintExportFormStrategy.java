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
package de.hybris.platform.warehousing.labels.strategy.impl;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.labels.strategy.PrintExportFormStrategy;
import de.hybris.platform.warehousing.sourcing.context.PosSelectionStrategy;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link PrintExportFormStrategy}. An export form is allowed to be printed if the order is not a
 * pickup or the {@link ConsignmentModel#SHIPPINGADDRESS} is different than the {@link PointOfServiceModel#ADDRESS}.
 */
public class DefaultPrintExportFormStrategy implements PrintExportFormStrategy
{
	private PosSelectionStrategy posSelectionStrategy;

	@Override
	public boolean canPrintExportForm(final ConsignmentModel consignmentModel)
	{
		ServicesUtil.validateParameterNotNull(consignmentModel, "Consignment cannot be null");
		boolean canPrint = true;

		if (consignmentModel.getDeliveryPointOfService() != null)
		{
			canPrint = false;
		}
		else
		{
			final PointOfServiceModel pointOfServiceModel = getPosSelectionStrategy()
					.getPointOfService(consignmentModel.getOrder(), consignmentModel.getWarehouse());
			if (pointOfServiceModel != null)
			{
				canPrint = !consignmentModel.getShippingAddress().getCountry().getIsocode()
						.equals(pointOfServiceModel.getAddress().getCountry().getIsocode());
			}
		}

		return canPrint;
	}

	protected PosSelectionStrategy getPosSelectionStrategy()
	{
		return posSelectionStrategy;
	}

	@Required
	public void setPosSelectionStrategy(final PosSelectionStrategy posSelectionStrategy)
	{
		this.posSelectionStrategy = posSelectionStrategy;
	}
}
