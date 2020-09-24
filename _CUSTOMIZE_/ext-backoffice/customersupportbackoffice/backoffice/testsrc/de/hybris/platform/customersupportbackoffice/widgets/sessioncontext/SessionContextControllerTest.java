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
/**
 *
 */
package de.hybris.platform.customersupportbackoffice.widgets.sessioncontext;

import com.hybris.cockpitng.testing.annotation.*;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvents;
import com.hybris.cockpitng.testing.annotation.SocketsAreJsonSerializable;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.SessionContextController;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util.SessionContextUtil;
import de.hybris.platform.ticket.model.CsTicketModel;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;


/**
 * @author D062720
 *
 */

@DeclaredViewEvents(
{ @DeclaredViewEvent(componentID = "userAnchor", eventName = Events.ON_CLICK),
		@DeclaredViewEvent(componentID = "ticketAnchor", eventName = Events.ON_CLICK),
		@DeclaredViewEvent(componentID = "orderAnchor", eventName = Events.ON_CLICK),
		@DeclaredViewEvent(componentID = "endSessionBtn", eventName = Events.ON_CLICK) })
@DeclaredInputs({
		@DeclaredInput(value = "selectedItem", socketType = Object.class),
		@DeclaredInput(value = "itemCreated", socketType = Object.class) })
@SocketsAreJsonSerializable(false)
public class SessionContextControllerTest extends AbstractWidgetUnitTest<SessionContextController>
{
	@Mock
	protected A userAnchor;
	@Mock
	protected A ticketAnchor;
	@Mock
	protected A orderAnchor;

	@Mock
	protected Button endSessionBtn;

	@Mock
	protected Label customerLabel;
	@Mock
	protected Label ticketLabel;
	@Mock
	protected Label orderLabel;

	@Mock
	protected Image userImage;
	@Mock
	protected Image ticketImage;
	@Mock
	protected Image orderImage;


	@InjectMocks
	private final SessionContextController sessionContextController = new SessionContextController();

	@Override
	protected SessionContextController getWidgetController()
	{
		return sessionContextController;
	}

	@Test
	public void testInputSocketForCustomer()
	{
		final CustomerModel customer = new CustomerModel();
		customer.setName("Customer 1");

		executeInputSocketEvent(CustomersupportbackofficeConstants.SELECTED_ITEM_SOCKET, customer);

		assertValueNotNull(CustomersupportbackofficeConstants.SESSION_CONEXT_ID);
		Assert.assertNotNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentCustomer());
		Assert.assertNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentOrder());
	}


	@Test
	public void testInputSocketForOrder()
	{
		final CustomerModel customer = new CustomerModel();
		customer.setName("Customer 2");

		final OrderModel order = new OrderModel();

		order.setUser(customer);
		order.setCode("1000");

		executeInputSocketEvent(CustomersupportbackofficeConstants.SELECTED_ITEM_SOCKET, order);

		assertValueNotNull(CustomersupportbackofficeConstants.SESSION_CONEXT_ID);
		Assert.assertNotNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentCustomer());
		Assert.assertNotNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentOrder());
		final OrderModel sessionOrder = (OrderModel) SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel())
				.getCurrentOrder();

		Assert.assertEquals(sessionOrder.getCode(), order.getCode());
	}


	@Test
	public void testInputSocketForTicket()
	{
		final CustomerModel customer = new CustomerModel();
		customer.setName("Customer 3");

		final OrderModel order = new OrderModel();

		order.setUser(customer);
		order.setCode("1001");

		final CsTicketModel ticket = new CsTicketModel();

		ticket.setTicketID("31133");
		ticket.setOrder(order);
		ticket.setCustomer(customer);

		executeInputSocketEvent(CustomersupportbackofficeConstants.SELECTED_ITEM_SOCKET, ticket);

		assertValueNotNull(CustomersupportbackofficeConstants.SESSION_CONEXT_ID);
		Assert.assertNotNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentCustomer());
		Assert.assertNotNull(SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel()).getCurrentOrder());


		final CsTicketModel sessionTicket = SessionContextUtil.getCurrentSessionContext(getWidgetController().getModel())
				.getCurrentTicket();

		final CustomerModel sessionCustomer = (CustomerModel) SessionContextUtil.getCurrentSessionContext(
				getWidgetController().getModel()).getCurrentCustomer();

		Assert.assertEquals(sessionTicket.getOrder().getCode(), order.getCode());
		Assert.assertEquals(sessionTicket.getTicketID(), ticket.getTicketID());
		Assert.assertEquals(customer.getName(), sessionCustomer.getName());
	}
}
