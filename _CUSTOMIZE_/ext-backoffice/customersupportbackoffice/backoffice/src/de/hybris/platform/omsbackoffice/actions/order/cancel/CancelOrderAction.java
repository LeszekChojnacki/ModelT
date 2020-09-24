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
package de.hybris.platform.omsbackoffice.actions.order.cancel;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import java.util.List;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections.CollectionUtils;


/**
 * This action opens a popup to cancel an {@link OrderModel}.
 */
public class CancelOrderAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<OrderModel, OrderModel>
{
	protected static final String SOCKET_OUT_CONTEXT = "cancelOrderContext";
	protected static final String CAPTURE_PAYMENT_ON_CONSIGNMENT = "warehousing.capturepaymentonconsignment";

	@Resource
	private UserService userService;
	@Resource
	private OrderCancelService orderCancelService;
	@Resource
	private List<OrderStatus> notCancellableOrderStatus;
	@Resource
	private List<ConsignmentStatus> notCancellableConsignmentStatus;
	@Resource
	private ConfigurationService configurationService;

	@Override
	public boolean canPerform(final ActionContext<OrderModel> actionContext)
	{
		final OrderModel order = actionContext.getData();

		return order != null && !CollectionUtils.isEmpty(order.getEntries()) && getOrderCancelService()
				.isCancelPossible(order, getUserService().getCurrentUser(), true, true).isAllowed() && !getNotCancellableOrderStatus()
				.contains(order.getStatus());
	}

	@Override
	public String getConfirmationMessage(final ActionContext<OrderModel> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<OrderModel> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<OrderModel> perform(final ActionContext<OrderModel> actionContext)
	{
		sendOutput(SOCKET_OUT_CONTEXT, actionContext.getData());
		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected OrderCancelService getOrderCancelService()
	{
		return orderCancelService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	protected List<OrderStatus> getNotCancellableOrderStatus()
	{
		return notCancellableOrderStatus;
	}

	protected List<ConsignmentStatus> getNotCancellableConsignmentStatus()
	{
		return notCancellableConsignmentStatus;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

}
