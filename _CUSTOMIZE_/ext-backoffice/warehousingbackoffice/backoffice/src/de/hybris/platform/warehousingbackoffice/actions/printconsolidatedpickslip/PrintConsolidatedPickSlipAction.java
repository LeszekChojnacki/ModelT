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
package de.hybris.platform.warehousingbackoffice.actions.printconsolidatedpickslip;


import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousingbackoffice.labels.strategy.impl.ConsolidatedConsignmentPrintPickSlipStrategy;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.apache.commons.collections4.CollectionUtils;


/**
 * A cockpit action to handle the printing of a consolidated pick slip.
 */
public class PrintConsolidatedPickSlipAction implements CockpitAction<Collection<Object>, Collection<Object>>
{
	protected static final String PICKING_STATUS = "NPR_Picking";

	@Resource
	protected ConsolidatedConsignmentPrintPickSlipStrategy consignmentsPrintPickSlipStrategy;

	@Override
	public boolean canPerform(final ActionContext<Collection<Object>> actionContext)
	{
		boolean returnValue = false;
		boolean isFulfillmentInternal;
		if (actionContext != null && CollectionUtils.isNotEmpty(actionContext.getData()))
		{
			final Collection<Object> items = actionContext.getData();

			// Check if we have only consignments with the same site and are internally fulfilled
			if (items.iterator().next() instanceof WorkflowActionModel)
			{
				final Collection<WorkflowActionModel> workflowActions = convertItemCollection(items);

				final BaseSiteModel firstSite = ((ConsignmentModel) workflowActions.iterator().next().getAttachmentItems().iterator()
						.next()).getOrder().getSite();
				isFulfillmentInternal = workflowActions.stream().allMatch(workflowActionModel ->
						((ConsignmentModel) workflowActionModel.getAttachmentItems().iterator().next()).getFulfillmentSystemConfig()
								== null);
				returnValue = workflowActions.stream().allMatch(
						workflowActionModel -> PICKING_STATUS.equals(workflowActionModel.getTemplate().getCode())
								&& ((ConsignmentModel) workflowActionModel.getAttachmentItems().iterator().next()).getOrder().getSite()
								.equals(firstSite)) && isFulfillmentInternal;
			}
			else if (items.iterator().next() instanceof ConsignmentModel)
			{
				final Collection<ConsignmentModel> consignments = convertItemCollection(items);
				final BaseSiteModel firstSite = consignments.iterator().next().getOrder().getSite();
				isFulfillmentInternal = consignments.stream()
						.allMatch(consignment -> consignment.getFulfillmentSystemConfig() == null);
				returnValue = consignments.stream().allMatch(consignment -> consignment.getOrder().getSite().equals(firstSite))
						&& isFulfillmentInternal;
			}
		}
		return returnValue;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<Collection<Object>> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<Collection<Object>> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<Collection<Object>> perform(final ActionContext<Collection<Object>> actionContext)
	{
		final List<ConsignmentModel> consignments = new ArrayList<>();
		if (actionContext.getData().iterator().next() instanceof WorkflowActionModel)
		{
			final Collection<WorkflowActionModel> workflowActions = convertItemCollection(actionContext.getData());
			workflowActions.forEach(workflowActionModel -> consignments
					.add((ConsignmentModel) workflowActionModel.getAttachmentItems().iterator().next()));
		}
		else
		{
			consignments.addAll(convertItemCollection(actionContext.getData()));
		}

		getConsignmentsPrintPickSlipStrategy().printDocument(consignments);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	/**
	 * Converts the received collection of objects to a collection of the specified class type.
	 *
	 * @param itemCollection
	 * 		the {@link Collection<Object>} to convert
	 * @param <T>
	 * 		the actual class type
	 * @return the newly populated collection
	 */
	protected <T> Collection<T> convertItemCollection(final Collection<Object> itemCollection)
	{
		final List<T> convertedCollection = new ArrayList<>();
		itemCollection.forEach(item -> convertedCollection.add((T) item));

		return convertedCollection;
	}

	protected ConsolidatedConsignmentPrintPickSlipStrategy getConsignmentsPrintPickSlipStrategy()
	{
		return consignmentsPrintPickSlipStrategy;
	}
}
