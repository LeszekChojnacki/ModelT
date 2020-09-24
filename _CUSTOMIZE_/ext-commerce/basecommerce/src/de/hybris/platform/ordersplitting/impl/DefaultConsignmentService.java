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
package de.hybris.platform.ordersplitting.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.ConsignmentCreationException;
import de.hybris.platform.ordersplitting.ConsignmentService;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Default implementation of {@link ConsignmentService}
 */
public class DefaultConsignmentService implements ConsignmentService
{
	private static final Logger LOG = Logger.getLogger(DefaultConsignmentService.class.getName());

	private ModelService modelService;
	private WarehouseService warehouseService;

	private static final String RANDOM_ALGORITHM = "SHA1PRNG";

	@Override
	public ConsignmentModel createConsignment(final AbstractOrderModel order, final String code,
			final List<AbstractOrderEntryModel> orderEntries) throws ConsignmentCreationException
	{

		final ConsignmentModel cons = modelService.create(ConsignmentModel.class);

		cons.setStatus(ConsignmentStatus.READY);
		cons.setConsignmentEntries(new HashSet<ConsignmentEntryModel>());
		cons.setCode(code);

		if (order != null)
		{
			cons.setShippingAddress(order.getDeliveryAddress());
		}

		for (final AbstractOrderEntryModel orderEntry : orderEntries)
		{
			final ConsignmentEntryModel entry = modelService.create(ConsignmentEntryModel.class);

			entry.setOrderEntry(orderEntry);
			entry.setQuantity(orderEntry.getQuantity());
			entry.setConsignment(cons);
			cons.getConsignmentEntries().add(entry);
			cons.setDeliveryMode(orderEntry.getDeliveryMode());

		}

		final List<WarehouseModel> warehouses = warehouseService.getWarehouses(orderEntries);
		if (warehouses.isEmpty())
		{
			throw new ConsignmentCreationException("No default warehouse found for consignment");
		}
		final WarehouseModel warehouse = warehouses.iterator().next();
		cons.setWarehouse(warehouse);
		cons.setOrder(order);


		return cons;

	}

	@Override
	public WarehouseModel getWarehouse(final List<AbstractOrderEntryModel> orderEntries)
	{
		try
		{
			final SecureRandom sRnd = SecureRandom.getInstance(RANDOM_ALGORITHM);

			final Set<WarehouseModel> warehouses = orderEntries.get(sRnd.nextInt(orderEntries.size()))
					.getChosenVendor().getWarehouses();
			final WarehouseModel[] warehouse = warehouses.toArray(new WarehouseModel[warehouses.size()]);

			return warehouse[sRnd.nextInt(warehouse.length)];
		}
		catch (final NoSuchAlgorithmException ex)
		{
			LOG.error("Get warehouse failed!!", ex);
			return null;
		}
	}


	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public void setWarehouseService(final WarehouseService warehouseService)
	{
		this.warehouseService = warehouseService;
	}

}
