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
package de.hybris.platform.omsbackoffice.widgets.returns.cancelreturnpopup;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.returns.OrderReturnException;
import de.hybris.platform.returns.ReturnActionResponse;
import de.hybris.platform.returns.ReturnCallbackService;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.hybris.backoffice.i18n.BackofficeLocaleService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Controller for the cancellation return pop up
 */
public class ReturnCancellationController extends DefaultWidgetController
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCancellationController.class);

	protected static final String IN_SOCKET = "cancelReturnContextInput";
	protected static final String OUT_CONFIRM = "cancelReturnContext";
	protected static final Object COMPLETED = "completed";

	private ReturnRequestModel returnRequest;
	private final List<String> cancellationReasons = new ArrayList<>();

	@Wire
	private Textbox returnRequestCode;
	@Wire
	private Textbox customerName;
	@Wire
	private Combobox globalCancelReasons;
	@Wire
	private Textbox globalCancelComment;

	@WireVariable
	private transient EnumerationService enumerationService;
	@WireVariable
	private transient BackofficeLocaleService cockpitLocaleService;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;
	@WireVariable
	private transient ReturnCallbackService returnCallbackService;
	@WireVariable
	private transient NotificationService notificationService;

	/**
	 * Initialize the popup to choose cancellation reason
	 *
	 * @param inputObject
	 *           the return request for which we want set the cancellation reason
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initCancelReturnForm(final ReturnRequestModel inputObject)
	{
		cancellationReasons.clear();
		getReturnRequestCode().setValue(inputObject.getRMA());
		getCustomerName().setValue(inputObject.getOrder().getUser().getName());

		setReturnRequest(inputObject);

		getWidgetInstanceManager().setTitle(
				getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelreturnpopup.title") + " "
						+ getReturnRequest().getRMA());

		// Populate the list of Cancellation reasons
		final Locale locale = getCockpitLocaleService().getCurrentLocale();
		getEnumerationService().getEnumerationValues(CancelReason.class).forEach(
				reason -> cancellationReasons.add(getEnumerationService().getEnumerationName(reason, locale)));
		globalCancelReasons.setModel(new ListModelArray(cancellationReasons));

		globalCancelReasons.addEventListener("onCustomChange",
				event -> Events.echoEvent("onLaterCustomChange", globalCancelReasons, event.getData()));
		globalCancelReasons.addEventListener("onLaterCustomChange", event -> {
			Clients.clearWrongValue(globalCancelReasons);
			globalCancelReasons.invalidate();
			handleGlobalCancelReason(event);
		});
	}

	/**
	 * Applies a global reason to all individual entries after an event is triggered
	 *
	 * @param event
	 *           the on select event that was triggered by the user
	 */
	protected void handleGlobalCancelReason(final Event event)
	{
		getSelectedCancelReason(event);
		// TODO: assign the cancel reason to a ReturnEntryToCancelDto once OMSE-1545 is resolved.
	}

	/**
	 * Retrieve the {@link de.hybris.platform.basecommerce.enums.CancelReason} according to the selected label in the
	 * combobox
	 *
	 * @param cancelReasonLabel
	 *           a {@link String} representation of the reason
	 * @return the CancelReason corresponding to the label
	 */
	protected Optional<CancelReason> matchingComboboxCancelReason(final String cancelReasonLabel)
	{
		return getEnumerationService()
				.getEnumerationValues(CancelReason.class)
				.stream()
				.filter(
						reason -> getEnumerationService().getEnumerationName(reason, getCockpitLocaleService().getCurrentLocale())
								.equals(cancelReasonLabel)).findFirst();
	}

	/**
	 * Gets the label for the selected cancel reason in case the event has a target of Combobox
	 *
	 * @param event
	 *           the event that was fired
	 * @return the CancelReason corresponding to the label.
	 */
	protected Optional<CancelReason> getSelectedCancelReason(final Event event)
	{
		Optional<CancelReason> reason = Optional.empty();
		if (event.getTarget() instanceof Combobox)
		{
			final Object selectedValue = event.getData();
			reason = matchingComboboxCancelReason(selectedValue.toString());
		}
		return reason;
	}

	/**
	 * Reinitialize the popup.
	 */
	@ViewEvent(componentID = "undocancelreturn", eventName = Events.ON_CLICK)
	public void undoCancelReturn()
	{
		globalCancelReasons.setSelectedItem(null);
		globalCancelComment.setValue("");
		initCancelReturnForm(getReturnRequest());

	}

	/**
	 * Confirm the return approval request
	 */
	@ViewEvent(componentID = "confirmcancelreturn", eventName = Events.ON_CLICK)
	public void confirmCancelReturn()
	{
		validateRequest();

		Messagebox.show(getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelreturnpopup.confirm.message.question"),
				getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelreturnpopup.title") + " " + getReturnRequest()
						.getRMA(), new Messagebox.Button[] { Messagebox.Button.NO, Messagebox.Button.YES }, Messagebox.QUESTION,
				this::processCancellation);
	}

	/**
	 * Process the cancellation only if the user confirmed it
	 *
	 * @param event
	 *           the confirmation event coming from the user
	 */
	protected void processCancellation(final Event event)
	{
		if (Messagebox.Button.YES.event.equals(event.getName()))
		{
			final ReturnActionResponse returnActionResponse = new ReturnActionResponse(getReturnRequest());
			try
			{
				getReturnCallbackService().onReturnCancelResponse(returnActionResponse);
				getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.SUCCESS, getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelreturnpopup.success.message"));
			}
			catch (final OrderReturnException e)
			{
				LOGGER.error(e.getMessage(), e);
				getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE, getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelreturnpopup.error.message"));
			}

			// This refresh doesn't work and is useless
			getCockpitEventQueue()
					.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, getReturnRequest(), null));

			getWidgetInstanceManager().sendOutput(OUT_CONFIRM, COMPLETED);
		}
	}

	/**
	 * Check if the data provided by the form are compliant with the validation rules
	 */
	protected void validateRequest()
	{
		if (globalCancelReasons.getSelectedItem() == null)
		{
			throw new WrongValueException(globalCancelReasons,
					getLabel("customersupportbackoffice.cancelreturnpopup.decline.validation.missing.reason"));
		}
	}

	protected ReturnRequestModel getReturnRequest()
	{
		return returnRequest;
	}

	public void setReturnRequest(final ReturnRequestModel returnRequest)
	{
		this.returnRequest = returnRequest;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	protected BackofficeLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	protected ReturnCallbackService getReturnCallbackService()
	{
		return returnCallbackService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public Textbox getCustomerName()
	{
		return customerName;
	}

	public Textbox getReturnRequestCode()
	{
		return returnRequestCode;
	}
}
