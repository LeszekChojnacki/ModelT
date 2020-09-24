/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.actions.order;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections4.CollectionUtils;

import static java.util.Collections.EMPTY_LIST;


/**
 * This action puts a given {@link OrderModel} On Hold, meaning no fulfillment can be performed
 * until it is released. An {@link OrderModel} can be put On Hold if it hasn't been Completed or Cancelled.
 */
public class PutOnHoldAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<OrderModel, OrderModel>
{
	protected static final String SOCKET_OUT_CONTEXT = "putOnHoldContext";

	@Resource
	private List<OrderStatus> onHoldableOrderStatusList;

	@Resource
	private List<ConsignmentStatus> notCancellableConsignmentStatus;

	@Override
	public ActionResult<OrderModel> perform(final ActionContext<OrderModel> actionContext)
	{
		final OrderModel order = actionContext.getData();
		sendOutput(SOCKET_OUT_CONTEXT, order);

		final ActionResult<OrderModel> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);

		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<OrderModel> actionContext)
	{
		final OrderModel order = actionContext.getData();

		return order != null && getOnHoldableOrderStatusList().contains(order.getStatus()) && order.getConsignments() != null
				&& order.getConsignments().stream()
				.anyMatch(consignment -> !getNotCancellableConsignmentStatus().contains(consignment.getStatus())) && order
				.getEntries().stream().anyMatch(orderEntry -> getQuantityPending(orderEntry) > 0);
	}

	/**
	 * Returns the quantity for the given {@link AbstractOrderEntryModel} which has not been shipped or picked up
	 *
	 * @param orderEntryModel
	 * 		the given {@link AbstractOrderEntryModel}
	 * @return the pending quantity
	 */
	protected Long getQuantityPending(final AbstractOrderEntryModel orderEntryModel)
	{
		return Long.valueOf(orderEntryModel.getQuantity().longValue() - getQuantityShipped(orderEntryModel).longValue());
	}

	/**
	 * Returns the shipped Quantity for the given {@link AbstractOrderEntryModel}
	 *
	 * @param orderEntryModel
	 * 		the given {@link AbstractOrderEntryModel}
	 * @return the shipped quantity
	 */
	protected Long getQuantityShipped(final AbstractOrderEntryModel orderEntryModel)
	{
		long shippedquantity = 0L;
		if (CollectionUtils.isNotEmpty(orderEntryModel.getConsignmentEntries()))
		{
			final Collection<ConsignmentStatus> confirmedConsignmentStatus = Arrays
					.asList(ConsignmentStatus.SHIPPED, ConsignmentStatus.PICKUP_COMPLETE);
			shippedquantity = orderEntryModel.getConsignmentEntries().stream()
					.filter(consignmentEntry -> confirmedConsignmentStatus.contains(consignmentEntry.getConsignment().getStatus()))
					.mapToLong(consEntry -> consEntry.getQuantity()).sum();
		}
		return shippedquantity;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrderModel> actionContext)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrderModel> actionContext)
	{
		return null;
	}

	protected List<OrderStatus> getOnHoldableOrderStatusList()
	{
		return CollectionUtils.isEmpty(onHoldableOrderStatusList) ? EMPTY_LIST : onHoldableOrderStatusList;
	}

	protected List<ConsignmentStatus> getNotCancellableConsignmentStatus()
	{
		return CollectionUtils.isEmpty(notCancellableConsignmentStatus) ? EMPTY_LIST : notCancellableConsignmentStatus;
	}
}
