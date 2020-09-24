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
package de.hybris.platform.warehousingbackoffice.widgets.stocks;

import com.hybris.backoffice.i18n.BackofficeLocaleService;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.enums.StockLevelAdjustmentReason;
import de.hybris.platform.warehousingbackoffice.dtos.StockAdjustmentDto;
import de.hybris.platform.warehousingfacades.stocklevel.data.StockLevelAdjustmentData;
import de.hybris.platform.warehousingfacades.stocklevel.impl.DefaultWarehousingStockLevelFacade;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.impl.InputElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;


/**
 * This controller creates a pop-up to create one or several stock level adjustment(s).
 */
public class StockToAdjustController extends DefaultWidgetController
{
	private static final long serialVersionUID = 1L;

	protected static final String IN_SOCKET = "stockLevelInput";
	protected static final String OUT_CONFIRM = "confirmOutput";
	protected static final Object COMPLETED = "completed";

	protected static final int COLUMN_INDEX_REASON = 0;
	protected static final int COLUMN_INDEX_QTY = 1;

	private transient Set<StockAdjustmentDto> stockAdjustmentsToCreate = new HashSet<>();
	private StockLevelModel stockLevel;
	private final List<String> stockAdjustmentReasons = new ArrayList<>();

	@Wire
	private Textbox productCode;
	@Wire
	private Textbox warehouseName;
	@Wire
	private Textbox bin;
	@Wire
	private Grid stockAdjustments;
	@WireVariable
	private transient EnumerationService enumerationService;
	@WireVariable
	private transient ModelService modelService;
	@WireVariable
	private transient com.hybris.backoffice.i18n.BackofficeLocaleService cockpitLocaleService;
	@WireVariable
	private transient DefaultWarehousingStockLevelFacade warehousingStockLevelFacade;
	@WireVariable
	private transient NotificationService notificationService;

	/**
	 * Initialize the popup to create stock level adjustment(s)
	 *
	 * @param inputObject
	 * 		the {@link StockLevelModel} for which we want to create one or several stock level adjustment(s)
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initStockLevelAdjustmentForm(final StockLevelModel inputObject)
	{
		stockAdjustmentReasons.clear();

		setStockLevel(inputObject);

		getWidgetInstanceManager().setTitle(
				getWidgetInstanceManager().getLabel("warehousingbackoffice.stockadjustment.title") + " " + getStockLevel()
						.getProductCode());
		productCode.setValue(getStockLevel().getProductCode());
		warehouseName.setValue(getStockLevel().getWarehouse().getName());
		bin.setValue(getStockLevel().getBin());


		// Populate the list of stock level adjustment reasons
		getEnumerationService().getEnumerationValues(StockLevelAdjustmentReason.class)
				.forEach(reason -> stockAdjustmentReasons.add(getEnumerationService().getEnumerationName(reason, getLocale())));

		stockAdjustmentsToCreate.add(new StockAdjustmentDto(stockAdjustmentReasons));
		refreshGrid();
	}

	/**
	 * Add a new row of stock adjustment reason.
	 */
	@ViewEvent(componentID = "addadjustment", eventName = Events.ON_CLICK)
	public void addAdjustment()
	{
		if (stockAdjustmentsToCreate.size() < StockLevelAdjustmentReason.values().length)
		{
			stockAdjustmentsToCreate.add(new StockAdjustmentDto(stockAdjustmentReasons));
			refreshGrid();
		}
	}

	/**
	 * Reinitialize the popup.
	 */
	@ViewEvent(componentID = "reset", eventName = Events.ON_CLICK)
	public void reset()
	{
		stockAdjustmentsToCreate.clear();
		stockAdjustmentsToCreate.add(new StockAdjustmentDto(stockAdjustmentReasons));

		refreshGrid();
	}

	/**
	 * Refresh the rendering of the grid of stock adjustment to create
	 */
	protected void refreshGrid()
	{
		getStockAdjustments().setModel(new ListModelList<Object>(stockAdjustmentsToCreate));
		getStockAdjustments().renderAll();
		addListeners();

		getStockAdjustments().getRows().getChildren().stream().filter(myRow -> myRow instanceof Row)
				.forEach(myRow -> manageFieldsVisibility((Row) myRow, ((Row) myRow).getValue()));
	}

	/**
	 * Add listeners on Intbox, Combobox and Textbox so that when a change happens on one of these components, we are
	 * catching it.
	 */
	protected void addListeners()
	{
		final List<Component> rows = getStockAdjustments().getRows().getChildren();
		for (final Component row : rows)
		{
			for (final Component myComponent : row.getChildren())
			{
				if (myComponent instanceof Vbox)
				{
					addListComponentListeners(myComponent);
				}
				else if (myComponent instanceof Hbox)
				{
					addButtonListeners(myComponent);
				}
			}
		}
	}

	/**
	 * Adds a listener according to the type of component inside a vertical box
	 *
	 * @param component
	 * 		the vertical box component containing the element to target to add a listener
	 */
	protected void addListComponentListeners(final Component component)
	{
		for (final Component myComponent : component.getChildren())
		{
			if (myComponent instanceof Combobox)
			{
				myComponent.addEventListener("onCustomChange",
						event -> Events.echoEvent("onLaterCustomChange", myComponent, event.getData()));
				myComponent.addEventListener("onLaterCustomChange", event -> {
					Clients.clearWrongValue(myComponent);
					myComponent.invalidate();
					handleIndividualReason(event);
				});
				myComponent.addEventListener(Events.ON_SELECT, this::handleIndividualReason);
			}
			else if (myComponent instanceof Intbox)
			{
				myComponent.addEventListener(Events.ON_CHANGE,
						event -> ((StockAdjustmentDto) ((Row) event.getTarget().getParent().getParent()).getValue())
								.setQuantity(Long.parseLong(((InputEvent) event).getValue())));
			}
			else if (myComponent instanceof Textbox)
			{
				myComponent.addEventListener(Events.ON_CHANGING,
						event -> ((StockAdjustmentDto) ((Row) event.getTarget().getParent().getParent()).getValue())
								.setComment(((InputEvent) event).getValue()));
			}
		}
	}

	/**
	 * Updates an individual entry with the selected reason
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleIndividualReason(final Event event)
	{
		Optional<StockLevelAdjustmentReason> reason = Optional.empty();

		if (event.getTarget() instanceof Combobox)
		{
			Object selectedValue = event.getData();
			if (selectedValue == null)
			{
				selectedValue = ((Combobox) event.getTarget()).getSelectedItem().getValue();
			}
			reason = matchingComboboxStockAdjustmentReason(selectedValue.toString());
		}

		if (reason.isPresent())
		{
			final StockAdjustmentDto stockAdjustmentDto = ((Row) event.getTarget().getParent().getParent()).getValue();
			stockAdjustmentDto.setSelectedReason(reason.get());
			stockAdjustmentDto.setLocalizedStringReason(getEnumerationService().getEnumerationName(reason.get(), getLocale()));
		}
	}

	/**
	 * Retrieve the {@link StockLevelAdjustmentReason} according to the selected label in the combobox
	 *
	 * @param stockAdjustmentReasonLabel
	 * 		a {@link String}
	 * @return the {@link StockLevelAdjustmentReason} corresponding to the label
	 */
	protected Optional<StockLevelAdjustmentReason> matchingComboboxStockAdjustmentReason(final String stockAdjustmentReasonLabel)
	{
		return getEnumerationService().getEnumerationValues(StockLevelAdjustmentReason.class).stream()
				.filter(reason -> getEnumerationService().getEnumerationName(reason, getLocale()).equals(stockAdjustmentReasonLabel))
				.findFirst();
	}

	/**
	 * Add listeners on the buttons for each line
	 *
	 * @param myComponent
	 * 		the initial component
	 */
	protected void addButtonListeners(final Component myComponent)
	{
		int buttonIndex = 0;
		for (final Component button : myComponent.getChildren())
		{
			if (button instanceof Button)
			{
				switch (buttonIndex)
				{
					case 0:
						button.addEventListener(Events.ON_CLICK, this::addStockAdjustment);
						break;
					case 1:
						button.addEventListener(Events.ON_CLICK, this::editStockAdjustment);
						break;
					case 2:
						button.addEventListener(Events.ON_CLICK, this::removeStockAdjustment);
						break;
					default:
						break;
				}
				buttonIndex++;
			}
		}
	}

	/**
	 * Adds an individual stock adjustment.
	 *
	 * @param event
	 * 		the click event triggered by the user
	 */
	protected void addStockAdjustment(final Event event)
	{
		final Row myRow = (Row) event.getTarget().getParent().getParent();
		final StockAdjustmentDto stockAdjustmentDto = myRow.getValue();

		final Combobox reason = (Combobox) myRow.getChildren().get(COLUMN_INDEX_REASON).getChildren().get(0);

		if (stockAdjustmentDto.getQuantity().equals(0L))
		{
			final InputElement quantity = (InputElement) myRow.getChildren().get(COLUMN_INDEX_QTY).getChildren().get(0);
			throw new WrongValueException(quantity, getLabel("warehousingbackoffice.stockadjustment.validation.missing.quantity"));
		}
		else if (stockAdjustmentDto.getSelectedReason() == null)
		{
			throw new WrongValueException(reason, getLabel("warehousingbackoffice.stockadjustment.validation.missing.reason"));
		}
		else if (
				stockAdjustmentsToCreate.stream().filter(entry -> entry.getSelectedReason() == stockAdjustmentDto.getSelectedReason())
						.count() > 1)
		{
			throw new WrongValueException(reason, getLabel("warehousingbackoffice.stockadjustment.validation.duplicate.reason"));
		}

		stockAdjustmentDto.setUnderEdition(false);
		manageFieldsVisibility(myRow, stockAdjustmentDto);
	}

	/**
	 * Removes an individual stock adjustment.
	 *
	 * @param event
	 * 		the click event triggered by the user
	 */
	protected void removeStockAdjustment(final Event event)
	{
		final StockAdjustmentDto stockAdjustmentDto = ((Row) event.getTarget().getParent().getParent()).getValue();
		stockAdjustmentsToCreate.remove(stockAdjustmentDto);
		refreshGrid();
	}

	/**
	 * Edits an individual stock adjustment.
	 *
	 * @param event
	 * 		the click event triggered by the user
	 */
	protected void editStockAdjustment(final Event event)
	{
		final Row myRow = (Row) event.getTarget().getParent().getParent();
		final StockAdjustmentDto myStockAdjustmentDto = myRow.getValue();
		myStockAdjustmentDto.setUnderEdition(true);
		getStockAdjustments().renderAll();
		manageFieldsVisibility(myRow, myStockAdjustmentDto);
	}

	/**
	 * Handles buttons visibility
	 *
	 * @param myRow
	 * 		the row for which the visibility change is requested
	 * @param stockAdjustmentDto
	 * 		the stock adjustment dto containing informations about the reason selected
	 */
	protected void manageFieldsVisibility(final Row myRow, final StockAdjustmentDto stockAdjustmentDto)
	{
		for (final Component myComponent : myRow.getChildren())
		{
			myComponent.getChildren().stream().filter(comp -> comp instanceof Label).forEach(comp -> {
				comp.setVisible(!stockAdjustmentDto.getUnderEdition());
				comp.invalidate();
			});
			myComponent.getChildren().stream()
					.filter(comp -> comp instanceof Combobox || comp instanceof Intbox || comp instanceof Textbox).forEach(comp -> {
				comp.setVisible(stockAdjustmentDto.getUnderEdition());
				Label myLabel = (Label) comp.getParent().getChildren().get(1); //NOSONAR

				if (comp instanceof Intbox)
				{
					myLabel.setValue(((Intbox) comp).getValue().toString());
				}
				else if (comp instanceof Combobox && stockAdjustmentDto.getSelectedReason() != null)
				{
					myLabel.setValue(stockAdjustmentDto.getLocalizedStringReason());
				}
				else
				{
					myLabel.setValue(((Textbox) comp).getValue());
				}

				myLabel.invalidate();
				comp.invalidate();
			});

			manageButtonsVisibility(stockAdjustmentDto.getUnderEdition(), myComponent);
		}
	}

	/**
	 * Handles the display of the buttons according to the situation (editable or not)
	 *
	 * @param isEditable
	 * 		the line is editable or not
	 * @param myComponent
	 * 		the component containing the buttons
	 */
	protected void manageButtonsVisibility(final boolean isEditable, final Component myComponent)
	{
		int buttonIndex = 0;
		for (Component myButton : myComponent.getChildren())
		{
			if (myButton instanceof Button)
			{
				switch (buttonIndex)
				{
					case 0:
						myButton.setVisible(isEditable);
						myButton.invalidate();
						break;
					case 1:
						myButton.setVisible(!isEditable);
						myButton.invalidate();
						break;
					default:
						break;
				}
				buttonIndex++;
			}
		}
	}

	/**
	 * Confirm the creation of the listed stock level adjustments
	 */
	@ViewEvent(componentID = "confirm", eventName = Events.ON_CLICK)
	public void confirmStockAdjustmentCreation() throws InterruptedException
	{
		validateStockAdjustmentCreation();

		if (!stockAdjustmentsToCreate.isEmpty())
		{
			stockAdjustmentsToCreate.forEach(stockAdjustment -> getWarehousingStockLevelFacade()
					.createStockLevelAdjustment(stockLevel, getStockLevelAdjustmentData(stockAdjustment)));

			getNotificationService().notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
					getLabel("warehousingbackoffice.stockadjustment.success.message"));
		}
		else
		{
			getNotificationService().notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
					getLabel("warehousingbackoffice.stockadjustment.error.message"));
		}

		sendOutput(OUT_CONFIRM, COMPLETED);
	}

	/**
	 * Check if the data provided by the form are compliant with the validation rules
	 */
	protected void validateStockAdjustmentCreation()
	{
		if (getStockAdjustments().getRows().getChildren().stream().anyMatch(row -> row.getChildren().stream()
				.anyMatch(component -> component.getChildren().stream().anyMatch(child -> child instanceof Combobox))))
		{
			getStockAdjustments().getRows().getChildren()
					.forEach(row -> row.getChildren().forEach(component -> component.getChildren().forEach(child -> {
						if (child instanceof Combobox && child.isVisible())
						{
							throw new WrongValueException(child,
									getLabel("warehousingbackoffice.stockadjustment.validation.check.required"));
						}
					})));
		}
	}

	/**
	 * Instantiate a {@link StockLevelAdjustmentData} and populate it from the given {@link StockAdjustmentDto}
	 *
	 * @param stockAdjustmentDto
	 * 		the stock adjustment DTO to use for population
	 * @return the new instance of {@link StockLevelAdjustmentData}
	 */
	protected StockLevelAdjustmentData getStockLevelAdjustmentData(final StockAdjustmentDto stockAdjustmentDto)
	{
		final StockLevelAdjustmentData stockLevelAdjustmentData = new StockLevelAdjustmentData();
		stockLevelAdjustmentData.setReason(stockAdjustmentDto.getSelectedReason());
		stockLevelAdjustmentData.setQuantity(stockAdjustmentDto.getQuantity());
		stockLevelAdjustmentData.setComment(stockAdjustmentDto.getComment());
		return stockLevelAdjustmentData;
	}

	protected Locale getLocale()
	{
		return getCockpitLocaleService().getCurrentLocale();
	}

	protected StockLevelModel getStockLevel()
	{
		return stockLevel;
	}

	public void setStockLevel(StockLevelModel stockLevel)
	{
		this.stockLevel = stockLevel;
	}

	protected BackofficeLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	public Grid getStockAdjustments()
	{
		return stockAdjustments;
	}

	public DefaultWarehousingStockLevelFacade getWarehousingStockLevelFacade()
	{
		return warehousingStockLevelFacade;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
