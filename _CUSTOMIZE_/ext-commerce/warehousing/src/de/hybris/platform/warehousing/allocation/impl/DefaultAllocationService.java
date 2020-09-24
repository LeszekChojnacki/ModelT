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
package de.hybris.platform.warehousing.allocation.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.commerceservices.util.GuidKeyGenerator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.DeliveryModeService;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.stock.impl.StockLevelDao;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.allocation.AllocationException;
import de.hybris.platform.warehousing.allocation.AllocationService;
import de.hybris.platform.warehousing.allocation.strategy.ShippingDateStrategy;
import de.hybris.platform.warehousing.comment.WarehousingCommentService;
import de.hybris.platform.warehousing.externalfulfillment.dao.WarehousingFulfillmentConfigDao;
import de.hybris.platform.warehousing.data.allocation.DeclineEntries;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentContext;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentEventType;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.DeclineConsignmentEntryEventModel;
import de.hybris.platform.warehousing.stock.services.impl.DefaultWarehouseStockService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.springframework.util.Assert.isTrue;


/**
 * Service to create consignments based on the sourcing results.
 */
public class DefaultAllocationService implements AllocationService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAllocationService.class);
	protected static final String PICKUP_CODE = "pickup";
	protected static final String REALLOCATE_COMMENT_SUBJECT = "Decline consignment entry";


	private ModelService modelService;
	private StockLevelDao stockLevelDao;
	private DeliveryModeService deliveryModeService;
	private InventoryEventService inventoryEventService;
	private WarehousingCommentService consignmentEntryCommentService;
	private GuidKeyGenerator guidKeyGenerator;
	private ShippingDateStrategy shippingDateStrategy;
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;
	private WarehousingFulfillmentConfigDao warehousingFulfillmentConfigDao;

	@Override
	public Collection<ConsignmentModel> createConsignments(final AbstractOrderModel order, final String code,
			final SourcingResults results)
	{
		validateParameterNotNullStandardMessage("results", results);
		isTrue(!Strings.isNullOrEmpty(code), "Parameter code cannot be null or empty");

		final AtomicLong index = new AtomicLong();
		order.getConsignments().forEach(value -> index.getAndIncrement());

		final Collection<ConsignmentModel> consignments = results.getResults().stream()
				.map(result -> createConsignment(order, code + "_" + index.getAndIncrement(), result)).collect(Collectors.toList());

		getModelService().save(order);

		return consignments;
	}

	/**
	 * This implementation assumes that all order entries in SourcingResult have the same delivery POS.
	 */
	@Override
	public ConsignmentModel createConsignment(final AbstractOrderModel order, final String code, final SourcingResult result)
	{
		validateParameterNotNullStandardMessage("result", result);
		validateParameterNotNullStandardMessage("order", order);
		isTrue(!Strings.isNullOrEmpty(code), "Parameter code cannot be null or empty");

		LOGGER.debug("Creating consignment for Location: '{}'", result.getWarehouse().getCode());

		final ConsignmentModel consignment = getModelService().create(ConsignmentModel.class);
		consignment.setCode(code);
		consignment.setOrder(order);

		try
		{
			consignment.setFulfillmentSystemConfig(getWarehousingFulfillmentConfigDao().getConfiguration(result.getWarehouse()));

			final Set<Entry<AbstractOrderEntryModel, Long>> resultEntries = result.getAllocation().entrySet();

			final Optional<PointOfServiceModel> pickupPos = resultEntries.stream()
					.map(entry -> entry.getKey().getDeliveryPointOfService()).filter(Objects::nonNull).findFirst();

			if (pickupPos.isPresent())
			{
				consignment.setStatus(ConsignmentStatus.READY);
				consignment.setDeliveryMode(getDeliveryModeService().getDeliveryModeForCode(PICKUP_CODE));
				//This cannot be null so we put the POS address as placeholder
				consignment.setShippingAddress(pickupPos.get().getAddress());
				consignment.setDeliveryPointOfService(pickupPos.get());
			}
			else
			{
				consignment.setStatus(ConsignmentStatus.READY);
				consignment.setDeliveryMode(order.getDeliveryMode());
				consignment.setShippingAddress(order.getDeliveryAddress());
				consignment.setShippingDate(getShippingDateStrategy().getExpectedShippingDate(consignment));
			}

			final Set<ConsignmentEntryModel> entries = resultEntries.stream()
					.map(mapEntry -> createConsignmentEntry(mapEntry.getKey(), mapEntry.getValue(), consignment))
					.collect(Collectors.toSet());

			consignment.setConsignmentEntries(entries);
			consignment.setWarehouse(result.getWarehouse());

			if (consignment.getFulfillmentSystemConfig() == null)
			{
				getWarehousingConsignmentWorkflowService().startConsignmentWorkflow(consignment);
			}

			if (!consignment.getWarehouse().isExternal())
			{
				getInventoryEventService().createAllocationEvents(consignment);
			}
		}
		catch (final AmbiguousIdentifierException e) // NOSONAR
		{
			consignment.setStatus(ConsignmentStatus.CANCELLED);
			LOGGER.warn("Cancelling consignment with code " + consignment.getCode()
					+ " since only one fulfillment system configuration is allowed per consignment.");
		}


		getModelService().save(consignment);
		return consignment;
	}

	@Override
	public Collection<ConsignmentModel> manualReallocate(final DeclineEntries declinedEntries) throws AllocationException
	{
		validateParameterNotNullStandardMessage("declinedEntries", declinedEntries);
		isTrue(CollectionUtils.isNotEmpty(declinedEntries.getEntries()), "Entries cannot be null or empty.");

		final boolean isEntryWithNoLocation = declinedEntries.getEntries().stream()
				.anyMatch(entry -> entry.getReallocationWarehouse() == null);

		if (isEntryWithNoLocation)
		{
			throw new AllocationException("Invalid or no warehouse selected for manual reallocation");
		}

		final boolean isWarehouseWithNoStockEntry = declinedEntries.getEntries().stream().anyMatch(entry -> {
			final Collection<StockLevelModel> stockLevels = getStockLevelDao()
					.findStockLevels(entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(),
							Collections.singletonList(entry.getReallocationWarehouse()));
			return CollectionUtils.isEmpty(stockLevels);
		});

		if (isWarehouseWithNoStockEntry)
		{
			throw new AllocationException("No stock level entry found for manual reallocation");
		}

		final List<ConsignmentModel> newConsignments = new ArrayList<>();
		final ConsignmentModel consignment = declinedEntries.getEntries().iterator().next().getConsignmentEntry().getConsignment();
		final AbstractOrderModel order = consignment.getOrder();

		// Create the consignment entry events representing the decline history + update Allocation InventoryEvent for the corresponding consignment
		// + map them to a SourcingResult
		LOGGER.debug("Declining consignment with code :{}", consignment.getCode());

		declinedEntries.getEntries().stream().map(this::declineConsignmentEntry).filter(Optional::isPresent).map(Optional::get)
				.forEach(event -> consolidateConsignmentEntries(newConsignments, consignment, order, event));

		return newConsignments;
	}

	/**
	 * Consolidates the {@link ConsignmentEntryModel} if they belong to the same {@link de.hybris.platform.ordersplitting.model.WarehouseModel}
	 *
	 * @param newConsignments
	 * 		the list of new consignments
	 * @param consignment
	 * 		the original {@link ConsignmentModel}
	 * @param order
	 * 		the related {@link AbstractOrderModel}
	 * @param event
	 * 		the {@link DeclineConsignmentEntryEventModel}
	 */
	protected void consolidateConsignmentEntries(final List<ConsignmentModel> newConsignments, final ConsignmentModel consignment,
			final AbstractOrderModel order, final DeclineConsignmentEntryEventModel event)
	{
		//Consolidates the consignmentEntries if they belong to the same warehouse
		final Optional<ConsignmentModel> optional = newConsignments.stream()
				.filter(newConsignment -> newConsignment.getWarehouse().equals(event.getReallocatedWarehouse())).findAny();
		if (optional.isPresent())
		{
			final ConsignmentModel newConsolidatedConsignment = optional.get();
			final ConsignmentEntryModel consignmentEntryModel = createConsignmentEntry(event.getConsignmentEntry().getOrderEntry(),
					event.getQuantity(), newConsolidatedConsignment);

			if (!consignmentEntryModel.getConsignment().getWarehouse().isExternal())
			{
				getInventoryEventService().createAllocationEventsForConsignmentEntry(consignmentEntryModel);
			}

			final Set<ConsignmentEntryModel> consEntries = new HashSet<>();
			newConsolidatedConsignment.getConsignmentEntries().forEach(consEntries::add);
			consEntries.add(consignmentEntryModel);

			newConsolidatedConsignment.setConsignmentEntries(consEntries);
		}
		else
		{
			final Map<AbstractOrderEntryModel, Long> allocation = new HashMap<>();
			allocation.put(event.getConsignmentEntry().getOrderEntry(), event.getQuantity());

			if (allocation.isEmpty())
			{
				throw new AllocationException("Unable to process reallocation since there is nothing to reallocate.");
			}

			// Create the new consignment by reusing the sourcing result based API
			final SourcingResult sourcingResult = new SourcingResult();
			sourcingResult.setAllocation(allocation);
			sourcingResult.setWarehouse(event.getReallocatedWarehouse());
			final ConsignmentModel newConsignment = this
					.createConsignment(order, consignment.getCode() + "_" + getNumDeclines(consignment), sourcingResult);

			newConsignments.add(newConsignment);
		}
	}

	@Override
	public void autoReallocate(final DeclineEntries declinedEntries)
	{
		validateParameterNotNullStandardMessage("declinedEntries", declinedEntries);
		isTrue(CollectionUtils.isNotEmpty(declinedEntries.getEntries()), "Entries cannot be null or empty.");

		final ConsignmentModel consignment = declinedEntries.getEntries().iterator().next().getConsignmentEntry().getConsignment();
		LOGGER.debug("Declining consignment with code : {}", consignment.getCode());

		// Create the consignment entry events representing the decline history + update Allocation InventoryEvent for the corresponding consignment
		declinedEntries.getEntries().forEach(this::declineConsignmentEntry);
	}

	/**
	 * Gets the number of decline entry event for the given consignment.
	 *
	 * @param consignment
	 * 		- the consignment
	 * @return number of decline entry event
	 */
	protected int getNumDeclines(final ConsignmentModel consignment)
	{
		return consignment.getConsignmentEntries().stream()
				.filter(entry -> CollectionUtils.isNotEmpty(entry.getDeclineEntryEvents()))
				.mapToInt(entry -> entry.getDeclineEntryEvents().size()).sum();
	}

	/**
	 * Decline a single consignment entry. This will decline as many quantities as are still pending. Any quantities that
	 * could not be declined will be ignored.
	 *
	 * @param declineEntry
	 * 		- the decline entry to process; contains the consignment entry, the new warehouse to which the items are being reallocated to and quantity to be declined
	 * @return the consignment entry event with the items that were successfully declined
	 */
	protected Optional<DeclineConsignmentEntryEventModel> declineConsignmentEntry(final DeclineEntry declineEntry)
	{
		// Only decline quantities that are still pending.
		final Long quantityToDecline = getQuantityToDecline(declineEntry);

		// Only save the decline if there are any quantities to decline
		if (quantityToDecline.longValue() > 0)
		{
			final ConsignmentEntryModel consignmentEntry = declineEntry.getConsignmentEntry();

			final DeclineConsignmentEntryEventModel event = getModelService().create(DeclineConsignmentEntryEventModel.class);
			event.setConsignmentEntry(consignmentEntry);
			event.setReason(declineEntry.getReason());
			event.setQuantity(quantityToDecline);
			event.setReallocatedWarehouse(declineEntry.getReallocationWarehouse());
			getModelService().save(event);

			if (!declineEntry.getConsignmentEntry().getConsignment().getWarehouse().isExternal())
			{
				reallocateAllocationEvent(declineEntry, quantityToDecline);
			}

			if (!Objects.isNull(declineEntry.getNotes()))
			{
				final WarehousingCommentContext commentContext = new WarehousingCommentContext();
				commentContext.setCommentType(WarehousingCommentEventType.REALLOCATE_CONSIGNMENT_COMMENT);
				commentContext.setItem(declineEntry.getConsignmentEntry());
				commentContext.setSubject(REALLOCATE_COMMENT_SUBJECT);
				commentContext.setText(declineEntry.getNotes());

				String code = "reallocation_" + getGuidKeyGenerator().generate().toString();
				getConsignmentEntryCommentService().createAndSaveComment(commentContext, code);
			}
			consignmentEntry.setQuantity(Long.valueOf(consignmentEntry.getQuantity().longValue() - quantityToDecline.longValue()));
			getModelService().save(consignmentEntry);
			return Optional.of(event);
		}

		return Optional.empty();
	}

	/**
	 * Get the quantity of items to decline.
	 *
	 * @param declineEntry
	 * 		- the consignment entry being declined
	 * @return returns the consignment entry pending quantity if it is greater than or equal to the decline entry's
	 * requested decline quantity; otherwise return the decline entry's requested decline quantity
	 */
	protected Long getQuantityToDecline(final DeclineEntry declineEntry)
	{
		getModelService().refresh(declineEntry.getConsignmentEntry());
		final ConsignmentEntryModel consignmentEntry = declineEntry.getConsignmentEntry();
		return declineEntry.getQuantity().longValue() <= consignmentEntry.getQuantity().longValue() ?
				declineEntry.getQuantity() :
				useQuantityPending(consignmentEntry, declineEntry);
	}

	protected Long useQuantityPending(ConsignmentEntryModel consignmentEntry, DeclineEntry declineEntry)
	{
		LOGGER.warn("You are trying to decline a quantity of: " + declineEntry.getQuantity() + " which is more than you "
				+ "are allowed, We will only decline the pending quantity of: " + consignmentEntry.getQuantity());
		return consignmentEntry.getQuantity();
	}

	/**
	 * Create a consignment entry.
	 *
	 * @param orderEntry
	 * 		- the order entry
	 * @param quantity
	 * 		- the ordered quantity
	 * @param consignment
	 * 		- the consignment to which the consignment entry will be added to
	 * @return the consignment entry; never <tt>null</tt>
	 */
	protected ConsignmentEntryModel createConsignmentEntry(final AbstractOrderEntryModel orderEntry, final Long quantity,
			final ConsignmentModel consignment)
	{
		LOGGER.debug("ConsignmentEntry :: Product [{}]: \tQuantity: '{}'", orderEntry.getProduct().getCode(), quantity);

		final ConsignmentEntryModel entry = getModelService().create(ConsignmentEntryModel.class);
		entry.setOrderEntry(orderEntry);
		entry.setQuantity(quantity);
		entry.setConsignment(consignment);

		final Set<ConsignmentEntryModel> consignmentEntries = new HashSet<>();

		if (orderEntry.getConsignmentEntries() != null)
		{
			orderEntry.getConsignmentEntries().forEach(consignmentEntries::add);
		}
		consignmentEntries.add(entry);

		orderEntry.setConsignmentEntries(consignmentEntries);

		return entry;
	}

	/**
	 * Delete or update the allocation event which has been created for a specific entry to decline
	 *
	 * @param declineEntry
	 * 		the entry to decline/reallocate
	 * @param quantityToDecline
	 * 		the quantity to decline/reallocate
	 */
	protected void reallocateAllocationEvent(final DeclineEntry declineEntry, final Long quantityToDecline)
	{
		LOGGER.debug(
				"Update or delete allocation event for a ConsignmentEntry to be declined :: Product [{}], at Warehouse [{}]: \tQuantity: '{}'",
				declineEntry.getConsignmentEntry().getOrderEntry().getProduct().getCode(),
				declineEntry.getConsignmentEntry().getConsignment().getWarehouse().getCode(), quantityToDecline);

		getInventoryEventService().reallocateAllocationEvent(declineEntry, quantityToDecline);
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

	protected DeliveryModeService getDeliveryModeService()
	{
		return deliveryModeService;
	}

	@Required
	public void setDeliveryModeService(final DeliveryModeService deliveryModeService)
	{
		this.deliveryModeService = deliveryModeService;
	}

	protected WarehousingCommentService getConsignmentEntryCommentService()
	{
		return consignmentEntryCommentService;
	}

	@Required
	public void setConsignmentEntryCommentService(final WarehousingCommentService consignmentEntryCommentService)
	{
		this.consignmentEntryCommentService = consignmentEntryCommentService;
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

	protected GuidKeyGenerator getGuidKeyGenerator()
	{
		return guidKeyGenerator;
	}

	@Required
	public void setGuidKeyGenerator(final GuidKeyGenerator guidKeyGenerator)
	{
		this.guidKeyGenerator = guidKeyGenerator;
	}

	protected ShippingDateStrategy getShippingDateStrategy()
	{
		return shippingDateStrategy;
	}

	public void setShippingDateStrategy(final ShippingDateStrategy shippingDateStrategy)
	{
		this.shippingDateStrategy = shippingDateStrategy;
	}

	protected WarehousingConsignmentWorkflowService getWarehousingConsignmentWorkflowService()
	{
		return warehousingConsignmentWorkflowService;
	}

	@Required
	public void setWarehousingConsignmentWorkflowService(
			final WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService)
	{
		this.warehousingConsignmentWorkflowService = warehousingConsignmentWorkflowService;
	}

	protected WarehousingFulfillmentConfigDao getWarehousingFulfillmentConfigDao()
	{
		return warehousingFulfillmentConfigDao;
	}

	@Required
	public void setWarehousingFulfillmentConfigDao(final WarehousingFulfillmentConfigDao warehousingFulfillmentConfigDao)
	{
		this.warehousingFulfillmentConfigDao = warehousingFulfillmentConfigDao;
	}

	protected StockLevelDao getStockLevelDao()
	{
		return stockLevelDao;
	}

	@Required
	public void setStockLevelDao(final StockLevelDao stockLevelDao)
	{
		this.stockLevelDao = stockLevelDao;
	}
}

