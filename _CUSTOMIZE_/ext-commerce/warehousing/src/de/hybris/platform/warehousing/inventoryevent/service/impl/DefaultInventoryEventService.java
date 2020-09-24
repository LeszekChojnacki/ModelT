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
package de.hybris.platform.warehousing.inventoryevent.service.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.stock.StockService;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.inventoryevent.dao.InventoryEventDao;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.CancellationEventModel;
import de.hybris.platform.warehousing.model.IncreaseEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;
import de.hybris.platform.warehousing.model.ShrinkageEventModel;
import de.hybris.platform.warehousing.model.WastageEventModel;
import de.hybris.platform.warehousing.stock.strategies.StockLevelSelectionStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * The default implementation of {@link InventoryEventService}
 */
public class DefaultInventoryEventService implements InventoryEventService
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultInventoryEventService.class);

	private ModelService modelService;
	private InventoryEventDao inventoryEventDao;
	private StockLevelSelectionStrategy stockLevelSelectionStrategy;
	private StockService stockService;
	private TimeService timeService;

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForConsignmentEntry(final ConsignmentEntryModel consignmentEntry)
	{
		validateParameterNotNullStandardMessage("consignmentEntry", consignmentEntry);
		return getInventoryEventDao().getAllocationEventsForConsignmentEntry(consignmentEntry);
	}

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForOrderEntry(final OrderEntryModel orderEntry)
	{
		validateParameterNotNullStandardMessage("orderEntry", orderEntry);

		return orderEntry.getConsignmentEntries().isEmpty() ?
				Collections.emptyList() :
				inventoryEventDao.getAllocationEventsForOrderEntry(orderEntry);
	}

	@Override
	public <T extends InventoryEventModel> Collection<T> getInventoryEventsForStockLevel(final StockLevelModel stockLevel,
			final Class<T> eventClassType)
	{
		validateParameterNotNullStandardMessage("stocklevel", stockLevel);
		validateParameterNotNullStandardMessage("eventClassType", eventClassType);
		return getInventoryEventDao().getInventoryEventsForStockLevel(stockLevel, eventClassType);
	}

	@Override
	public Collection<AllocationEventModel> createAllocationEvents(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);
		Preconditions.checkArgument(!consignment.getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final List<AllocationEventModel> allocationEvents = consignment.getConsignmentEntries().stream()
				.map(this::createAllocationEventsForConsignmentEntry).flatMap(Collection::stream).collect(Collectors.toList());

		getModelService().saveAll(allocationEvents);

		return allocationEvents;
	}

	@Override
	public List<AllocationEventModel> createAllocationEventsForConsignmentEntry(final ConsignmentEntryModel consignmentEntry)
	{
		Preconditions.checkArgument(!consignmentEntry.getConsignment().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		LOGGER.debug("Creating allocation event for ConsignmentEntry :: Product [{}], at Warehouse [{}]: \tQuantity: '{}'",
				consignmentEntry.getOrderEntry().getProduct().getCode(), consignmentEntry.getConsignment().getWarehouse().getCode(),
				consignmentEntry.getQuantity());

		final Collection<StockLevelModel> stockLevels = getStockService()
				.getStockLevels(consignmentEntry.getOrderEntry().getProduct(),
						Collections.singletonList(consignmentEntry.getConsignment().getWarehouse()));

		final Map<StockLevelModel, Long> stockLevelForAllocation = getStockLevelSelectionStrategy()
				.getStockLevelsForAllocation(stockLevels, consignmentEntry.getQuantity());

		final List<AllocationEventModel> allocationEvents = stockLevelForAllocation.entrySet().stream().map(stockMapEntry -> {
			final AllocationEventModel allocationEvent = getModelService().create(AllocationEventModel.class);
			allocationEvent.setConsignmentEntry(consignmentEntry);
			allocationEvent.setStockLevel(stockMapEntry.getKey());
			allocationEvent.setEventDate(getTimeService().getCurrentTime());
			allocationEvent.setQuantity(stockMapEntry.getValue());
			getModelService().save(allocationEvent);
			return allocationEvent;
		}).collect(Collectors.toList());

		return allocationEvents;
	}

	@Override
	public ShrinkageEventModel createShrinkageEvent(final ShrinkageEventModel shrinkageEventModel)
	{
		validateParameterNotNullStandardMessage("stockLevel", shrinkageEventModel.getStockLevel());
		Preconditions.checkArgument(!shrinkageEventModel.getStockLevel().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final StockLevelModel stockLevel = shrinkageEventModel.getStockLevel();
		final Long quantity = shrinkageEventModel.getQuantity();

		LOGGER.debug("Creating Shrinkage event for ConsignmentEntry :: Product [{}], at Warehouse [{}]: \tQuantity: '{}'",
				stockLevel.getProductCode(), stockLevel.getWarehouse(), quantity);

		final ShrinkageEventModel shrinkageEvent = getModelService().create(ShrinkageEventModel.class);
		shrinkageEvent.setStockLevel(stockLevel);
		shrinkageEvent.setEventDate(getTimeService().getCurrentTime());
		shrinkageEvent.setQuantity(quantity.longValue());
		shrinkageEvent.setComments(shrinkageEventModel.getComments());

		getModelService().save(shrinkageEvent);
		return shrinkageEvent;
	}

	@Override
	public WastageEventModel createWastageEvent(final WastageEventModel wastageEventModel)
	{
		validateParameterNotNullStandardMessage("stockLevel", wastageEventModel.getStockLevel());
		Preconditions.checkArgument(!wastageEventModel.getStockLevel().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final StockLevelModel stockLevel = wastageEventModel.getStockLevel();
		Long quantity = wastageEventModel.getQuantity();

		LOGGER.debug("Creating Wastage event for ConsignmentEntry :: Product [{}], at Warehouse [{}]: \tQuantity: '{}'",
				stockLevel.getProductCode(), stockLevel.getWarehouse(), quantity);

		final WastageEventModel wastageEvent = getModelService().create(WastageEventModel.class);
		wastageEvent.setStockLevel(stockLevel);
		wastageEvent.setEventDate(getTimeService().getCurrentTime());
		wastageEvent.setQuantity(quantity.longValue());

		getModelService().save(wastageEvent);
		return wastageEvent;
	}

	@Override
	public List<CancellationEventModel> createCancellationEvents(final CancellationEventModel cancellationEventModel)
	{
		validateParameterNotNullStandardMessage("consignmentEntry", cancellationEventModel.getConsignmentEntry());
		Preconditions.checkArgument(!cancellationEventModel.getConsignmentEntry().getConsignment().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final Collection<AllocationEventModel> allocationEvents = getAllocationEventsForConsignmentEntry(
				cancellationEventModel.getConsignmentEntry());

		final Map<StockLevelModel, Long> stockLevelsForCancellation = getStockLevelSelectionStrategy()
				.getStockLevelsForCancellation(allocationEvents, cancellationEventModel.getQuantity());

		final List<CancellationEventModel> cancellationEvents = stockLevelsForCancellation.entrySet().stream()
				.map(stockMapEntry -> {
					final CancellationEventModel cancellationEvent = getModelService().create(CancellationEventModel.class);
					cancellationEvent.setConsignmentEntry(cancellationEventModel.getConsignmentEntry());
					cancellationEvent.setEventDate(getTimeService().getCurrentTime());
					cancellationEvent.setQuantity(stockMapEntry.getValue());
					cancellationEvent.setStockLevel(stockMapEntry.getKey());
					cancellationEvent.setReason(cancellationEventModel.getReason());
					getModelService().save(cancellationEvent);
					return cancellationEvent;
				}).collect(Collectors.toList());

		return cancellationEvents;
	}

	@Override
	public IncreaseEventModel createIncreaseEvent(final IncreaseEventModel increaseEventModel)
	{
		validateParameterNotNullStandardMessage("stockLevel", increaseEventModel.getStockLevel());
		Preconditions.checkArgument(!increaseEventModel.getStockLevel().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final IncreaseEventModel increaseEvent = getModelService().create(IncreaseEventModel.class);
		increaseEvent.setEventDate(getTimeService().getCurrentTime());
		increaseEvent.setQuantity(increaseEventModel.getQuantity());
		increaseEvent.setStockLevel(increaseEventModel.getStockLevel());
		increaseEvent
				.setComments((increaseEventModel.getComments() != null) ? increaseEventModel.getComments() : new ArrayList<>());
		getModelService().save(increaseEvent);
		return increaseEvent;
	}

	@Override
	public void reallocateAllocationEvent(final DeclineEntry declineEntry, final Long quantityToDecline)
	{
		Preconditions.checkArgument(!declineEntry.getConsignmentEntry().getConsignment().getWarehouse().isExternal(),
				"External warehouses are not allowed to create AllocationEvent");

		final Collection<AllocationEventModel> allocationEvents = getAllocationEventsForConsignmentEntry(
				declineEntry.getConsignmentEntry());

		if (declineEntry.getConsignmentEntry().getQuantity().equals(quantityToDecline))
		{
			getModelService().removeAll(allocationEvents);
		}
		else
		{
			final Map<AllocationEventModel, Long> allocationEventsForReallocation = getAllocationEventsForReallocation(
					allocationEvents, quantityToDecline);

			for (final Map.Entry<AllocationEventModel, Long> allocationMapEntry : allocationEventsForReallocation.entrySet())
			{
				final AllocationEventModel allocationEvent = allocationMapEntry.getKey();
				if (allocationEvent.getQuantity() == allocationMapEntry.getValue())
				{
					getModelService().remove(allocationEvent);
				}
				else
				{
					allocationEvent.setQuantity(allocationEvent.getQuantity() - allocationMapEntry.getValue());
					getModelService().save(allocationEvent);
				}
			}
		}
	}

	@Override
	public Map<AllocationEventModel, Long> getAllocationEventsForReallocation(
			final Collection<AllocationEventModel> allocationEvents, final Long quantityToDecline)
	{
		final Map<AllocationEventModel, Long> allocationMap = new LinkedHashMap<>();
		long quantityLeftToDecline = quantityToDecline;

		final List<AllocationEventModel> sortedAllocationEvents = allocationEvents.stream().sorted(Comparator
				.comparing(event -> event.getStockLevel().getReleaseDate(), Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());

		final Iterator<AllocationEventModel> it = sortedAllocationEvents.iterator();

		while (quantityLeftToDecline > 0 && it.hasNext())
		{
			final AllocationEventModel allocationEvent = it.next();
			final long declinableQty = allocationEvent.getQuantity();

			quantityLeftToDecline = addToAllocationMap(allocationMap, allocationEvent, quantityLeftToDecline, declinableQty);
		}
		finalizeStockMap(allocationMap, quantityLeftToDecline);
		return allocationMap;
	}

	/**
	 * Adds {@link AllocationEventModel} and quantity to the Map depending on the quantity left and quantity available.
	 * Returns the quantity that was not able to be fulfilled by the passed {@link AllocationEventModel}
	 *
	 * @param allocationMap
	 * 		the {@link AllocationEventModel} map to append the stock passed
	 * @param allocationEvent
	 * 		the {@link AllocationEventModel}
	 * @param quantityLeft
	 * 		the quantity left to be added to the map
	 * @param quantityAvailable
	 * 		the quantity available in the passed {@link AllocationEventModel#getStockLevel()}
	 * @return the quantity left.
	 */
	protected long addToAllocationMap(final Map<AllocationEventModel, Long> allocationMap,
			final AllocationEventModel allocationEvent, final long quantityLeft, final Long quantityAvailable)
	{
		long quantityToFulfill = quantityLeft;
		if (quantityAvailable == null || quantityAvailable >= quantityToFulfill)
		{
			allocationMap.put(allocationEvent, quantityLeft);
			quantityToFulfill = 0;
		}
		else if (quantityAvailable > 0)
		{
			allocationMap.put(allocationEvent, quantityAvailable);
			quantityToFulfill -= quantityAvailable;
		}
		return quantityToFulfill;
	}

	/**
	 * If there's still any quantity left, the first entry of the passed Stock map will be increased by this amount.
	 *
	 * @param allocationMap
	 * 		the {@link AllocationEventModel} to quantity map
	 * @param quantityLeft
	 * 		the quantity left to fulfill
	 */
	protected void finalizeStockMap(final Map<AllocationEventModel, Long> allocationMap, final long quantityLeft)
	{
		if (quantityLeft > 0 && allocationMap.size() > 0)
		{
			final Map.Entry<AllocationEventModel, Long> mapEntry = allocationMap.entrySet().iterator().next();
			allocationMap.put(mapEntry.getKey(), mapEntry.getValue() + quantityLeft);
		}
	}

	@Override
	public Collection<AllocationEventModel> getAllocationEventsForConsignment(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);
		return getInventoryEventDao().getAllocationEventsForConsignment(consignment);
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

	protected InventoryEventDao getInventoryEventDao()
	{
		return inventoryEventDao;
	}

	@Required
	public void setInventoryEventDao(final InventoryEventDao inventoryEventDao)
	{
		this.inventoryEventDao = inventoryEventDao;
	}

	protected StockLevelSelectionStrategy getStockLevelSelectionStrategy()
	{
		return stockLevelSelectionStrategy;
	}

	@Required
	public void setStockLevelSelectionStrategy(final StockLevelSelectionStrategy stockLevelSelectionStrategy)
	{
		this.stockLevelSelectionStrategy = stockLevelSelectionStrategy;
	}

	protected StockService getStockService()
	{
		return stockService;
	}

	@Required
	public void setStockService(final StockService stockService)
	{
		this.stockService = stockService;
	}

	protected TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

}
