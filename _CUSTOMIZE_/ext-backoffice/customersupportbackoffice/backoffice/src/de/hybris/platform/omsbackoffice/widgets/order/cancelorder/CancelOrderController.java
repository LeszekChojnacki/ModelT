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
package de.hybris.platform.omsbackoffice.widgets.order.cancelorder;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.omsbackoffice.dto.OrderEntryToCancelDto;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import com.hybris.backoffice.i18n.BackofficeLocaleService;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.DefaultWidgetController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;


/**
 * This controller creates a pop-up to cancel an order or its entries.
 */
public class CancelOrderController extends DefaultWidgetController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderController.class);
	private static final long serialVersionUID = 1L;

	protected static final String IN_SOCKET = "inputObject";
	protected static final String CONFIRM_ID = "confirmcancellation";
	protected static final Object COMPLETED = "completed";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_PICKUP = "customersupportbackoffice.cancelorder.pickup";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_TITLE = "customersupportbackoffice.cancelorder.confirm.title";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_ERROR = "customersupportbackoffice.cancelorder.confirm.error";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_MSG = "customersupportbackoffice.cancelorder.confirm.msg";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_ERROR_QTYCANCELLED_INVALID = "customersupportbackoffice.cancelorder.error.qtycancelled.invalid";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_MISSING_QUANTITY = "customersupportbackoffice.cancelorder.missing.quantity";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_ERROR_REASON = "customersupportbackoffice.cancelorder.error.reason";
	protected static final String CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_MISSING_SELECTED_LINE = "customersupportbackoffice.cancelorder.missing.selectedLine";
	protected static final String CANCELORDER_CONFIRM_ICON = "oms-widget-cancelorder-confirm-icon";
	protected static final String CAPTURE_PAYMENT_ON_CONSIGNMENT = "warehousing.capturepaymentonconsignment";
	protected static final int COLUMN_INDEX_PENDING_QUANTITY = 4;
	protected static final int COLUMN_INDEX_CANCEL_QUANTITY = 5;
	protected static final int COLUMN_INDEX_CANCEL_REASON = 6;
	protected static final int COLUMN_INDEX_CANCEL_COMMENT = 7;

	private transient Map<AbstractOrderEntryModel, Long> orderCancellableEntries;
	private transient Set<OrderEntryToCancelDto> orderEntriesToCancel;

	private OrderModel orderModel;
	private final List<String> cancelReasons = new ArrayList<>();

	@Wire
	private Textbox orderNumber;
	@Wire
	private Textbox customerName;
	@Wire
	private Combobox globalCancelReasons;
	@Wire
	private Textbox globalCancelComment;
	@Wire
	private Grid orderEntries;
	@Wire
	private Checkbox globalCancelEntriesSelection;

	@WireVariable
	private transient BackofficeLocaleService cockpitLocaleService;
	@WireVariable
	private transient OrderCancelService orderCancelService;
	@WireVariable
	private transient EnumerationService enumerationService;
	@WireVariable
	private transient ModelService modelService;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;
	@WireVariable
	private transient UserService userService;
	@WireVariable
	private transient List<ConsignmentStatus> notCancellableConsignmentStatus;
	@WireVariable
	private transient NotificationService notificationService;
	@WireVariable
	private transient ConfigurationService configurationService;

	// public methods

	/**
	 * Confirm the cancel order request
	 */
	@ViewEvent(componentID = CONFIRM_ID, eventName = Events.ON_CLICK)
	public void confirmCancellation()
	{
		validateRequest();
		showMessageBox();
	}

	/**
	 * Initialize the popup to cancel orders
	 *
	 * @param inputObject
	 * 		the order for which we want to cancel several entries
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initCancellationOrderForm(final OrderModel inputObject)
	{
		cancelReasons.clear();

		globalCancelEntriesSelection.setChecked(false);

		setOrderModel(inputObject);

		getWidgetInstanceManager().setTitle(
				getWidgetInstanceManager().getLabel("customersupportbackoffice.cancelorder.confirm.title") + " " + getOrderModel()
						.getCode());
		orderNumber.setValue(getOrderModel().getCode());
		customerName.setValue(getOrderModel().getUser().getDisplayName());

		// Populate the list of cancel reasons
		final Locale locale = getLocale();
		getEnumerationService().getEnumerationValues(CancelReason.class)
				.forEach(reason -> cancelReasons.add(getEnumerationService().getEnumerationName(reason, locale)));
		globalCancelReasons.setModel(new ListModelArray<>(cancelReasons));

		orderEntriesToCancel = new HashSet<>();
		orderCancellableEntries = getOrderCancelService()
				.getAllCancelableEntries(getOrderModel(), getUserService().getCurrentUser());
		if (!orderCancellableEntries.isEmpty())
		{
			//if capture-on-consignment property is set, change the cancellableQty to reflect only quantities in uncaptured consignments
			if (getConfigurationService().getConfiguration().getBoolean(CAPTURE_PAYMENT_ON_CONSIGNMENT, Boolean.FALSE))
			{

				final Map<Integer, Long> consignmentSums = getOrderModel().getConsignments().stream()
						.filter(c -> !getNotCancellableConsignmentStatus().contains(c.getStatus()))
						.map(ConsignmentModel::getConsignmentEntries).flatMap(Collection::stream).collect(
								Collectors.toMap(o -> o.getOrderEntry().getEntryNumber(), ConsignmentEntryModel::getQuantity, Long::sum));

				orderCancellableEntries.forEach((entry, cancellableQty) -> {
					if (consignmentSums.get(entry.getEntryNumber()) != null)
					{
						orderEntriesToCancel
								.add(new OrderEntryToCancelDto(entry, cancelReasons, consignmentSums.get(entry.getEntryNumber()),
										determineDeliveryMode(entry)));
					}
				});
			}
			else
			{
				orderCancellableEntries.forEach((entry, cancellableQty) -> orderEntriesToCancel
						.add(new OrderEntryToCancelDto(entry, cancelReasons, cancellableQty, determineDeliveryMode(entry))));
			}
		}

		getOrderEntries().setModel(new ListModelList<>(orderEntriesToCancel));
		getOrderEntries().renderAll();
		addListeners();
	}

	/**
	 * determines the delivery mode for a given orderEntry. A pick up Entry is an entry that have a
	 * DeliveryPointOfService, other wise we take the value from Entry DeliveryMode if not null, other wise we take the
	 * value from DeliveryMode on order level.
	 *
	 * @param orderEntry
	 * 		the {@link AbstractOrderEntryModel} for which to determine the delivery mode
	 * @return the delivery mode name for a given orderEntry
	 */
	protected String determineDeliveryMode(final AbstractOrderEntryModel orderEntry)
	{
		String deliveryModeResult;
		if (orderEntry.getDeliveryMode() != null)
		{
			deliveryModeResult = orderEntry.getDeliveryMode().getName();
		}
		else if (orderEntry.getDeliveryPointOfService() != null)
		{
			deliveryModeResult = getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_PICKUP);
		}
		else
		{
			//return empty string in case of DeliveryMode is not set, and use the Code in case Name is not available
			deliveryModeResult = orderEntry.getOrder().getDeliveryMode() != null ?
					orderEntry.getOrder().getDeliveryMode().getName() != null ?
							orderEntry.getOrder().getDeliveryMode().getName() :
							orderEntry.getOrder().getDeliveryMode().getCode() :
					null;
		}
		return deliveryModeResult;
	}

	/**
	 * Reinitialize the popup
	 */
	@ViewEvent(componentID = "undocancellation", eventName = Events.ON_CLICK)
	public void reset()
	{
		globalCancelReasons.setSelectedItem(null);
		globalCancelComment.setValue("");

		initCancellationOrderForm(getOrderModel());
	}

	// protected methods

	/**
	 * Add listeners on Intbox, Combobox and Textbox so that when a change happens on one of these components, we are
	 * catching it.
	 */
	protected void addListeners()
	{
		final List<Component> rows = getOrderEntries().getRows().getChildren();
		for (final Component row : rows)
		{
			for (final Component myComponent : row.getChildren())
			{
				if (myComponent instanceof Checkbox)
				{
					myComponent.addEventListener(Events.ON_CHECK, event -> handleRow((Row) event.getTarget().getParent()));
				}
				else if (myComponent instanceof Combobox)
				{
					myComponent.addEventListener("onCustomChange",
							event -> Events.echoEvent("onLaterCustomChange", myComponent, event.getData()));
					myComponent.addEventListener("onLaterCustomChange", event -> {
						Clients.clearWrongValue(myComponent);
						myComponent.invalidate();
						handleIndividualCancelReason(event);
					});
				}
				else if (myComponent instanceof Intbox)
				{
					myComponent.addEventListener(Events.ON_CHANGE, event -> {
						autoSelect(event);
						((OrderEntryToCancelDto) ((Row) event.getTarget().getParent()).getValue())
								.setQuantityToCancel(Long.valueOf(((InputEvent) event).getValue()));
					});
				}
				else if (myComponent instanceof Textbox)
				{
					myComponent.addEventListener(Events.ON_CHANGING, event -> {
						autoSelect(event);
						((OrderEntryToCancelDto) ((Row) event.getTarget().getParent()).getValue())
								.setCancelOrderEntryComment(((InputEvent) event).getValue());
					});
				}
			}
		}

		globalCancelReasons.addEventListener(Events.ON_SELECT, this::handleGlobalCancelReason);
		globalCancelComment.addEventListener(Events.ON_CHANGING, this::handleGlobalCancelComment);
		globalCancelEntriesSelection.addEventListener(Events.ON_CHECK, event -> selectAllEntries());
	}

	/**
	 * Apply a value to the elements contained in the grid
	 *
	 * @param data
	 * 		the value to pass to the grid
	 * @param childrenIndex
	 * 		the index indicating where to find the right component in the grid
	 */
	protected void applyToGrid(final Object data, final int childrenIndex)
	{
		getOrderEntriesGridRows().stream().filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked())
				.forEach(entry -> applyToRow(data, childrenIndex, entry));
	}

	/**
	 * Apply a specific change to a specific row of the grid
	 *
	 * @param data
	 * 		the new value to set
	 * @param childrenIndex
	 * 		the index of the component in the grid
	 * @param row
	 * 		the row to which the change needs to be applied
	 */
	protected void applyToRow(final Object data, final int childrenIndex, final Component row)
	{
		int index = 0;
		for (final Component myComponent : row.getChildren())
		{
			if (index == childrenIndex)
			{
				if (myComponent instanceof Checkbox && data != null)
				{
					((Checkbox) myComponent).setChecked((Boolean) data);
				}
				if (myComponent instanceof Combobox)
				{
					if (data == null)
					{
						((Combobox) myComponent).setSelectedItem(null);
					}
					else
					{
						((Combobox) myComponent).setSelectedIndex((Integer) data);
					}
				}
				else if (myComponent instanceof Intbox)
				{
					((Intbox) myComponent).setValue((Integer) data);
				}
				else if (myComponent instanceof Textbox)
				{
					((Textbox) myComponent).setValue((String) data);
				}
			}
			index++;
		}
	}

	/**
	 * Automatically select the checkbox in front of the row
	 *
	 * @param event
	 * 		the event that triggered the autoselect
	 */
	protected void autoSelect(final Event event)
	{
		((Checkbox) (event.getTarget().getParent()).getChildren().iterator().next()).setChecked(true);
	}

	/**
	 * Build the context parameter to cancel an order and return the given process
	 *
	 * @return a cancel request with the selected fields
	 */
	protected OrderCancelRequest buildCancelRequest()
	{
		if (getOrderModel() != null)
		{
			final List<OrderCancelEntry> orderCancelEntries = new ArrayList<>();
			getOrderEntriesGridRows().stream().filter(entry -> ((Checkbox) (entry.getFirstChild())).isChecked())
					.forEach(entry -> createOrderCancelEntry(orderCancelEntries, ((Row) entry).getValue()));


			final OrderCancelRequest orderCancelRequest = new OrderCancelRequest(getOrderModel(), orderCancelEntries);
			orderCancelRequest.setCancelReason(matchingComboboxCancelReason(globalCancelReasons.getValue()).orElse(null));
			orderCancelRequest.setNotes(globalCancelComment.getValue());
			return orderCancelRequest;
		}

		return null;
	}

	/**
	 * Creates an {@link OrderCancelEntry}
	 *
	 * @param orderCancelEntries
	 * 		the list which contains all {@link OrderCancelEntry}
	 * @param entry
	 * 		the entry to be converted into a {@link OrderCancelEntry}
	 */
	protected void createOrderCancelEntry(final List<OrderCancelEntry> orderCancelEntries, final Object entry)
	{
		final OrderEntryToCancelDto orderEntryToCancel = (OrderEntryToCancelDto) entry;
		final OrderCancelEntry orderCancelEntry = new OrderCancelEntry(orderEntryToCancel.getOrderEntry(),
				orderEntryToCancel.getQuantityToCancel().longValue(), orderEntryToCancel.getCancelOrderEntryComment(),
				orderEntryToCancel.getSelectedReason());
		orderCancelEntries.add(orderCancelEntry);
	}

	/**
	 * Gets the reason index within the combobox model
	 *
	 * @param cancelReason
	 * 		the reason for which we want to know the index
	 * @return the index of the given cancel reason
	 */
	protected int getReasonIndex(final CancelReason cancelReason)
	{
		int index = 0;
		final String myReason = getEnumerationService()
				.getEnumerationName(cancelReason, getCockpitLocaleService().getCurrentLocale());
		for (final String reason : cancelReasons)
		{
			if (myReason.equals(reason))
			{
				break;
			}
			index++;
		}
		return index;
	}

	/**
	 * get the corresponding cancel reason according to the selected item in the combobox if existing
	 *
	 * @param event
	 * 		the select event on the combobox
	 * @return the corresponding cancel reason
	 */
	protected Optional<CancelReason> getSelectedCancelReason(final Event event)
	{
		Optional<CancelReason> result = Optional.empty();
		if (!((SelectEvent) event).getSelectedItems().isEmpty())
		{
			final Object selectedValue = ((Comboitem) ((SelectEvent) event).getSelectedItems().iterator().next()).getValue();
			result = matchingComboboxCancelReason(selectedValue.toString());
		}
		return result;
	}

	/**
	 * Applies a global comment to all individual entries after an event is triggered
	 *
	 * @param event
	 * 		the on changing event that was triggered by the user
	 */
	protected void handleGlobalCancelComment(final Event event)
	{
		applyToGrid(((InputEvent) event).getValue(), COLUMN_INDEX_CANCEL_COMMENT);

		getOrderEntriesGridRows().stream().filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked())
				.forEach(entry -> {
					final OrderEntryToCancelDto myEntry = ((Row) entry).getValue();
					myEntry.setCancelOrderEntryComment(((InputEvent) event).getValue());
				});
	}

	/**
	 * Applies a global reason to all individual entries after an event is triggered
	 *
	 * @param event
	 * 		the on select event that was triggered by the user
	 */
	protected void handleGlobalCancelReason(final Event event)
	{
		final Optional<CancelReason> cancelReason = getSelectedCancelReason(event);
		if (cancelReason.isPresent())
		{
			applyToGrid(Integer.valueOf(getReasonIndex(cancelReason.get())), COLUMN_INDEX_CANCEL_REASON);

			getOrderEntriesGridRows().stream().filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked())
					.forEach(entry -> {
						final OrderEntryToCancelDto myEntry = ((Row) entry).getValue();
						myEntry.setSelectedReason(cancelReason.get());
					});
		}
	}

	/**
	 * Applies a cancel reason to an individual entry after an event is triggered
	 *
	 * @param event
	 * 		the on select event that was triggered by the user
	 */
	protected void handleIndividualCancelReason(final Event event)
	{
		final Optional<CancelReason> cancelReason = getCustomSelectedCancelReason(event);
		if (cancelReason.isPresent())
		{
			autoSelect(event);
			((OrderEntryToCancelDto) ((Row) event.getTarget().getParent()).getValue()).setSelectedReason(cancelReason.get());
		}
	}

	/**
	 * Set or Reset the given row with the appropriate default information
	 *
	 * @param row
	 * 		the row to set/reset
	 */
	protected void handleRow(final Row row)
	{
		final OrderEntryToCancelDto myEntry = row.getValue();

		if (!((Checkbox) (row.getChildren().iterator().next())).isChecked())
		{
			applyToRow(Integer.valueOf(0), COLUMN_INDEX_CANCEL_QUANTITY, row);
			applyToRow(null, COLUMN_INDEX_CANCEL_REASON, row);
			applyToRow(null, COLUMN_INDEX_CANCEL_COMMENT, row);

			myEntry.setQuantityToCancel(Long.valueOf(0L));
			myEntry.setSelectedReason(null);
			myEntry.setCancelOrderEntryComment(null);
		}
		else
		{
			applyToRow(Integer.valueOf(globalCancelReasons.getSelectedIndex()), COLUMN_INDEX_CANCEL_REASON, row);
			applyToRow(globalCancelComment.getValue(), COLUMN_INDEX_CANCEL_COMMENT, row);

			final Optional<CancelReason> reason = matchingComboboxCancelReason(
					(globalCancelReasons.getSelectedItem() != null) ? globalCancelReasons.getSelectedItem().getLabel() : null);
			myEntry.setSelectedReason(reason.orElse(null));
			myEntry.setCancelOrderEntryComment(globalCancelComment.getValue());
		}
	}

	/**
	 * Gets the label for the selected cancel reason in case the event has a target of Combobox
	 *
	 * @param event
	 * 		the event that was fired
	 * @return the CancelReason corresponding to the label.
	 */
	protected Optional<CancelReason> getCustomSelectedCancelReason(final Event event)
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
	 * Retrieve the {@link de.hybris.platform.basecommerce.enums.CancelReason} according to the selected label in the
	 * combobox
	 *
	 * @param cancelReasonLabel
	 * 		a {@link String} representation of the reason
	 * @return the CancelReason corresponding to the label
	 */
	protected Optional<CancelReason> matchingComboboxCancelReason(final String cancelReasonLabel)
	{
		return getEnumerationService().getEnumerationValues(CancelReason.class).stream()
				.filter(reason -> getEnumerationService().getEnumerationName(reason, getLocale()).equals(cancelReasonLabel))
				.findFirst();
	}

	/**
	 * Process the cancellation Check if the user confirm the cancellation, then it sends the information to the service.
	 *
	 * @param obj
	 * 		the {@link Event}
	 */
	protected void processCancellation(final Event obj)
	{
		if (Messagebox.Button.YES.event.equals(obj.getName()))
		{
			try
			{
				final OrderCancelRecordEntryModel orderCancelRecordEntry = getOrderCancelService()
						.requestOrderCancel(buildCancelRequest(), getUserService().getCurrentUser());

				switch (orderCancelRecordEntry.getCancelResult())
				{
					case FULL:
					case PARTIAL:
					{
						//more here for messaging to user
						getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
								NotificationEvent.Level.SUCCESS, getLabel("customersupportbackoffice.cancelorder.confirm.success"));
						break;
					}
					case DENIED:
					{
						//more here for messaging to user
						getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
								NotificationEvent.Level.FAILURE, getLabel("customersupportbackoffice.cancelorder.confirm.error"));
						break;
					}
					default:
						break;
				}
			}
			catch (final CancellationException | OrderCancelException e)
			{
				LOGGER.info(e.getMessage(), e);
				getNotificationService().notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE,
						NotificationEvent.Level.FAILURE, getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_ERROR));
			}

			//notify cockpit to refresh the order entry
			final OrderModel object = getModelService().get(getOrderModel().getPk());

			object.getEntries().forEach(entry -> getCockpitEventQueue()
					.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, entry, null)));

			sendOutput(CONFIRM_ID, COMPLETED);
		}
	}

	/**
	 * Select or unselect all the rows of the grid and set the default values for each of them
	 */
	protected void selectAllEntries()
	{
		applyToGrid(Boolean.TRUE, 0);

		for (final Component row : getOrderEntriesGridRows())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox)
			{
				((Checkbox) firstComponent).setChecked(globalCancelEntriesSelection.isChecked());
			}
			handleRow((Row) row);
			if (globalCancelEntriesSelection.isChecked())
			{
				final int cancellableQuantity = Integer
						.parseInt(((Label) row.getChildren().get(COLUMN_INDEX_PENDING_QUANTITY)).getValue());
				applyToRow(Integer.valueOf(cancellableQuantity), COLUMN_INDEX_CANCEL_QUANTITY, row);
			}
		}

		if (globalCancelEntriesSelection.isChecked())
		{
			orderEntriesToCancel.forEach(entry -> entry.setQuantityToCancel(orderCancellableEntries.get(entry.getOrderEntry())));
		}
	}

	/**
	 * Displays a message box whether the user wants to cancel the order
	 */
	protected void showMessageBox()
	{
		Messagebox.show(getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_MSG),
				getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_CONFIRM_TITLE) + " " + getOrderModel().getCode(),
				new Messagebox.Button[] { Messagebox.Button.NO, Messagebox.Button.YES }, CANCELORDER_CONFIRM_ICON, obj -> {
					getOrderModel().setStatus(OrderStatus.CANCELLING);
					getModelService().save(getOrderModel());
					processCancellation(obj);
				});
	}

	/**
	 * Retrieve the row who needs to apply a validation message
	 *
	 * @param stringToValidate
	 * 		the string to compare to identify a specific row in the grid
	 * @param indexLabelToCheck
	 * 		the index of the label to which we want to compare the passed string
	 * @param indexTargetComponent
	 * 		the index of the grid where the target component is located
	 */
	protected Component targetFieldToApplyValidation(final String stringToValidate, final int indexLabelToCheck,
			final int indexTargetComponent)
	{
		for (final Component component : getOrderEntriesGridRows())
		{
			final Label label = (Label) component.getChildren().get(indexLabelToCheck);
			if (label.getValue().equals(stringToValidate))
			{
				return component.getChildren().get(indexTargetComponent);
			}
		}
		return null;
	}

	/**
	 * Validate each order entry and throw a {@link WrongValueException} if it fails any check
	 *
	 * @param entry
	 * 		the individual entry to validate
	 */
	protected void validateOrderEntry(final OrderEntryToCancelDto entry)
	{
		if (entry.getQuantityToCancel().longValue() > orderCancellableEntries.get(entry.getOrderEntry()).longValue())
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(entry.getOrderEntry().getProduct().getCode(),
					1, COLUMN_INDEX_CANCEL_QUANTITY);
			throw new WrongValueException(quantity, getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_ERROR_QTYCANCELLED_INVALID));
		}
		else if (entry.getSelectedReason() != null && entry.getQuantityToCancel().longValue() == 0)
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(entry.getOrderEntry().getProduct().getCode(),
					1, COLUMN_INDEX_CANCEL_QUANTITY);
			throw new WrongValueException(quantity, getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_MISSING_QUANTITY));
		}
		else if (entry.getSelectedReason() == null && entry.getQuantityToCancel().longValue() > 0)
		{
			final Combobox reason = (Combobox) targetFieldToApplyValidation(entry.getOrderEntry().getProduct().getCode(), 1,
					COLUMN_INDEX_CANCEL_REASON);
			throw new WrongValueException(reason, getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_ERROR_REASON));
		}
	}

	/**
	 * Check if the data provided by the form are compliant with the validation rules
	 */
	protected void validateRequest()
	{
		for (final Component row : getOrderEntriesGridRows())
		{
			if (((Checkbox) (row.getChildren().iterator().next())).isChecked())
			{
				final InputElement cancelQty = (InputElement) row.getChildren().get(COLUMN_INDEX_CANCEL_QUANTITY);
				if (cancelQty.getRawValue().equals(Integer.valueOf(0)))
				{
					throw new WrongValueException(cancelQty, getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_MISSING_QUANTITY));
				}
			}
		}

		final ListModelList<OrderEntryToCancelDto> modelList = (ListModelList) getOrderEntries().getModel();

		if (modelList.stream().allMatch(entry -> entry.getQuantityToCancel().longValue() == 0))
		{
			throw new WrongValueException(globalCancelEntriesSelection,
					getLabel(CUSTOMERSUPPORTBACKOFFICE_CANCELORDER_MISSING_SELECTED_LINE));
		}
		else
		{
			modelList.forEach(this::validateOrderEntry);
		}
	}

	protected List<Component> getOrderEntriesGridRows()
	{
		return getOrderEntries().getRows().getChildren();
	}

	protected Locale getLocale()
	{
		return getCockpitLocaleService().getCurrentLocale();
	}

	protected BackofficeLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	protected Grid getOrderEntries()
	{
		return orderEntries;
	}

	protected OrderModel getOrderModel()
	{
		return orderModel;
	}

	public void setOrderModel(final OrderModel orderModel)
	{
		this.orderModel = orderModel;
	}

	protected OrderCancelService getOrderCancelService()
	{
		return orderCancelService;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected List<ConsignmentStatus> getNotCancellableConsignmentStatus()
	{
		return notCancellableConsignmentStatus;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
