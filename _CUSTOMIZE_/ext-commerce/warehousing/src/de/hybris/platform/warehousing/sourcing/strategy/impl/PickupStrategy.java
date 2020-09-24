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
package de.hybris.platform.warehousing.sourcing.strategy.impl;

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.sourcing.context.PosSelectionStrategy;
import de.hybris.platform.warehousing.sourcing.strategy.AbstractSourcingStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Strategy to apply when the order is to be picked up at a specific location.
 */
public class PickupStrategy extends AbstractSourcingStrategy
{
	private static Logger LOGGER = LoggerFactory.getLogger(PickupStrategy.class);

	protected static final String CODE_PICKUP = "pickup";

	private PosSelectionStrategy posSelectionStrategy;

	@Override
	public void source(final SourcingContext sourcingContext)
	{
		validateParameterNotNullStandardMessage("sourcingContext", sourcingContext);

		final Optional<AbstractOrderEntryModel> entryOptional = sourcingContext.getOrderEntries().stream().filter(
				orderEntry -> Objects.nonNull(orderEntry.getDeliveryPointOfService())
						&& ((OrderEntryModel) orderEntry).getQuantityUnallocated().longValue() > 0).findFirst();

		if (entryOptional.isPresent())
		{
			final AbstractOrderEntryModel entry = entryOptional.get();
			// find sourcing location associated to the delivery location
			final Optional<SourcingLocation> pickupLocation = sourcingContext.getSourcingLocations().stream()
					.filter(location -> canSourceOrderEntry(entry, location)).findFirst();

			pickupLocation.ifPresent(sourcingLocation -> createPickupSourcingResult(sourcingContext, sourcingLocation));
		}

		if (Objects.nonNull(sourcingContext.getResult()) && !sourcingContext.getResult().getResults().isEmpty())
		{
			LOGGER.debug("Total order entries sourceable using Pickup Strategy: {}",
					sourcingContext.getResult().getResults().iterator().next().getAllocation().size());
		}
	}

	/**
	 * Validates if the given {@link SourcingLocation} can be used to source the given {@link AbstractOrderEntryModel}
	 *
	 * @param entry
	 * 		given {@link AbstractOrderEntryModel}
	 * @param location
	 * 		given {@link SourcingLocation}
	 * @return boolean to indicate if the given location is eligible to fulfill the order entry
	 */
	protected boolean canSourceOrderEntry(final AbstractOrderEntryModel entry, final SourcingLocation location)
	{
		validateParameterNotNullStandardMessage("entry", entry);
		validateParameterNotNullStandardMessage("location", location);
		validateParameterNotNullStandardMessage("warehouse", location.getWarehouse());

		final Collection<DeliveryModeModel> warehouseSupportedDeliveryModes = location.getWarehouse().getDeliveryModes();
		final boolean isPickupDeliveryMode = warehouseSupportedDeliveryModes.stream().anyMatch(
				deliveryMode -> deliveryMode instanceof PickUpDeliveryModeModel || CODE_PICKUP.equals(deliveryMode.getCode()));
		final boolean isPosMatch = entry.getDeliveryPointOfService()
				.equals(getPosSelectionStrategy().getPointOfService(entry.getOrder(), location.getWarehouse()));

		return isPickupDeliveryMode && isPosMatch;
	}

	/**
	 * Create a sourcing result if the pickup location specified in the order has some stocks available for a given
	 * product and store it in the sourcing context.<br/>
	 * When there is insufficient stock for a given product, the sourcing context is marked as not complete.<br/>
	 * When there is no stock for a given product, no sourcing result is created.
	 *
	 * @param sourcingContext
	 * 		- the sourcing context
	 * @param location
	 * 		- the location where we getting the product availability.
	 */
	protected void createPickupSourcingResult(final SourcingContext sourcingContext, final SourcingLocation location)
	{
		boolean isComplete = true;
		final Map<AbstractOrderEntryModel, Long> allocations = new HashMap<AbstractOrderEntryModel, Long>();
		for (final AbstractOrderEntryModel entry : sourcingContext.getOrderEntries())
		{
			final OrderEntryModel orderEntryModel = (OrderEntryModel) entry;
			final Long stock = location.getAvailability().get(entry.getProduct());
			Long orderQty;
			if (stock.longValue() >= orderEntryModel.getQuantityUnallocated().longValue())
			{
				orderQty = orderEntryModel.getQuantityUnallocated();
			}
			else
			{
				// insufficient stock
				orderQty = stock;
				isComplete = false;

				LOGGER.debug("Incomplete sourcing - Insufficient stock for product [{}]: requested qty [{}], stock qty [{}]",
						entry.getProduct().getCode(), orderEntryModel.getQuantityUnallocated(), stock);
			}

			if (stock.longValue() > 0)
			{
				allocations.put(entry, orderQty);
				LOGGER.debug("Created sourcing result allocation for product [{}]: requested qty [{}] at location [{}] ",
						entry.getProduct().getCode(), orderQty, location.getWarehouse().getCode());
			}
		}

		if (!allocations.isEmpty())
		{
			final SourcingResult result = getSourcingResultFactory().create(allocations, location);
			sourcingContext.getResult().getResults().add(result);
		}

		sourcingContext.getResult().setComplete(isComplete);
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
