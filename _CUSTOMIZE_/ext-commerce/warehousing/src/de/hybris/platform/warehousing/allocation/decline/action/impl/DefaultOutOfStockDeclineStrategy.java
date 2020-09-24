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
package de.hybris.platform.warehousing.allocation.decline.action.impl;


import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.allocation.decline.action.DeclineActionStrategy;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.ShrinkageEventModel;
import de.hybris.platform.warehousing.stock.services.impl.DefaultWarehouseStockService;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfAnyResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Strategy to apply when the decline reason for a consignment entry is {@link de.hybris.platform.warehousing.enums
 * .DeclineReason#OUTOFSTOCK}.
 */
public class DefaultOutOfStockDeclineStrategy implements DeclineActionStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOutOfStockDeclineStrategy.class);

	private InventoryEventService inventoryEventService;
	private DefaultWarehouseStockService warehouseStockService;

	@Override
	public void execute(final DeclineEntry declineEntry)
	{
		validateParameterNotNull(declineEntry, "Decline Entry cannot be null");
		final ProductModel product = declineEntry.getConsignmentEntry().getOrderEntry().getProduct();
		final WarehouseModel warehouse = declineEntry.getConsignmentEntry().getConsignment().getWarehouse();

		final Long totalQuantity = getWarehouseStockService().getStockLevelForProductCodeAndWarehouse(product.getCode(), warehouse);

		final Long unReceivedQuantity = warehouse.getStockLevels().stream()
				.filter(stockLevel -> stockLevel.getAsnEntry() != null && stockLevel.getAsnEntry().getAsn() != null && AsnStatus.CREATED
						.equals(stockLevel.getAsnEntry().getAsn().getStatus())).mapToLong(stockLevel -> stockLevel.getAvailable())
				.sum();

		final Long quantityToShrink = totalQuantity - unReceivedQuantity;

		// only create shrink event if atp quantity is more than 0
		if (quantityToShrink.longValue() > 0)
		{
			final ShrinkageEventModel shrinkageEventModel = new ShrinkageEventModel();
			shrinkageEventModel
					.setStockLevel(getWarehouseStockService().getUniqueStockLevel(product.getCode(), warehouse.getCode(), null, null));
			shrinkageEventModel.setQuantity(quantityToShrink);
			getInventoryEventService().createShrinkageEvent(shrinkageEventModel);
			LOGGER.debug("Out of stock Strategy is being invoked, Shrinkage event is being created for the amount: {}", quantityToShrink);
		}
	}

	@Override
	public void execute(final Collection<DeclineEntry> declineEntries)
	{
		validateIfAnyResult(declineEntries, "Nothing to decline");
		declineEntries.forEach(this::execute);
	}


	protected InventoryEventService getInventoryEventService()
	{
		return inventoryEventService;
	}

	@Required
	public void setInventoryEventService(final InventoryEventService inventoryEventService)
	{
		this.inventoryEventService = inventoryEventService;
	}

	protected DefaultWarehouseStockService getWarehouseStockService()
	{
		return warehouseStockService;
	}

	@Required
	public void setWarehouseStockService(final DefaultWarehouseStockService warehouseStockService)
	{
		this.warehouseStockService = warehouseStockService;
	}
}
