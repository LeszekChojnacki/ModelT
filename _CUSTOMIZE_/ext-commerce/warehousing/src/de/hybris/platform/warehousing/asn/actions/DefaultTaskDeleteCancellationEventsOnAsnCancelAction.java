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

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.asn.service.AsnService;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.model.CancellationEventModel;
import de.hybris.platform.warehousing.taskassignment.actions.AbstractTaskAssignmentActions;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * An automated Task to Delete {@link CancellationEventModel} for the {@link StockLevelModel}(s) from the cancelled {@link AdvancedShippingNoticeModel}
 */
public class DefaultTaskDeleteCancellationEventsOnAsnCancelAction extends AbstractTaskAssignmentActions
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskDeleteCancellationEventsOnAsnCancelAction.class);
	private InventoryEventService inventoryEventService;
	private AsnService asnService;

	@Override
	public WorkflowDecisionModel perform(final WorkflowActionModel workflowActionModel)
	{
		if (getAttachedAsn(workflowActionModel).isPresent())
		{
			final AdvancedShippingNoticeModel attachedAsn = (AdvancedShippingNoticeModel) getAttachedAsn(workflowActionModel).get();

			final List<StockLevelModel> stockLevels = getAsnService().getStockLevelsForAsn(attachedAsn);

			//Removing all cancellation events for each stocklevel
			stockLevels.forEach(stockLevelModel ->
			{
				final Collection<CancellationEventModel> cancellationEvents = getInventoryEventService()
						.getInventoryEventsForStockLevel(stockLevelModel, CancellationEventModel.class);
				getModelService().removeAll(cancellationEvents);
				LOGGER.info(cancellationEvents.size() + " Cancellation Events linked to " + stockLevelModel.getProductCode()
						+ " are being removed because ASN: " + attachedAsn.getInternalId() + " got cancelled");
			});
		}
		return workflowActionModel.getDecisions().isEmpty() ? null : workflowActionModel.getDecisions().iterator().next();
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
}
