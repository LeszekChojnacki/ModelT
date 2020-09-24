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
package de.hybris.platform.warehousing.asn.actions;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.warehousing.asn.service.AsnService;
import de.hybris.platform.warehousing.data.allocation.DeclineEntries;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.enums.DeclineReason;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.process.impl.DefaultConsignmentProcessService;
import de.hybris.platform.warehousing.taskassignment.actions.AbstractTaskAssignmentActions;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * An automated Task to Reallocate {@link ConsignmentModel} that were using the {@link StockLevelModel} from the cancelled {@link AdvancedShippingNoticeModel}
 * @deprecated since 18.11 - no replacement as this step of the workflow will be removed
 */
@Deprecated
public class DefaultTaskReallocateConsignmentsOnAsnCancelAction extends AbstractTaskAssignmentActions
{
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String REALLOCATE_CONSIGNMENT_CHOICE = "reallocateConsignment";
	protected static final String DECLINE_ENTRIES = "declineEntries";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskReallocateConsignmentsOnAsnCancelAction.class);
	private AsnService asnService;
	private InventoryEventService inventoryEventService;
	private List<ConsignmentStatus> reallocableConsignmentStatusList;

	@Override
	public WorkflowDecisionModel perform(final WorkflowActionModel workflowActionModel)
	{
		return workflowActionModel.getDecisions().isEmpty() ? null : workflowActionModel.getDecisions().iterator().next();
	}

	/**
	 * Reallocates {@link ConsignmentModel}(s) originally allocated from the stocklevels associated with the given {@link AdvancedShippingNoticeModel}
	 *
	 * @param attachedAsn
	 * 		the cancelled {@link AdvancedShippingNoticeModel}
	 * @param allocationEvents
	 * 		collection of {@link AllocationEventModel}(s) for which we want to reallocate consignments
	 * @param alreadyReallocatedConsignments
	 * 		set containing {@link ConsignmentModel#CODE}(s), which have already been reallocated for different {@link AllocationEventModel}
	 */
	protected void reallocateConsignments(final AdvancedShippingNoticeModel attachedAsn,
			final Collection<AllocationEventModel> allocationEvents, final Set<String> alreadyReallocatedConsignments)
	{
		allocationEvents.forEach(allocationEvent ->
		{
			final ConsignmentModel consignment = allocationEvent.getConsignmentEntry().getConsignment();

			if (getReallocableConsignmentStatusList().contains(consignment.getStatus()) && !alreadyReallocatedConsignments
					.contains(consignment.getCode()))
			{
				final List<DeclineEntry> declineEntries = new ArrayList<>();
				populateConsignmentEntries(consignment, declineEntries);
				final BusinessProcessModel businessProcess = ((DefaultConsignmentProcessService) getConsignmentBusinessProcessService())
						.getConsignmentProcess(consignment);
				buildDeclineParam((ConsignmentProcessModel) businessProcess, declineEntries);

				getConsignmentBusinessProcessService()
						.triggerChoiceEvent(consignment, CONSIGNMENT_ACTION_EVENT_NAME, REALLOCATE_CONSIGNMENT_CHOICE);

				LOGGER.info(
						"Consignment: " + consignment.getCode() + " is being reallocated because ASN: " + attachedAsn.getInternalId()
								+ "got cancelled");

				alreadyReallocatedConsignments.add(consignment.getCode());
			}
		});
	}

	/**
	 * Populates the consignment entries of a given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which entries needs to be populated
	 * @param declineEntries
	 * 		the list of {@link DeclineEntry} which contains the informations to populate
	 */
	protected void populateConsignmentEntries(final ConsignmentModel consignment, final List<DeclineEntry> declineEntries)
	{
		consignment.getConsignmentEntries().forEach(consignmentEntryModel ->
		{
			final DeclineEntry declineEntry = new DeclineEntry();
			declineEntry.setQuantity(consignmentEntryModel.getQuantity());
			declineEntry.setConsignmentEntry(consignmentEntryModel);

			declineEntry.setReason(DeclineReason.ASNCANCELLATION);
			declineEntries.add(declineEntry);
		});
	}


	/**
	 * Build and save the context parameter for decline entries and set it into the given process
	 *
	 * @param processModel
	 * 		{@link ConsignmentProcessModel}
	 * 		the process model for which the context parameters has to be register
	 * @param entriesToReallocate
	 * 		the {@link DeclineEntry}(s) to be reallocated
	 */
	protected void buildDeclineParam(final ConsignmentProcessModel processModel, final List<DeclineEntry> entriesToReallocate)
	{
		cleanDeclineParam(processModel);

		final DeclineEntries declinedEntries = new DeclineEntries();
		declinedEntries.setEntries(entriesToReallocate);

		final BusinessProcessParameterModel declineParam = new BusinessProcessParameterModel();
		declineParam.setName(DECLINE_ENTRIES);
		declineParam.setValue(declinedEntries);

		declineParam.setProcess(processModel);
		processModel.setContextParameters(Collections.singleton(declineParam));
		getModelService().save(processModel);
	}

	/**
	 * Removes the old decline entries from {@link ConsignmentProcessModel#CONTEXTPARAMETERS}(if any exists), before attempting to decline
	 *
	 * @param processModel
	 * 		the {@link ConsignmentProcessModel} for the consignment to be declined
	 */
	protected void cleanDeclineParam(final ConsignmentProcessModel processModel)
	{
		final Collection<BusinessProcessParameterModel> contextParams = new ArrayList<>();
		processModel.getContextParameters().forEach(param -> contextParams.add(param));
		if (CollectionUtils.isNotEmpty(contextParams))
		{
			final Optional<BusinessProcessParameterModel> declineEntriesParamOptional = contextParams.stream()
					.filter(param -> param.getName().equals(DECLINE_ENTRIES)).findFirst();
			if (declineEntriesParamOptional.isPresent())
			{
				final BusinessProcessParameterModel declineEntriesParam = declineEntriesParamOptional.get();
				contextParams.remove(declineEntriesParam);
				getModelService().remove(declineEntriesParam);
			}
		}
	}


	protected AsnService getAsnService()
	{
		return asnService;
	}

	@Required
	public void setAsnService(final AsnService asnService)
	{
		this.asnService = asnService;
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

	protected List<ConsignmentStatus> getReallocableConsignmentStatusList()
	{
		return reallocableConsignmentStatusList;
	}

	@Required
	public void setReallocableConsignmentStatusList(final List<ConsignmentStatus> reallocableConsignmentStatusList)
	{
		this.reallocableConsignmentStatusList = reallocableConsignmentStatusList;
	}
}
