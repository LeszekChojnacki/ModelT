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
package de.hybris.platform.omsbackoffice.widgets.returns.createreturnrequest;

import de.hybris.platform.basecommerce.enums.RefundReason;
import de.hybris.platform.basecommerce.enums.ReturnAction;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.commerceservices.event.CreateReturnEvent;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.omsbackoffice.widgets.returns.dtos.ReturnEntryToCreateDto;
import de.hybris.platform.refund.RefundService;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.hybris.backoffice.i18n.BackofficeLocaleService;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.util.DefaultWidgetController;
import org.apache.log4j.Logger;
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
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;


/**
 * This controller creates a pop-up to create a {@link ReturnRequestModel}. It offers to select manually the reason of
 * the return as well as the custom amount to refund for each item. If not specified, the amount to refund will be
 * automatically set to the base price of the product. It also offer to set an optional comment.
 */
public class CreateReturnRequestController extends DefaultWidgetController
{
	private static final Logger LOG = Logger.getLogger(CreateReturnRequestController.class.getName());
	private static final long serialVersionUID = 1L;

	protected static final String IN_SOCKET = "inputObject";
	protected static final String OUT_CONFIRM = "confirm";
	protected static final Object COMPLETED = "completed";
	protected static final int COLUMN_INDEX_RETURNABLE_QUANTITY = 5;
	protected static final int COLUMN_INDEX_RETURN_QUANTITY = 6;
	protected static final int COLUMN_INDEX_RETURN_AMOUNT = 7;
	protected static final int COLUMN_INDEX_RETURN_REASON = 8;
	protected static final int COLUMN_INDEX_RETURN_COMMENT = 9;

	private final List<String> refundReasons = new ArrayList<>();
	private transient Set<ReturnEntryToCreateDto> returnEntriesToCreate;
	private OrderModel order;

	@Wire
	private Textbox orderCode;
	@Wire
	private Textbox customer;
	@Wire
	private Doublebox totalDiscounts;
	@Wire
	private Doublebox orderTotal;
	@Wire
	private Combobox globalReason;
	@Wire
	private Textbox globalComment;
	@Wire
	private Grid returnEntries;
	@Wire
	private Checkbox isReturnInstore;
	@Wire
	private Checkbox refundDeliveryCost;
	@Wire
	private Checkbox globalReturnEntriesSelection;
	@Wire
	private Doublebox totalRefundAmount;
	@Wire
	private Doublebox estimatedTax;
	@Wire
	private Doublebox deliveryCost;

	@WireVariable
	private transient ReturnService returnService;
	@WireVariable
	private transient RefundService refundService;
	@WireVariable
	private transient EventService eventService;
	@WireVariable
	private transient EnumerationService enumerationService;
	@WireVariable
	private transient ModelService modelService;
	@WireVariable
	private transient BackofficeLocaleService cockpitLocaleService;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;
	@WireVariable
	private transient NotificationService notificationService;

	/**
	 * Initialize the popup to create a {@link ReturnRequestModel}
	 *
	 * @param inputOrder
	 * 		the order for which we want to create the return request
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initCreateReturnRequestForm(final OrderModel inputOrder)
	{
		setOrder(inputOrder);
		refundReasons.clear();

		isReturnInstore.setChecked(false);
		refundDeliveryCost.setChecked(false);
		globalReturnEntriesSelection.setChecked(false);
		deliveryCost.setValue(getOrder().getDeliveryCost());

		refundDeliveryCost.setDisabled(getReturnService().getReturnRequests(getOrder().getCode()).stream().anyMatch(
				returnRequest -> returnRequest.getRefundDeliveryCost() && returnRequest.getStatus() != ReturnStatus.CANCELED));

		getWidgetInstanceManager().setTitle(
				getWidgetInstanceManager().getLabel("customersupportbackoffice.createreturnrequest.confirm.title") + " " + getOrder()
						.getCode());

		orderCode.setValue(getOrder().getCode());
		customer.setValue(getOrder().getUser().getDisplayName());
		orderTotal.setValue(getOrder().getTotalPrice());

		setTotalDiscounts();

		// Populate the list of refund reasons
		final Locale locale = getCockpitLocaleService().getCurrentLocale();
		getEnumerationService().getEnumerationValues(RefundReason.class)
				.forEach(reason -> refundReasons.add(getEnumerationService().getEnumerationName(reason, locale)));

		globalReason.setModel(new ListModelArray(refundReasons));
		final Map<AbstractOrderEntryModel, Long> returnableOrderEntries = getReturnService().getAllReturnableEntries(inputOrder);

		returnEntriesToCreate = new HashSet<>();
		returnableOrderEntries.forEach((orderEntry, returnableQty) -> returnEntriesToCreate
				.add(new ReturnEntryToCreateDto(orderEntry, returnableQty.intValue(), refundReasons)));

		getReturnEntries().setModel(new ListModelList<>(returnEntriesToCreate));
		getReturnEntries().renderAll();
		addListeners();
	}

	/**
	 * Add listeners on Intbox, Combobox and Textbox so that when a change happens on one of these components, we are
	 * catching it.
	 */
	protected void addListeners()
	{
		final List<Component> rows = returnEntries.getRows().getChildren();
		for (final Component row : rows)
		{
			for (final Component myComponent : row.getChildren())
			{
				if (myComponent instanceof Combobox)
				{
					myComponent.addEventListener("onCustomChange",
							event -> Events.echoEvent("onLaterCustomChange", myComponent, event.getData()));
					myComponent.addEventListener("onLaterCustomChange", event -> {
						Clients.clearWrongValue(myComponent);
						myComponent.invalidate();
						handleIndividualRefundReason(event);
					});
				}
				else if (myComponent instanceof Checkbox)
				{
					myComponent.addEventListener(Events.ON_CHECK, event -> {
						handleRow((Row) event.getTarget().getParent());
						calculateTotalRefundAmount();
					});
				}
				else if (myComponent instanceof Intbox)
				{
					myComponent.addEventListener(Events.ON_CHANGING, this::handleIndividualQuantityToReturn);
				}
				else if (myComponent instanceof Doublebox)
				{
					myComponent.addEventListener(Events.ON_CHANGING, this::handleIndividualAmountToReturn);
				}
				else if (myComponent instanceof Textbox)
				{
					myComponent.addEventListener(Events.ON_CHANGING, event -> {
						autoSelect(event);
						((ReturnEntryToCreateDto) ((Row) event.getTarget().getParent()).getValue())
								.setRefundEntryComment(((InputEvent) event).getValue());
					});
				}
			}
		}

		globalReason.addEventListener(Events.ON_SELECT, this::handleGlobalReason);

		globalComment.addEventListener(Events.ON_CHANGING, this::handleGlobalComment);

		globalReturnEntriesSelection.addEventListener(Events.ON_CHECK, event -> selectAllEntries());

		refundDeliveryCost.addEventListener(Events.ON_CLICK, event -> calculateTotalRefundAmount());
	}

	/**
	 * Calculates the row amount for a return for the selected entry after an event was triggered
	 *
	 * @param event
	 * 		the on change event that was triggered by the user
	 */
	protected void handleIndividualAmountToReturn(final Event event)
	{
		((Checkbox) (event.getTarget().getParent()).getChildren().iterator().next()).setChecked(true);
		final Row myRow = (Row) event.getTarget().getParent();
		final ReturnEntryToCreateDto returnEntryDto = myRow.getValue();
		final String refundAmountStr = ((InputEvent) event).getValue();
		final BigDecimal newAmount = refundAmountStr != null && !refundAmountStr.isEmpty() ?
				BigDecimal.valueOf(Double.parseDouble(refundAmountStr)) :
				BigDecimal.ZERO;
		returnEntryDto.getRefundEntry().setAmount(newAmount);
		applyToRow(newAmount.setScale(2, RoundingMode.CEILING).doubleValue(), COLUMN_INDEX_RETURN_AMOUNT, myRow);

		calculateIndividualTaxEstimate(returnEntryDto);
		calculateTotalRefundAmount();
	}

	/**
	 * Calculates the individual tax amount for a {@link RefundEntryModel}
	 *
	 * @param returnEntryDto
	 * 		the DTO representing a {@link RefundEntryModel} on the UI
	 */
	protected void calculateIndividualTaxEstimate(final ReturnEntryToCreateDto returnEntryDto)
	{
		if (returnEntryDto.getQuantityToReturn() <= returnEntryDto.getReturnableQuantity())
		{
			final RefundEntryModel refundEntry = returnEntryDto.getRefundEntry();
			final Optional<TaxValue> orderEntryOptional = refundEntry.getOrderEntry().getTaxValues().stream().findFirst();

			BigDecimal orderEntryTax = BigDecimal.ZERO;
			if (orderEntryOptional.isPresent())
			{
				orderEntryTax = BigDecimal.valueOf(orderEntryOptional.get().getValue());
			}

			if (refundEntry.getAmount().compareTo(BigDecimal.valueOf(refundEntry.getOrderEntry().getTotalPrice())) >= 0)
			{
				returnEntryDto.setTax(orderEntryTax);
			}
			else
			{
				final BigDecimal returnEntryTax = orderEntryTax.multiply(refundEntry.getAmount())
						.divide(BigDecimal.valueOf(refundEntry.getOrderEntry().getTotalPrice()), RoundingMode.HALF_UP)
						.setScale(refundEntry.getOrderEntry().getOrder().getCurrency().getDigits(), RoundingMode.HALF_UP);
				returnEntryDto.setTax(returnEntryTax);
			}
		}
	}

	/**
	 * Calculates the row amount for a return for the selected entry after an event was triggered
	 *
	 * @param event
	 * 		the on change event that was triggered by the user
	 */
	protected void handleIndividualQuantityToReturn(final Event event)
	{
		autoSelect(event);

		final Row myRow = (Row) event.getTarget().getParent();
		final ReturnEntryToCreateDto myReturnEntry = myRow.getValue();
		final String returnQuantityStr = ((InputEvent) event).getValue();
		final int amountEntered =
				returnQuantityStr != null && !returnQuantityStr.isEmpty() ? Integer.parseInt(returnQuantityStr) : 0;
		calculateRowAmount(myRow, myReturnEntry, amountEntered);
	}

	/**
	 * Applies a refund reason to an individual entry after an event is triggered.
	 *
	 * @param event
	 * 		the on select event that was triggered by the user
	 */
	protected void handleIndividualRefundReason(final Event event)
	{
		final Optional<RefundReason> refundReason = getCustomSelectedRefundReason(event);
		if (refundReason.isPresent())
		{
			autoSelect(event);
			((ReturnEntryToCreateDto) ((Row) event.getTarget().getParent()).getValue()).getRefundEntry()
					.setReason(refundReason.get());
		}
	}

	/**
	 * Updates all entries with the selected global comment
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleGlobalComment(final Event event)
	{
		applyToGrid(((InputEvent) event).getValue(), COLUMN_INDEX_RETURN_COMMENT);

		returnEntries.getRows().getChildren().stream()
				.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked()).forEach(entry -> {
			final ReturnEntryToCreateDto myEntry = ((Row) entry).getValue();
			myEntry.setRefundEntryComment(((InputEvent) event).getValue());
		});
	}

	/**
	 * Updates all entries with the selected global reason
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleGlobalReason(final Event event)
	{
		final Optional<RefundReason> refundReason = getSelectedRefundReason(event);
		if (refundReason.isPresent())
		{
			applyToGrid(Integer.valueOf(getReasonIndex(refundReason.get())), COLUMN_INDEX_RETURN_REASON);

			returnEntries.getRows().getChildren().stream()
					.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked()).forEach(entry -> {
				final ReturnEntryToCreateDto myEntry = ((Row) entry).getValue();
				myEntry.getRefundEntry().setReason(refundReason.get());
			});
		}
	}

	/**
	 * Calculates the total refund amount for a specific return entry according to the the quantity to return
	 *
	 * @param myRow
	 * 		the row I want the calculation to apply
	 * @param myReturnEntry
	 * 		the targeted return entry
	 * @param qtyEntered
	 * 		the quantity to be returned
	 */
	protected void calculateRowAmount(final Row myRow, final ReturnEntryToCreateDto myReturnEntry, final int qtyEntered)
	{
		final BigDecimal newAmount = myReturnEntry.isDiscountApplied() ?
				BigDecimal.ZERO :
				BigDecimal.valueOf(myReturnEntry.getRefundEntry().getOrderEntry().getBasePrice() * qtyEntered);

		myReturnEntry.setQuantityToReturn(qtyEntered);
		myReturnEntry.getRefundEntry().setAmount(newAmount);
		applyToRow(newAmount.setScale(2, RoundingMode.HALF_EVEN).doubleValue(), COLUMN_INDEX_RETURN_AMOUNT, myRow);

		calculateIndividualTaxEstimate(myReturnEntry);
		calculateTotalRefundAmount();
	}

	/**
	 * Calculate the total refund amount to display it to the end user
	 */
	protected void calculateTotalRefundAmount()
	{
		calculateEstimatedTax();

		Double calculatedRefundAmount = refundDeliveryCost.isChecked() ? getOrder().getDeliveryCost() : 0D;
		calculatedRefundAmount =
				calculatedRefundAmount + returnEntriesToCreate.stream().map(entry -> entry.getRefundEntry().getAmount())
						.reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
		totalRefundAmount.setValue(
				BigDecimal.valueOf(calculatedRefundAmount).add(BigDecimal.valueOf(estimatedTax.doubleValue())).doubleValue());
	}

	/**
	 * Calculate an estimate of the tax for the return to display it to the end user
	 */
	protected void calculateEstimatedTax()
	{
		BigDecimal totalTax = returnEntriesToCreate.stream()
				.filter(returnEntryToCreate -> returnEntryToCreate.getQuantityToReturn() > 0 && returnEntryToCreate.getTax() != null)
				.map(ReturnEntryToCreateDto::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

		if (refundDeliveryCost.isChecked())
		{
			final BigDecimal deliveryCostTax = BigDecimal.valueOf(
					getOrder().getTotalTax() - getOrder().getEntries().stream().filter(entry -> !entry.getTaxValues().isEmpty())
							.mapToDouble(entry -> entry.getTaxValues().stream().findFirst().get().getValue()).sum());
			totalTax = totalTax.add(deliveryCostTax);
		}

		estimatedTax.setValue(totalTax.doubleValue());
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
	 * Set or Reset the given row with the appropriate default informations
	 *
	 * @param row
	 * 		the row to set/reset
	 */
	protected void handleRow(final Row row)
	{
		final ReturnEntryToCreateDto myEntry = row.getValue();

		if (row.getChildren().iterator().next() instanceof Checkbox)
		{
			if (!((Checkbox) row.getChildren().iterator().next()).isChecked())
			{
				applyToRow(Integer.valueOf(0), COLUMN_INDEX_RETURN_QUANTITY, row);
				applyToRow(null, COLUMN_INDEX_RETURN_REASON, row);
				applyToRow(Double.valueOf(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN).doubleValue()),
						COLUMN_INDEX_RETURN_AMOUNT, row);
				applyToRow(null, COLUMN_INDEX_RETURN_COMMENT, row);

				myEntry.setQuantityToReturn(0);
				myEntry.getRefundEntry().setAmount(BigDecimal.ZERO);
				myEntry.getRefundEntry().setReason(null);
				myEntry.setRefundEntryComment(null);
			}
			else
			{
				applyToRow(Integer.valueOf(globalReason.getSelectedIndex()), COLUMN_INDEX_RETURN_REASON, row);
				applyToRow(globalComment.getValue(), COLUMN_INDEX_RETURN_COMMENT, row);

				final Optional<RefundReason> reason = matchingComboboxReturnReason(
						(globalReason.getSelectedItem() != null) ? globalReason.getSelectedItem().getLabel() : null);

				myEntry.getRefundEntry().setReason(reason.isPresent() ? reason.get() : null);
				myEntry.setRefundEntryComment(globalComment.getValue());
			}
		}
		calculateTotalRefundAmount();
	}

	/**
	 * Select or unselect all the rows of the grid and sets the default values for each of them
	 */
	protected void selectAllEntries()
	{
		applyToGrid(Boolean.TRUE, 0);

		for (final Component row : returnEntries.getRows().getChildren())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox)
			{
				((Checkbox) firstComponent).setChecked(globalReturnEntriesSelection.isChecked());
			}
			handleRow((Row) row);
			if (globalReturnEntriesSelection.isChecked())
			{
				final int returnableQty = Integer
						.parseInt(((Label) row.getChildren().get(COLUMN_INDEX_RETURNABLE_QUANTITY)).getValue());
				applyToRow(Integer.valueOf(returnableQty), COLUMN_INDEX_RETURN_QUANTITY, row);
				calculateRowAmount((Row) row, ((Row) row).getValue(), returnableQty);
			}
		}

		if (globalReturnEntriesSelection.isChecked())
		{
			returnEntriesToCreate.forEach(entry -> entry.setQuantityToReturn(entry.getReturnableQuantity()));
			calculateTotalRefundAmount();
		}
	}

	/**
	 * Gets the reason index within the combobox model
	 *
	 * @param refundReason
	 * 		the reason for which we want to know the index
	 * @return the index of the given refund reason
	 */
	protected int getReasonIndex(final RefundReason refundReason)
	{
		int index = 0;
		final String myReason = getEnumerationService()
				.getEnumerationName(refundReason, getCockpitLocaleService().getCurrentLocale());
		for (final String reason : refundReasons)
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
	 * get the corresponding refund reason according to the selected item in the combobox if existing
	 *
	 * @param event
	 * 		the select event on the combobox
	 * @return the corresponding refund reason
	 */
	protected Optional<RefundReason> getSelectedRefundReason(final Event event)
	{
		Optional<RefundReason> result = Optional.empty();
		if (!((SelectEvent) event).getSelectedItems().isEmpty())
		{
			final Object selectedValue = ((Comboitem) ((SelectEvent) event).getSelectedItems().iterator().next()).getValue();
			result = matchingComboboxReturnReason(selectedValue.toString());
		}
		return result;
	}

	/**
	 * Gets the label for the selected refund reason in case the event has a target of Combobox
	 *
	 * @param event
	 * 		the event that was fired
	 * @return the RefundReason corresponding to the label
	 */
	protected Optional<RefundReason> getCustomSelectedRefundReason(final Event event)
	{
		Optional<RefundReason> reason = Optional.empty();
		if (event.getTarget() instanceof Combobox)
		{
			final Object selectedValue = event.getData();
			reason = matchingComboboxReturnReason(selectedValue.toString());
		}
		return reason;
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
		for (final Component row : returnEntries.getRows().getChildren())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox && (((Checkbox) firstComponent).isChecked()))
			{
				applyToRow(data, childrenIndex, row);
			}
		}
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
			if (index != childrenIndex)
			{
				index++;
				continue;
			}

			if (myComponent instanceof Checkbox && data != null)
			{
				((Checkbox) myComponent).setChecked(((Boolean) data).booleanValue());
			}
			if (myComponent instanceof Combobox)
			{
				if (!(data instanceof Integer))
				{
					((Combobox) myComponent).setSelectedItem(null);
				}
				else
				{
					((Combobox) myComponent).setSelectedIndex(((Integer) data).intValue());
				}
			}
			else if (myComponent instanceof Intbox)
			{
				((Intbox) myComponent).setValue((Integer) data);
			}
			else if (myComponent instanceof Doublebox)
			{
				((Doublebox) myComponent).setValue((Double) data);
			}
			else if (myComponent instanceof Textbox)
			{
				((Textbox) myComponent).setValue((String) data);
			}
			index++;
		}
	}

	/**
	 * Reinitialize the popup.
	 */
	@ViewEvent(componentID = "resetcreatereturnrequest", eventName = Events.ON_CLICK)
	public void reset()
	{
		globalReason.setSelectedItem(null);
		globalComment.setValue("");
		initCreateReturnRequestForm(getOrder());
		calculateTotalRefundAmount();
	}

	/**
	 * Create the return request
	 */
	@ViewEvent(componentID = "confirmcreatereturnrequest", eventName = Events.ON_CLICK)
	public void confirmCreation()
	{
		validateRequest();

		try
		{
			final ReturnRequestModel returnRequest = getReturnService().createReturnRequest(getOrder());
			returnRequest.setRefundDeliveryCost(refundDeliveryCost.isChecked());
			final ReturnStatus status = isReturnInstore.isChecked() ? ReturnStatus.RECEIVED : ReturnStatus.APPROVAL_PENDING;
			returnRequest.setStatus(status);
			getModelService().save(returnRequest);

			returnEntriesToCreate.stream().filter(entry -> entry.getQuantityToReturn() != 0)
					.forEach(entry -> createRefundWithCustomAmount(returnRequest, entry));

			applyReturnRequest(returnRequest);

			final CreateReturnEvent createReturnEvent = new CreateReturnEvent();
			createReturnEvent.setReturnRequest(returnRequest);
			getEventService().publishEvent(createReturnEvent);

			getNotificationService()
					.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
							getWidgetInstanceManager().getLabel("customersupportbackoffice.createreturnrequest.confirm.success") + " - "
									+ returnRequest.getRMA());
		}
		catch (final Exception e)
		{
			LOG.info(e.getMessage(), e);
			getNotificationService()
					.notifyUser(Strings.EMPTY, CustomersupportbackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							getWidgetInstanceManager().getLabel("customersupportbackoffice.createreturnrequest.confirm.error"));
		}

		sendOutput(OUT_CONFIRM, COMPLETED);
	}

	/**
	 * Apply the return request
	 */
	private void applyReturnRequest(final ReturnRequestModel returnRequest) throws OrderReturnRecordsHandlerException
	{
		try
		{
			getRefundService().apply(returnRequest.getOrder(), returnRequest);
		}
		catch (final IllegalStateException ise) //NOSONAR
		{
			LOG.info("Order " + getOrder().getCode() + " Return record already in progress"); //NOSONAR
		}
	}

	/**
	 * Create and set the custom price of the refund entry.
	 *
	 * @param returnRequest
	 * 		the associated {@link ReturnRequestModel} to the {@link RefundEntryModel}
	 * @param entry
	 * 		the {@link RefundEntryModel} use to get the custom informations
	 * @return the newly {@link RefundEntryModel} created
	 */
	protected RefundEntryModel createRefundWithCustomAmount(final ReturnRequestModel returnRequest,
			final ReturnEntryToCreateDto entry)
	{
		final ReturnAction actionToExecute = isReturnInstore.isChecked() ? ReturnAction.IMMEDIATE : ReturnAction.HOLD;
		final RefundEntryModel refundEntryToBeCreated = getReturnService()
				.createRefund(returnRequest, entry.getRefundEntry().getOrderEntry(), entry.getRefundEntryComment(),
						Long.valueOf(entry.getQuantityToReturn()), actionToExecute, entry.getRefundEntry().getReason());

		refundEntryToBeCreated.setAmount(entry.getRefundEntry().getAmount());
		returnRequest.setSubtotal(returnRequest.getSubtotal().add(entry.getRefundEntry().getAmount()));
		getModelService().save(refundEntryToBeCreated);

		return refundEntryToBeCreated;
	}

	/**
	 * Validate each return entry and throw a {@link WrongValueException} if it fails any check
	 *
	 * @param entry
	 * 		the individual entry to validate
	 */
	protected void validateReturnEntry(final ReturnEntryToCreateDto entry)
	{
		if (entry.getQuantityToReturn() > entry.getReturnableQuantity())
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(
					entry.getRefundEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_RETURN_QUANTITY);
			throw new WrongValueException(quantity,
					getLabel("customersupportbackoffice.createreturnrequest.validation.invalid.quantity"));
		}
		else if (entry.getRefundEntry().getReason() != null && entry.getQuantityToReturn() == 0)
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(
					entry.getRefundEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_RETURN_QUANTITY);
			throw new WrongValueException(quantity,
					getLabel("customersupportbackoffice.createreturnrequest.validation.missing.quantity"));
		}
		else if (entry.getRefundEntry().getReason() == null && entry.getQuantityToReturn() > 0)
		{
			final Combobox combobox = (Combobox) targetFieldToApplyValidation(
					entry.getRefundEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_RETURN_REASON);
			throw new WrongValueException(combobox,
					getLabel("customersupportbackoffice.createreturnrequest.validation.missing.reason"));
		}
		else if (entry.getQuantityToReturn() > 0 && entry.getRefundEntry().getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			final InputElement amountInput = (InputElement) targetFieldToApplyValidation(
					entry.getRefundEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_RETURN_AMOUNT);
			throw new WrongValueException(amountInput,
					getLabel("customersupportbackoffice.createreturnrequest.validation.invalid.amount"));
		}
	}

	/**
	 * Check if the data provided by the form are compliant with the validation rules
	 */
	protected void validateRequest()
	{
		for (final Component row : getReturnEntries().getRows().getChildren())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox && ((Checkbox) firstComponent).isChecked())
			{
				final InputElement returnQty = (InputElement) row.getChildren().get(COLUMN_INDEX_RETURN_QUANTITY);
				if (returnQty.getRawValue().equals(0))
				{
					throw new WrongValueException(returnQty,
							getLabel("customersupportbackoffice.createreturnrequest.validation.missing.quantity"));
				}
			}
		}

		final ListModelList<ReturnEntryToCreateDto> modelList = (ListModelList) getReturnEntries().getModel();

		if (modelList.stream().allMatch(entry -> entry.getQuantityToReturn() == 0))
		{
			throw new WrongValueException(globalReturnEntriesSelection,
					getLabel("customersupportbackoffice.createreturnrequest.validation.missing.selectedLine"));
		}
		else
		{
			modelList.forEach(this::validateReturnEntry);
		}
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
		for (final Component component : returnEntries.getRows().getChildren())
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
	 * Retrieve the {@link de.hybris.platform.basecommerce.enums.RefundReason} according to the selected label in the
	 * combobox
	 *
	 * @param refundReasonLabel
	 * 		a {@link String}
	 * @return the return reason corresponding to the label
	 */
	protected Optional<RefundReason> matchingComboboxReturnReason(final String refundReasonLabel)
	{
		return getEnumerationService().getEnumerationValues(RefundReason.class).stream().filter(
				reason -> getEnumerationService().getEnumerationName(reason, getCockpitLocaleService().getCurrentLocale())
						.equals(refundReasonLabel)).findFirst();
	}

	/**
	 * Sets the total discounts doublebox based on {@link OrderModel} applied discounts
	 */
	protected void setTotalDiscounts()
	{
		Double totalDiscount = getOrder().getTotalDiscounts() != null ? getOrder().getTotalDiscounts() : 0.0;

		totalDiscount += getOrder().getEntries().stream()
				.mapToDouble(entry -> entry.getDiscountValues().stream().mapToDouble(DiscountValue::getAppliedValue).sum()).sum();

		totalDiscounts.setValue(totalDiscount);
	}

	protected OrderModel getOrder()
	{
		return order;
	}

	public void setOrder(final OrderModel order)
	{
		this.order = order;
	}

	public Grid getReturnEntries()
	{
		return returnEntries;
	}

	public void setReturnEntries(final Grid returnEntries)
	{
		this.returnEntries = returnEntries;
	}

	public ReturnService getReturnService()
	{
		return returnService;
	}

	public void setReturnService(final ReturnService returnService)
	{
		this.returnService = returnService;
	}

	public EventService getEventService()
	{
		return eventService;
	}

	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected BackofficeLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	public void setCockpitLocaleService(final BackofficeLocaleService cockpitLocaleService)
	{
		this.cockpitLocaleService = cockpitLocaleService;
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}

	protected RefundService getRefundService()
	{
		return refundService;
	}

	public void setRefundService(final RefundService refundService)
	{
		this.refundService = refundService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
