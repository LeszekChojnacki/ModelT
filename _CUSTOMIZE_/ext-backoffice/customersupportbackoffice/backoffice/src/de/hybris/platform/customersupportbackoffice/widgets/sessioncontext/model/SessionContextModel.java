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
package de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.model;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.util.Map;


public class SessionContextModel
{

	private UserModel currentCustomer;
	private CsTicketModel currentTicket;
	private AbstractOrderModel currentOrder;
	private Map<String, ReturnRequestModel> currentOrderReturns;

	/**
	 * @return the currentCustomer
	 */
	public UserModel getCurrentCustomer()
	{
		return currentCustomer;
	}

	/**
	 * @param currentCustomer
	 *           the currentCustomer to set
	 */
	public void setCurrentCustomer(final UserModel currentCustomer)
	{
		this.currentCustomer = currentCustomer;
	}

	/**
	 * @return the currentTicket
	 */
	public CsTicketModel getCurrentTicket()
	{
		return currentTicket;
	}

	/**
	 * @param currentTicket
	 *           the currentTicket to set
	 */
	public void setCurrentTicket(final CsTicketModel currentTicket)
	{
		this.currentTicket = currentTicket;
	}

	/**
	 * @return the currentOrder
	 */
	public AbstractOrderModel getCurrentOrder()
	{
		return currentOrder;
	}

	/**
	 * @param currentOrder
	 *           the currentOrder to set
	 */
	public void setCurrentOrder(final AbstractOrderModel currentOrder)
	{
		this.currentOrder = currentOrder;
	}

	/**
	 * @return the currentOrderReturns
	 */
	public Map<String, ReturnRequestModel> getCurrentOrderReturns()
	{
		return currentOrderReturns;
	}

	/**
	 * @param currentOrderReturns
	 *           the currentOrderReturns to set
	 */
	public void setCurrentOrderReturns(final Map<String, ReturnRequestModel> currentOrderReturns)
	{
		this.currentOrderReturns = currentOrderReturns;
	}
}
