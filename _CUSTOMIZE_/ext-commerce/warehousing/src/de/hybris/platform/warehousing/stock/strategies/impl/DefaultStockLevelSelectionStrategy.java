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
package de.hybris.platform.warehousing.stock.strategies.impl;

import de.hybris.platform.commerceservices.stock.strategies.CommerceAvailabilityCalculationStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.stock.strategies.StockLevelSelectionStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * The default implementation of {@link StockLevelSelectionStrategy}
 */
public class DefaultStockLevelSelectionStrategy implements StockLevelSelectionStrategy
{
	private CommerceAvailabilityCalculationStrategy commerceStockLevelCalculationStrategy;

	@Override
	public Map<StockLevelModel, Long> getStockLevelsForAllocation(final Collection<StockLevelModel> stockLevels,
			final Long quantityToAllocate)
	{
		final Map<StockLevelModel, Long> stockMap = new LinkedHashMap<>();
		final List<StockLevelModel> filteredStockLevels = filterAsnCancelledStockLevels(stockLevels);
		long quantityLeftAllocate = quantityToAllocate;

		final List<StockLevelModel> sortedStocks = filteredStockLevels.stream()
				.sorted(Comparator.comparing(StockLevelModel::getReleaseDate, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());

		final Iterator<StockLevelModel> it = sortedStocks.iterator();
		while (quantityLeftAllocate > 0 && it.hasNext())
		{
			final StockLevelModel stockLevel = it.next();
			final Long stockAvailability = getCommerceStockLevelCalculationStrategy()
					.calculateAvailability(Collections.singletonList(stockLevel));

			quantityLeftAllocate = addToStockMap(stockMap, stockLevel, quantityLeftAllocate, stockAvailability);
		}
		finalizeStockMap(stockMap, quantityLeftAllocate);

		return stockMap;
	}

	@Override
	public Map<StockLevelModel, Long> getStockLevelsForCancellation(final Collection<AllocationEventModel> allocationEvents,
			final Long quantityToCancel)
	{
		final Map<StockLevelModel, Long> stockMap = new LinkedHashMap<>();
		long quantityLeftToCancel = quantityToCancel;

		final List<AllocationEventModel> sortedAllocationEvents = allocationEvents.stream().sorted(Comparator
				.comparing(event -> event.getStockLevel().getReleaseDate(), Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());

		final Iterator<AllocationEventModel> it = sortedAllocationEvents.iterator();

		while (quantityLeftToCancel > 0 && it.hasNext())
		{
			final AllocationEventModel allocationEvent = it.next();
			final long allocatedQuantity = allocationEvent.getQuantity();
			quantityLeftToCancel = addToStockMap(stockMap, allocationEvent.getStockLevel(), quantityLeftToCancel, allocatedQuantity);
		}
		finalizeStockMap(stockMap, quantityLeftToCancel);
		return stockMap;
	}

	/**
	 * Adds {@link StockLevelModel} and quantity to the Map depending on the quantity left and quantity available.
	 * Returns the quantity that was not able to be fulfilled by the passed {@link StockLevelModel}
	 *
	 * @param stockMap
	 * 		the {@link StockLevelModel} map to append the stock passed
	 * @param stockLevel
	 * 		the {@link StockLevelModel}
	 * @param quantityLeft
	 * 		the quantity left to be added to the map
	 * @param quantityAvailable
	 * 		the quantity available in the passed {@link StockLevelModel}
	 * @return the quantity left.
	 */
	protected long addToStockMap(final Map<StockLevelModel, Long> stockMap, final StockLevelModel stockLevel,
			final long quantityLeft, final Long quantityAvailable)
	{
		long quantityToFulfill = quantityLeft;
		if (quantityAvailable == null || quantityAvailable >= quantityToFulfill)
		{
			stockMap.put(stockLevel, quantityLeft);
			quantityToFulfill = 0;
		}
		else if (quantityAvailable > 0)
		{
			stockMap.put(stockLevel, quantityAvailable);
			quantityToFulfill -= quantityAvailable;
		}
		return quantityToFulfill;
	}

	/**
	 * If there's still any quantity left, the first entry of the passed Stock map will be increased by this amount.
	 *
	 * @param stockMap
	 * 		the {@link StockLevelModel} to quantity map
	 * @param quantityLeft
	 * 		the quantity left to fulfill
	 */
	protected void finalizeStockMap(final Map<StockLevelModel, Long> stockMap, final long quantityLeft)
	{
		if (quantityLeft > 0 && stockMap.size() > 0)
		{
			final Map.Entry<StockLevelModel, Long> mapEntry = stockMap.entrySet().iterator().next();
			stockMap.put(mapEntry.getKey(), mapEntry.getValue() + quantityLeft);
		}
	}

	/**
	 * Removes the {@link StockLevelModel}(s), which belong to cancelled {@link de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel}
	 *
	 * @param stockLevels
	 * 		the given collection of {@link StockLevelModel}
	 * @return the filtered list of {@link StockLevelModel}(s) or null if the filtered list is empty
	 */
	protected List<StockLevelModel> filterAsnCancelledStockLevels(final Collection<StockLevelModel> stockLevels)
	{
		return stockLevels.stream().filter(stockLevel -> stockLevel.getAsnEntry() == null || !AsnStatus.CANCELLED
				.equals(stockLevel.getAsnEntry().getAsn().getStatus())).collect(Collectors.toList());
	}

	protected CommerceAvailabilityCalculationStrategy getCommerceStockLevelCalculationStrategy()
	{
		return commerceStockLevelCalculationStrategy;
	}

	@Required
	public void setCommerceStockLevelCalculationStrategy(
			final CommerceAvailabilityCalculationStrategy commerceStockLevelCalculationStrategy)
	{
		this.commerceStockLevelCalculationStrategy = commerceStockLevelCalculationStrategy;
	}

}
