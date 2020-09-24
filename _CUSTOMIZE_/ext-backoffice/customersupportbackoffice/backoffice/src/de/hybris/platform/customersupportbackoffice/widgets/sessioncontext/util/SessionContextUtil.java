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
package de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.model.SessionContextModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hybris.cockpitng.core.model.WidgetModel;


/**
 *
 * Session Context Utility class with help functions to make it easier for Session Context manipulation
 *
 */
public class SessionContextUtil
{
	private SessionContextUtil()
	{
		// utility class. Prevent creation.
	}

	/**
	 * creates to return session context model
	 *
	 * @param model
	 *           model to retrieve session context from
	 * @return session context model
	 */
	public static SessionContextModel createOrReturnSessionContext(final WidgetModel model)
	{
		SessionContextModel sessionContext = getCurrentSessionContext(model);

		if (null == sessionContext)
		{
			sessionContext = new SessionContextModel();

			model.setValue(CustomersupportbackofficeConstants.SESSION_CONEXT_ID, sessionContext);
		}
		return sessionContext;
	}

	/**
	 * returns current session context model with populated info
	 *
	 * @param model
	 *           model to retrieve session context from
	 * @return session context model with populated info
	 */
	public static SessionContextModel getCurrentSessionContext(final WidgetModel model)
	{
		SessionContextModel sessionContext = null;

		if (null != model.getValue(CustomersupportbackofficeConstants.SESSION_CONEXT_ID, SessionContextModel.class))
		{
			sessionContext = model.getValue(CustomersupportbackofficeConstants.SESSION_CONEXT_ID, SessionContextModel.class);
		}

		return sessionContext;
	}

	/**
	 * clears current session context
	 *
	 * @param model
	 *           model to clear the session context from
	 */
	public static void clearSessionContext(final WidgetModel model)
	{
		model.remove(CustomersupportbackofficeConstants.SESSION_CONEXT_ID);
	}

	/**
	 * populates customer data inside the session context
	 *
	 * @param model
	 *           model to retrieve session context from
	 * @param customerModel
	 *           customer model to fetch the info from it and place it into the session context
	 */
	public static void populateCustomer(final WidgetModel model, final UserModel customerModel)
	{
		updateSessionContext(model, customerModel, null, null);
	}

	/**
	 * populates order data and its related data inside the session context
	 *
	 * @param model
	 *           model to retrieve session context from
	 * @param orderModel
	 *           order model to fetch the info from it and place it into the session context
	 */
	public static void populateOrder(final WidgetModel model, final OrderModel orderModel)
	{
		updateSessionContext(model, orderModel.getUser(), null, orderModel);
	}

	/**
	 * populates ticket data and its related data inside the session context
	 *
	 * @param model
	 *           model to retrieve session context from
	 * @param ticketModel
	 *           ticket model to fetch the info from it and place it into the session context
	 */
	public static void populateTicket(final WidgetModel model, final CsTicketModel ticketModel)
	{
		AbstractOrderModel orderModel = null;

		if (null != ticketModel.getOrder() && ticketModel.getOrder() instanceof OrderModel)
		{
			orderModel = ticketModel.getOrder();
		}

		updateSessionContext(model, ticketModel.getCustomer(), ticketModel, orderModel);
	}

	//do the actual session context update
	public static void updateSessionContext(final WidgetModel model, final UserModel customerModel,
			final CsTicketModel ticketModel, final AbstractOrderModel orderModel)
	{

		final SessionContextModel sessionContext = createOrReturnSessionContext(model);

		sessionContext.setCurrentTicket(ticketModel);

		sessionContext.setCurrentCustomer(customerModel);

		sessionContext.setCurrentOrder(orderModel);
		//if we have returns then populate the session context order returns map
		if (null != orderModel && orderModel instanceof OrderModel && (null != ((OrderModel) orderModel).getReturnRequests()))
		{
			final List<ReturnRequestModel> returns = ((OrderModel) orderModel).getReturnRequests();

			final Map<String, ReturnRequestModel> returnsMap = returns.stream().collect(
					Collectors.toMap(ReturnRequestModel::getCode, Function.identity()));

			sessionContext.setCurrentOrderReturns(returnsMap);
		}
		else
		{
			sessionContext.setCurrentOrderReturns(null);
		}
	}
}
