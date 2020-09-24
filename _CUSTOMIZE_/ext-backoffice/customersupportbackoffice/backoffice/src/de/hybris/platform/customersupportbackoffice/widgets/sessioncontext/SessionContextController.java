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
package de.hybris.platform.customersupportbackoffice.widgets.sessioncontext;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.util.AbstractComparator;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.model.SessionContextModel;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util.AsmUtils;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util.SessionContextUtil;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.ticket.model.CsTicketModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Stopwatch;
import com.hybris.cockpitng.util.CockpitSessionService;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 *
 * controller class for handling Session Context widget interactions
 *
 */
public class SessionContextController extends DefaultWidgetController
{
	//UI Components part
	protected A userAnchor;
	protected A ticketAnchor;
	protected A orderAnchor;

	protected Button endSessionBtn;
	protected Button asmBtn;
	protected Button callContextBtn;

	protected Label customerPlaceholder;
	protected Label ticketPlaceholder;
	protected Label orderPlaceholder;

	protected Image userImage;
	protected Image ticketImage;
	protected Image orderImage;

	protected Combobox availableSites;

	protected Stopwatch stopWatch;
	protected Div sessionTimerDiv;

	protected static final String USER_ANCHOR_COMPONENT_ID = "userAnchor";
	protected static final String TICKET_ANCHOR_COMPONENT_ID = "ticketAnchor";
	protected static final String ORDER_ANCHOR_COMPONENT_ID = "orderAnchor";
	protected static final String END_SESSION_BUTTON_COMPONENT_ID = "endSessionBtn";
	protected static final String SESSION_CALL_BUTTON_COMPONENT_ID = "callContextBtn";
	protected static final String ASM_LAUNCH_BUTTON_COMPONENT_ID = "asmBtn";

	protected static final String START_CALL_LABEL_KEY = "sessionContext.call.button.start";
	protected static final String END_CALL_LABEL_KEY = "sessionContext.call.button.end";
	protected static final String CURRENT_SESSION_CALL_MODE = "enabled.call.mode";
	protected static final String IN_CALL_MODE = "in.call";
	protected static final String FREE_CALL_MODE = "free";

	//CSS Classes
	protected static final String START_CALL_CSS_CLASS = "y-start-call-btn";
	protected static final String END_CALL_CSS_CLASS = "y-end-call-btn";
	protected static final String ANCHOR_CSS_CLASS = "y-session-context-label-value";

	@Resource
	private transient BaseSiteService baseSiteService;

	@Resource
	private transient CockpitSessionService cockpitSessionService;

	protected final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("mm 'min' ':' ss 'sec'");

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

		final ListModelList<BaseSiteModel> model = getAvailableSites();
		if (null != model && model.getSize() > 0)
		{
			model.addToSelection(model.get(0));
		}
		else
		{
			asmBtn.setVisible(false);
		}

		availableSites.setModel(model);
	}

	@ViewEvent(componentID = USER_ANCHOR_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void getUserDetails()
	{
		final UserModel user = SessionContextUtil.createOrReturnSessionContext(getWidgetInstanceManager().getModel())
				.getCurrentCustomer();
		if (null != user)
		{
			sendOutput(CustomersupportbackofficeConstants.ITEM_TO_SHOW_SOCKET, user);
		}
	}

	@ViewEvent(componentID = TICKET_ANCHOR_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void getTicketDetails()
	{
		final CsTicketModel ticket = SessionContextUtil.createOrReturnSessionContext(getWidgetInstanceManager().getModel())
				.getCurrentTicket();

		if (null != ticket)
		{
			sendOutput(CustomersupportbackofficeConstants.ITEM_TO_SHOW_SOCKET, ticket);
		}
	}

	@ViewEvent(componentID = ORDER_ANCHOR_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void getOrderDetails()
	{

		final AbstractOrderModel order = SessionContextUtil.createOrReturnSessionContext(getWidgetInstanceManager().getModel())
				.getCurrentOrder();

		if (null != order)
		{
			sendOutput(CustomersupportbackofficeConstants.ITEM_TO_SHOW_SOCKET, order);
		}
	}

	@SocketEvent(socketId = CustomersupportbackofficeConstants.SELECTED_ITEM_SOCKET)
	public void itemSelected(final Object msg)
	{
		// it have input from simpleSelect widget, and can receive some extra object, that should be ignored
		if (!(msg instanceof CustomerModel) && !(msg instanceof OrderModel) && !(msg instanceof ReturnRequestModel)
				&& !(msg instanceof CsTicketModel))
		{
			return;
		}
		//we made a soft end session, we need to reset stuff
		resetToDefault();
		String currentSessionCustomerId = "";

		final SessionContextModel currentContextSession = SessionContextUtil
				.getCurrentSessionContext(getWidgetInstanceManager().getModel());

		if (msg instanceof CustomerModel)
		{
			final CustomerModel customer = (CustomerModel) msg;
			currentSessionCustomerId = customerSelected(customer, currentContextSession);
		}

		else if (msg instanceof CsTicketModel)
		{
			final CsTicketModel currentTicket = (CsTicketModel) msg;
			currentSessionCustomerId = currentTicketSelected(currentTicket, currentContextSession);
		}

		else if (msg instanceof OrderModel || msg instanceof ReturnRequestModel)
		{
			currentSessionCustomerId = currentOrderSelected(msg, currentContextSession);
		}

		callContextBtn.setAttribute(CURRENT_SESSION_CALL_MODE, FREE_CALL_MODE);

		handleUIComponents();
		handleSessionCallMode();
		cockpitSessionService.setAttribute(CustomersupportbackofficeConstants.SESSION_CONTEXT_UID_SESSION_ATTR,
				currentSessionCustomerId);

		sessionTimerDiv.setVisible(true);
	}

	private String customerSelected(CustomerModel customer, SessionContextModel currentContextSession)
	{
		userAnchor.setLabel(customer.getName());

		if (null == currentContextSession || !customer.equals(currentContextSession.getCurrentCustomer()))
		{
			SessionContextUtil.populateCustomer(getWidgetInstanceManager().getModel(), customer);

			restartTimer();
		}

		return customer.getUid();
	}

	private String currentTicketSelected(CsTicketModel currentTicket, SessionContextModel currentContextSession)
	{
		String currentSessionCustomerId = "";

		//keep tracking of previous customer to check if we should restart the session timer or not
		UserModel previousCustomer = null;

		if (null != currentContextSession)
		{
			previousCustomer = currentContextSession.getCurrentCustomer();
		}

		SessionContextUtil.populateTicket(getWidgetInstanceManager().getModel(), currentTicket);

		if (null != currentTicket.getOrder() && currentTicket.getOrder() instanceof OrderModel)
		{
			orderAnchor.setLabel(currentTicket.getOrder().getCode());
		}

		if (null != currentTicket.getCustomer())
		{
			userAnchor.setLabel(currentTicket.getCustomer().getName());

			currentSessionCustomerId = currentTicket.getCustomer().getUid();
			if (null == previousCustomer || !previousCustomer.getUid().equals(currentSessionCustomerId))
			{
				restartTimer();
			}
		}
		ticketAnchor.setLabel(currentTicket.getTicketID());

		handleSiteUpdate(currentTicket.getBaseSite());

		return currentSessionCustomerId;
	}

	private String currentOrderSelected(Object msg, SessionContextModel currentContextSession)
	{
		String currentSessionCustomerId = "";

		//keep tracking of previous customer to check if we should restart the session timer or not
		UserModel previousCustomer = null;

		OrderModel currentOrder = null;
		if (msg instanceof ReturnRequestModel)
		{
			final ReturnRequestModel returnModel = (ReturnRequestModel) msg;
			currentOrder = returnModel.getOrder();
		}
		else
		{
			currentOrder = (OrderModel) msg;
		}

		if (null != currentContextSession)
		{
			previousCustomer = currentContextSession.getCurrentCustomer();
		}

		SessionContextUtil.populateOrder(getWidgetInstanceManager().getModel(), currentOrder);

		userAnchor.setLabel(currentOrder.getUser().getName());

		orderAnchor.setLabel(currentOrder.getCode());

		currentSessionCustomerId = currentOrder.getUser().getUid();
		if (null == previousCustomer || !previousCustomer.getUid().equals(currentSessionCustomerId))
		{
			restartTimer();
		}
		handleSiteUpdate(currentOrder.getSite());

		return currentSessionCustomerId;
	}

	@ViewEvent(componentID = END_SESSION_BUTTON_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void endCurrentSession()
	{
		//this is a hard end session, everything should be reset to default
		SessionContextUtil.clearSessionContext(getWidgetInstanceManager().getModel());

		resetToDefault();

		handleUIComponents();

		sendOutput(CustomersupportbackofficeConstants.ITEM_TO_SHOW_SOCKET, null);
		sendOutput(CustomersupportbackofficeConstants.SEARCH_VIEW_TYPE_SOCKET, null);

		stopWatch.stop();
		sessionTimerDiv.setVisible(false);

		cockpitSessionService.removeAttribute(CustomersupportbackofficeConstants.SESSION_CONTEXT_UID_SESSION_ATTR);
	}

	@SocketEvent(socketId = CustomersupportbackofficeConstants.CREATED_ITEM_SOCKET)
	public void itemCreated(final Object msg)
	{
		if (msg instanceof CsTicketModel || msg instanceof CustomerModel)
		{
			itemSelected(msg);
			sendOutput(CustomersupportbackofficeConstants.ITEM_TO_SHOW_SOCKET, msg);
		}
	}

	protected void restartTimer()
	{
		stopWatch.reset();
		stopWatch.start();

	}

	protected void resetToDefault()
	{
		callContextBtn.removeAttribute(CURRENT_SESSION_CALL_MODE);

		userAnchor.setLabel(null);
		ticketAnchor.setLabel(null);
		orderAnchor.setLabel(null);

		availableSites.setDisabled(false);
		if (availableSites.getModel().getSize() > 0)
		{
			availableSites.setSelectedIndex(0);
		}
	}

	protected void handleSiteUpdate(final BaseSiteModel baseSiteModel)
	{
		if (baseSiteModel == null)
		{
			availableSites.setDisabled(true);
			return;
		}

		final ListModel<BaseSiteModel> sites = availableSites.getModel();
		for (int i = 0; i < sites.getSize(); i++)
		{
			if (sites.getElementAt(i).getUid().equals(baseSiteModel.getUid()))
			{
				availableSites.setSelectedIndex(i);
				availableSites.setDisabled(true);
				break;
			}
		}
	}

	@ViewEvent(componentID = SESSION_CALL_BUTTON_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void handleSessionCall()
	{
		handleSessionCallMode();
	}

	@ViewEvent(componentID = ASM_LAUNCH_BUTTON_COMPONENT_ID, eventName = Events.ON_CLICK)
	public void launchASM()
	{
		final SessionContextModel currentSessionContext = SessionContextUtil.getCurrentSessionContext(getWidgetInstanceManager()
				.getModel());

		Executions.getCurrent().sendRedirect(
				AsmUtils.getAsmDeepLink((BaseSiteModel) availableSites.getModel().getElementAt(availableSites.getSelectedIndex()),
						currentSessionContext), "_blank");
	}

	public boolean showASMButton()
	{
		return AsmUtils.showAsmButton();
	}

	/**
	 * show/hide UI components
	 */
	protected void handleUIComponents()
	{
		final SessionContextModel currentSessionContext = SessionContextUtil.getCurrentSessionContext(getWidgetInstanceManager()
				.getModel());


		endSessionBtn.setVisible(null != currentSessionContext);

		callContextBtn.setVisible(null != currentSessionContext);

		customerPlaceholder.setVisible(null == currentSessionContext || null == currentSessionContext.getCurrentCustomer());

		ticketPlaceholder.setVisible(null == currentSessionContext || null == currentSessionContext.getCurrentTicket());

		orderPlaceholder
				.setVisible(null == currentSessionContext || null == currentSessionContext.getCurrentOrder());
	}

	protected void handleSessionCallMode()
	{
		if (null != callContextBtn.getAttribute(CURRENT_SESSION_CALL_MODE)
				&& callContextBtn.getAttribute(CURRENT_SESSION_CALL_MODE).equals(IN_CALL_MODE))
		{
			callContextBtn.setLabel(getWidgetInstanceManager().getLabel(END_CALL_LABEL_KEY));
			callContextBtn.setAttribute(CURRENT_SESSION_CALL_MODE, FREE_CALL_MODE);
			callContextBtn.setSclass(END_CALL_CSS_CLASS);
		}
		else
		{
			callContextBtn.setLabel(getWidgetInstanceManager().getLabel(START_CALL_LABEL_KEY));
			callContextBtn.setAttribute(CURRENT_SESSION_CALL_MODE, IN_CALL_MODE);
			callContextBtn.setSclass(START_CALL_CSS_CLASS);
		}
	}

	/**
	 * return current available sites in the system
	 *
	 * @return list of sites
	 */
	protected ListModelList<BaseSiteModel> getAvailableSites()
	{
		final ListModelList model = new ListModelList();
		final Collection<BaseSiteModel> allBaseSites = baseSiteService.getAllBaseSites();
		if (CollectionUtils.isNotEmpty(allBaseSites))
		{
			final List<BaseSiteModel> availableBaseSites = new ArrayList<BaseSiteModel>(allBaseSites.size());

			for (final BaseSiteModel baseSite : allBaseSites)
			{
				availableBaseSites.add(baseSite);
			}

			// Sort the stores by name
			Collections.sort(availableBaseSites, new AbstractComparator<BaseSiteModel>()
			{
				@Override
				protected int compareInstances(final BaseSiteModel instance1, final BaseSiteModel instance2)
				{
					return new AbstractComparator<String>()
					{
						@Override
						protected int compareInstances(final String instance1, final String instance2)
						{
							final int result = instance1.compareToIgnoreCase(instance2);
							if (result == EQUAL)
							{
								return instance1.compareTo(instance2);
							}
							return result;
						}
					}.compare(instance1.getName(), instance2.getName());
				}
			});

			model.addAll(availableBaseSites);
		}
		return model;
	}
}
