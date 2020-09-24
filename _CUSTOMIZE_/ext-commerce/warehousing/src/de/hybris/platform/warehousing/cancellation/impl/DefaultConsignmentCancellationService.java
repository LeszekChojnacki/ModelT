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
package de.hybris.platform.warehousing.cancellation.impl;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.commerceservices.util.GuidKeyGenerator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.cancellation.ConsignmentCancellationService;
import de.hybris.platform.warehousing.comment.WarehousingCommentService;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentContext;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentEventType;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.CancellationEventModel;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.List;


/**
 * Cancellation service implementation that will create cancellation events associated with the consignment entries.
 */
public class DefaultConsignmentCancellationService implements ConsignmentCancellationService
{
	private static Logger LOGGER = LoggerFactory.getLogger(DefaultConsignmentCancellationService.class);

	protected static final String COMMENT_SUBJECT = "Cancel consignment";
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String CANCEL_CONSIGNMENT_CHOICE = "cancelConsignment";

	private WarehousingCommentService consignmentEntryCommentService;
	private InventoryEventService inventoryEventService;
	private ModelService modelService;
	private GuidKeyGenerator guidKeyGenerator;
	private List<ConsignmentStatus> nonCancellableConsignmentStatus;
	private WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;

	@Override
	public void processConsignmentCancellation(final OrderCancelResponse orderCancelResponse)
	{
		Map<AbstractOrderEntryModel, Long> orderCancelEntriesCompleted = new HashMap<>();

		for (OrderCancelEntry orderCancelEntry : orderCancelResponse.getEntriesToCancel())
		{
			Long alreadyCancelledQty = getAlreadyCancelledQty(orderCancelEntriesCompleted, orderCancelEntry);
			if (orderCancelEntry.getCancelQuantity() > alreadyCancelledQty.longValue())
			{
				moveProcessAndTerminateWorkflow(orderCancelResponse, orderCancelEntriesCompleted, orderCancelEntry);
			}
		}
	}

	/**
	 * Moves the consignment business process and terminates the consignment workflow
	 *
	 * @param orderCancelResponse
	 * 		the {@link OrderCancelResponse}
	 * @param orderCancelEntriesCompleted
	 * 		the map of the completed order cancel entries and their values
	 * @param orderCancelEntry
	 * 		the current {@link OrderCancelEntry}
	 */
	protected void moveProcessAndTerminateWorkflow(final OrderCancelResponse orderCancelResponse,
			final Map<AbstractOrderEntryModel, Long> orderCancelEntriesCompleted, final OrderCancelEntry orderCancelEntry)
	{
		for (ConsignmentEntryModel consignmentEntry : orderCancelEntry.getOrderEntry().getConsignmentEntries())
		{
			if (!getNonCancellableConsignmentStatus().contains(consignmentEntry.getConsignment().getStatus()))
			{
				consignmentEntry.getConsignment().setStatus(ConsignmentStatus.CANCELLED);
				getModelService().save(consignmentEntry.getConsignment());
				orderCancelEntriesCompleted.putAll(cancelConsignment(consignmentEntry.getConsignment(), orderCancelResponse));
				getConsignmentBusinessProcessService()
						.triggerChoiceEvent(consignmentEntry.getConsignment(), CONSIGNMENT_ACTION_EVENT_NAME,
								CANCEL_CONSIGNMENT_CHOICE);
				getWarehousingConsignmentWorkflowService().terminateConsignmentWorkflow(consignmentEntry.getConsignment());
			}
		}
	}

	/**
	 * Gets the already cancelled quantity
	 *
	 * @param orderCancelEntriesCompleted
	 * 		the map of the completed order cancel entries and their values
	 * @param orderCancelEntry
	 * 		the current {@link OrderCancelEntry}
	 * @return the calculated already cancelled quantity
	 */
	protected Long getAlreadyCancelledQty(final Map<AbstractOrderEntryModel, Long> orderCancelEntriesCompleted,
			final OrderCancelEntry orderCancelEntry)
	{
		Long alreadyCancelledQty = Long.valueOf(0L);

		for (Map.Entry<AbstractOrderEntryModel, Long> entry : orderCancelEntriesCompleted.entrySet())
		{
			if (entry.getKey().equals(orderCancelEntry.getOrderEntry()))
			{
				alreadyCancelledQty = Long.valueOf(alreadyCancelledQty.longValue() + entry.getValue().longValue());
			}
		}
		return alreadyCancelledQty;
	}

	@Override
	public Map<AbstractOrderEntryModel, Long> cancelConsignment(final ConsignmentModel consignment,
			final OrderCancelResponse orderCancelResponse)
	{
		LOGGER.debug("Cancel consignment with code: [{}]", consignment.getCode());

		Map<AbstractOrderEntryModel, Long> result = new HashMap<>();

		for (ConsignmentEntryModel consignmentEntry : consignment.getConsignmentEntries())
		{
			// Find cancel reason and notes
			final Optional<OrderCancelEntry> myEntry = orderCancelResponse.getEntriesToCancel().stream()
					.filter(entry -> entry.getOrderEntry().equals(consignmentEntry.getOrderEntry())).findFirst();
			final CancelReason myReason = (myEntry.isPresent()) ?
					myEntry.get().getCancelReason() :
					orderCancelResponse.getCancelReason();
			final String myNote = (myEntry.isPresent()) ? myEntry.get().getNotes() : orderCancelResponse.getNotes();

			createCancellationEventForInternalWarehouse(consignmentEntry, myReason);

			if (!Objects.isNull(myNote))
			{
				final WarehousingCommentContext commentContext = new WarehousingCommentContext();
				commentContext.setCommentType(WarehousingCommentEventType.CANCEL_CONSIGNMENT_COMMENT);
				commentContext.setItem(consignment);
				commentContext.setSubject(COMMENT_SUBJECT);
				commentContext.setText(myNote);

				final String code = "cancellation_" + getGuidKeyGenerator().generate().toString();
				getConsignmentEntryCommentService().createAndSaveComment(commentContext, code);
			}

			if (myEntry.isPresent())
			{
				result.put(consignmentEntry.getOrderEntry(), consignmentEntry.getQuantity());
			}
		}
		return result;
	}

	/**
	 * Create cancellation event in case the warehouse is not "marked" as external.
	 *
	 * @param consignmentEntry
	 * @param myReason
	 */
	protected void createCancellationEventForInternalWarehouse(final ConsignmentEntryModel consignmentEntry,
			final CancelReason myReason)
	{
		if (!consignmentEntry.getConsignment().getWarehouse().isExternal())
		{
			final CancellationEventModel event = new CancellationEventModel();
			event.setConsignmentEntry(consignmentEntry);
			event.setOrderEntry(getModelService().get(consignmentEntry.getOrderEntry().getPk()));
			event.setReason(myReason.getCode());
			event.setQuantity(consignmentEntry.getQuantity().longValue());

			getInventoryEventService().createCancellationEvents(event);
		}
	}

	protected WarehousingCommentService getConsignmentEntryCommentService()
	{
		return consignmentEntryCommentService;
	}

	@Required
	public void setConsignmentEntryCommentService(
			final WarehousingCommentService consignmentEntryCommentService)
	{
		this.consignmentEntryCommentService = consignmentEntryCommentService;
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

	protected InventoryEventService getInventoryEventService()
	{
		return inventoryEventService;
	}

	@Required
	public void setInventoryEventService(final InventoryEventService inventoryEventService)
	{
		this.inventoryEventService = inventoryEventService;
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

	protected List<ConsignmentStatus> getNonCancellableConsignmentStatus()
	{
		return nonCancellableConsignmentStatus;
	}

	@Required
	public void setNonCancellableConsignmentStatus(final List<ConsignmentStatus> nonCancellableConsignmentStatus)
	{
		this.nonCancellableConsignmentStatus = nonCancellableConsignmentStatus;
	}

	protected WarehousingBusinessProcessService<ConsignmentModel> getConsignmentBusinessProcessService()
	{
		return consignmentBusinessProcessService;
	}

	@Required
	public void setConsignmentBusinessProcessService(
			final WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService)
	{
		this.consignmentBusinessProcessService = consignmentBusinessProcessService;
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

}
