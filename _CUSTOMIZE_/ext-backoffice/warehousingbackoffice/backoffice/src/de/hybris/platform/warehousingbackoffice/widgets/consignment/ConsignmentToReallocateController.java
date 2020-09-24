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
package de.hybris.platform.warehousingbackoffice.widgets.consignment;

import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.constants.WarehousingConstants;
import de.hybris.platform.warehousing.data.allocation.DeclineEntries;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.enums.DeclineReason;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;
import de.hybris.platform.warehousing.sourcing.filter.SourcingFilterProcessor;
import de.hybris.platform.warehousing.stock.services.impl.DefaultWarehouseStockService;
import de.hybris.platform.warehousingbackoffice.dtos.ConsignmentEntryToReallocateDto;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hybris.backoffice.i18n.BackofficeLocaleService;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.DefaultWidgetController;
import org.apache.commons.collections4.CollectionUtils;
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
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;


/**
 * This controller creates a pop-up to reallocate part or entire consignment. It offers to select manually the new
 * location where to reallocate. If not specified, the reallocation will be automatic. It also offers to set the reason
 * for the decline as well as an optional comment.
 */
public class ConsignmentToReallocateController extends DefaultWidgetController
{
	private static final long serialVersionUID = 1L;

	protected static final String IN_SOCKET = "consignmentInput";
	protected static final String OUT_CONFIRM = "confirmOutput";
	protected static final Object COMPLETED = "completed";

	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String REALLOCATE_CONSIGNMENT_CHOICE = "reallocateConsignment";
	protected static final String DECLINE_ENTRIES = "declineEntries";

	protected static final int COLUMN_INDEX_REALLOCATION_QUANTITY = 4;
	protected static final int COLUMN_INDEX_REALLOCATION_REASON = 5;
	protected static final int COLUMN_INDEX_REALLOCATION_LOCATION = 6;
	protected static final int COLUMN_INDEX_REALLOCATION_COMMENT = 7;
	private final List<String> declineReasons = new ArrayList<>();
	private final Set<WarehouseModel> locations = Sets.newHashSet();
	private transient Set<ConsignmentEntryToReallocateDto> consignmentsEntriesToReallocate;
	private ConsignmentModel consignment;
	@Wire
	private Textbox consignmentCode;
	@Wire
	private Textbox customerName;
	@Wire
	private Combobox globalDeclineReasons;
	@Wire
	private Textbox globalDeclineComment;
	@Wire
	private Grid consignmentEntries;
	@Wire
	private Combobox globalPossibleLocations;
	@Wire
	private Checkbox globalDeclineEntriesSelection;
	@WireVariable
	private transient SourcingFilterProcessor sourcingFilterProcessor;
	@WireVariable
	private transient WarehouseService warehouseService;
	@WireVariable
	private transient EnumerationService enumerationService;
	@WireVariable
	private transient WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;
	@WireVariable
	private transient ModelService modelService;
	@WireVariable
	private transient BackofficeLocaleService cockpitLocaleService;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;
	@WireVariable
	private transient DefaultWarehouseStockService warehouseStockService;
	@WireVariable
	private transient NotificationService notificationService;

	// public methods

	/**
	 * Initialize the popup to reallocate consignment entries
	 *
	 * @param inputObject
	 * 		the consignment for which we want to ask for one or several reallocation(s)
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initReallocationConsignmentForm(final ConsignmentModel inputObject)
	{
		declineReasons.clear();
		locations.clear();

		globalDeclineEntriesSelection.setChecked(false);

		setConsignment(inputObject);

		getWidgetInstanceManager().setTitle(
				getWidgetInstanceManager().getLabel("warehousingbackoffice.reallocationconsignment.title") + " " + getConsignment()
						.getCode());
		consignmentCode.setValue(getConsignment().getCode());
		customerName.setValue(getConsignment().getOrder().getUser().getDisplayName());

		// Populate the list of decline reasons
		final Locale locale = getCockpitLocaleService().getCurrentLocale();
		getEnumerationService().getEnumerationValues(DeclineReason.class).stream()
				.filter(reason -> !reason.equals(DeclineReason.ASNCANCELLATION))
				.forEach(reason -> declineReasons.add(getEnumerationService().getEnumerationName(reason, locale)));

		// Populates the list of potential locations
		sourcingFilterProcessor.filterLocations(inputObject.getOrder(), locations);
		if (locations.contains(getConsignment().getWarehouse()))
		{
			locations.remove(getConsignment().getWarehouse());
		}

		globalDeclineReasons.setModel(new ListModelArray<String>(declineReasons));
		globalPossibleLocations.setModel(new ListModelArray(locations.toArray()));

		consignmentsEntriesToReallocate = new HashSet<>();
		getConsignment().getConsignmentEntries().stream().filter(entry -> entry.getQuantityPending().longValue() > 0).forEach(
				entry -> consignmentsEntriesToReallocate.add(new ConsignmentEntryToReallocateDto(entry, declineReasons, locations)));

		getConsignmentEntries().setModel(new ListModelList<ConsignmentEntryToReallocateDto>(consignmentsEntriesToReallocate));
		getConsignmentEntries().renderAll();
		addListeners();
	}

	/**
	 * Confirm the reallocation request
	 */
	@ViewEvent(componentID = "confirmreallocation", eventName = Events.ON_CLICK)
	public void confirmReallocation() throws InterruptedException
	{
		validateRequest();

		final String consignmentProcessCode = consignment.getCode() + WarehousingConstants.CONSIGNMENT_PROCESS_CODE_SUFFIX;
		final Optional<ConsignmentProcessModel> myConsignmentProcess = consignment.getConsignmentProcesses().stream()
				.filter(consignmentProcess -> consignmentProcess.getCode().equals(consignmentProcessCode)).findFirst();

		final Collection<DeclineEntry> entriesToReallocate = new ArrayList<>();

		if (myConsignmentProcess.isPresent())
		{
			final List<Component> rows = consignmentEntries.getRows().getChildren();
			rows.stream().filter(entry -> ((Checkbox) (entry.getFirstChild())).isChecked())
					.forEach(entry -> createDeclineEntry(entriesToReallocate, entry));
		}

		if (!entriesToReallocate.isEmpty())
		{
			buildDeclineParam(myConsignmentProcess.get(), entriesToReallocate);

			try
			{
				getConsignmentBusinessProcessService()
						.triggerChoiceEvent(getConsignment(), CONSIGNMENT_ACTION_EVENT_NAME, REALLOCATE_CONSIGNMENT_CHOICE);
			}
			catch (final BusinessProcessException e) //NOSONAR
			{
				getNotificationService()
						.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
								getLabel("warehousingbackoffice.reallocationconsignment.error.message"));

			}

			final ConsignmentModel refreshedConsignment = getModelService().get(getConsignment().getPk());

			final int iterationLimit = 500000;
			int iterationCount = 0;
			while (!isDeclineProcessDone(refreshedConsignment, entriesToReallocate) && iterationCount < iterationLimit)
			{
				getModelService().refresh(refreshedConsignment);
				iterationCount++;
			}

			refreshedConsignment.getConsignmentEntries().forEach(entry -> getCockpitEventQueue()
					.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, entry, null)));

			setConsignment(refreshedConsignment);

			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.SUCCESS,
							getLabel("warehousingbackoffice.reallocationconsignment.success.message"));
		}
		else
		{
			getNotificationService()
					.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
							getLabel("warehousingbackoffice.reallocationconsignment.error.message"));
		}

		sendOutput(OUT_CONFIRM, COMPLETED);
	}

	/**
	 * Creates a decline entry for the current row
	 *
	 * @param entriesToReallocate
	 * 		the list to which the newly created decline entry will be added to
	 * @param component
	 * 		the current row from which the decline entry will be created
	 */
	protected void createDeclineEntry(final Collection<DeclineEntry> entriesToReallocate, final Component component)
	{
		final ConsignmentEntryToReallocateDto consignmentEntryToReallocate = ((Row) component).getValue();
		final Long qtyToReallocate = consignmentEntryToReallocate.getQuantityToReallocate();
		final Long qtyAvailableForReallocation = consignmentEntryToReallocate.getConsignmentEntry().getQuantityPending();

		if (qtyToReallocate.longValue() > 0 && qtyToReallocate.longValue() <= qtyAvailableForReallocation.longValue())
		{
			final DeclineEntry newEntry = new DeclineEntry();
			newEntry.setQuantity(qtyToReallocate);
			newEntry.setConsignmentEntry(consignmentEntryToReallocate.getConsignmentEntry());
			newEntry.setNotes(consignmentEntryToReallocate.getDeclineConsignmentEntryComment());
			newEntry.setReallocationWarehouse(consignmentEntryToReallocate.getSelectedLocation());
			newEntry.setReason(consignmentEntryToReallocate.getSelectedReason());

			entriesToReallocate.add(newEntry);
		}
	}

	/**
	 * Reinitialize the popup.
	 */
	@ViewEvent(componentID = "undoreallocation", eventName = Events.ON_CLICK)
	public void reset()
	{
		globalDeclineReasons.setSelectedItem(null);
		globalPossibleLocations.setSelectedItem(null);
		globalDeclineComment.setValue("");

		initReallocationConsignmentForm(getConsignment());
	}

	// protected methods

	/**
	 * Add listeners on Intbox, Combobox and Textbox so that when a change happens on one of these components, we are
	 * catching it.
	 */
	protected void addListeners()
	{
		final List<Component> rows = consignmentEntries.getRows().getChildren();
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
					myComponent.addEventListener(Events.ON_SELECT, this::handleIndividualLocation);
					myComponent.addEventListener("onCustomChange",
							event -> Events.echoEvent("onLaterCustomChange", myComponent, event.getData()));
					myComponent.addEventListener("onLaterCustomChange", event -> {
						Clients.clearWrongValue(myComponent);
						myComponent.invalidate();
						handleIndividualReason(event);
					});

				}
				else if (myComponent instanceof Intbox)
				{
					myComponent.addEventListener(Events.ON_CHANGE, event -> {
						autoSelect(event);
						((ConsignmentEntryToReallocateDto) ((Row) event.getTarget().getParent()).getValue())
								.setQuantityToReallocate(Long.parseLong(((InputEvent) event).getValue()));
					});
				}
				else if (myComponent instanceof Textbox)
				{
					myComponent.addEventListener(Events.ON_CHANGING, event -> {
						autoSelect(event);
						((ConsignmentEntryToReallocateDto) ((Row) event.getTarget().getParent()).getValue())
								.setDeclineConsignmentEntryComment(((InputEvent) event).getValue());
					});
				}
			}
		}

		globalDeclineReasons.addEventListener(Events.ON_SELECT, this::handleGlobalReason);
		globalPossibleLocations.addEventListener(Events.ON_SELECT, this::handleGlobalLocation);
		globalDeclineComment.addEventListener(Events.ON_CHANGING, this::handleGlobalComment);
		globalDeclineEntriesSelection.addEventListener(Events.ON_CHECK, event -> selectAllEntries());
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
		consignmentEntries.getRows().getChildren().stream()
				.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked())
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
				applyToCheckboxRow(data, myComponent);
				applyToComboboxRow(data, myComponent);
				if (myComponent instanceof Intbox)
				{
					((Intbox) myComponent).setValue((Integer) data);
				}
				else if (!(myComponent instanceof Combobox) && (myComponent instanceof Textbox))
				{
					((Textbox) myComponent).setValue((String) data);
				}
			}
			index++;
		}
	}

	/**
	 * Applies a specific change if the current element is a combobox
	 *
	 * @param data
	 * 		the element for which the change has to be applied
	 * @param component
	 * 		the component which will have the change applied
	 */
	protected void applyToComboboxRow(final Object data, final Component component)
	{
		if (component instanceof Combobox)
		{
			if (data == null)
			{
				((Combobox) component).setSelectedItem(null);
			}
			else
			{
				((Combobox) component).setSelectedIndex((Integer) data);
			}
		}
	}

	/**
	 * Applies a specific change if the current element is a checkbox
	 *
	 * @param data
	 * 		the element for which the change has to be applied
	 * @param component
	 * 		the component which will have the change applied
	 */
	protected void applyToCheckboxRow(final Object data, final Component component)
	{
		if (component instanceof Checkbox)
		{
			if (data == null)
			{
				((Checkbox) component).setChecked(Boolean.FALSE);
			}
			else
			{
				((Checkbox) component).setChecked((Boolean) data);
			}
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
	 * Build and save the context parameter for decline entries and set it into the given process
	 *
	 * @param processModel
	 * 		the process model for which the context parameters has to be register
	 * @param entriesToReallocate
	 * 		the entries to be reallocated
	 */
	protected void buildDeclineParam(final ConsignmentProcessModel processModel,
			final Collection<DeclineEntry> entriesToReallocate)
	{
		cleanDeclineParam(processModel);

		final Collection<BusinessProcessParameterModel> contextParams = new ArrayList<>();
		contextParams.addAll(processModel.getContextParameters());

		final DeclineEntries declinedEntries = new DeclineEntries();
		declinedEntries.setEntries(entriesToReallocate);
		final BusinessProcessParameterModel declineParam = new BusinessProcessParameterModel();
		declineParam.setName(DECLINE_ENTRIES);
		declineParam.setValue(declinedEntries);
		declineParam.setProcess(processModel);
		contextParams.add(declineParam);

		processModel.setContextParameters(contextParams);
		getModelService().save(processModel);
	}

	/**
	 * Removes the old decline entries from {@link ConsignmentProcessModel#CONTEXTPARAMETERS}(if any exists), before attempting to decline
	 *
	 * @param processModel
	 * 		the {@link ConsignmentProcessModel} for the consignment to be declined
	 */
	protected void cleanDeclineParam(final ConsignmentProcessModel processModel)
	{
		final Collection<BusinessProcessParameterModel> contextParams = new ArrayList<>();
		contextParams.addAll(processModel.getContextParameters());
		if (CollectionUtils.isNotEmpty(contextParams))
		{
			final Optional<BusinessProcessParameterModel> declineEntriesParamOptional = contextParams.stream()
					.filter(param -> param.getName().equals(DECLINE_ENTRIES)).findFirst();
			if (declineEntriesParamOptional.isPresent())
			{
				final BusinessProcessParameterModel declineEntriesParam = declineEntriesParamOptional.get();
				contextParams.remove(declineEntriesParam);
				getModelService().remove(declineEntriesParam);

				processModel.setContextParameters(contextParams);
				getModelService().save(processModel);
			}
		}
	}

	/**
	 * Gets the location index within the combobox model
	 *
	 * @param location
	 * 		the location which the item(s) will be reallocated to
	 * @return the index of the given location
	 */
	protected int getLocationIndex(final WarehouseModel location)
	{
		int index = 0;
		for (final WarehouseModel warehouseModel : locations)
		{
			if (location.getCode().equals(warehouseModel.getCode()))
			{
				break;
			}
			index++;
		}
		return index;
	}

	/**
	 * Gets the reason index within the combobox model
	 *
	 * @param declineReason
	 * 		the reason for which we want to know the index
	 * @return the index of the given decline reason
	 */
	protected int getReasonIndex(final DeclineReason declineReason)
	{
		int index = 0;
		final String myReason = getEnumerationService()
				.getEnumerationName(declineReason, getCockpitLocaleService().getCurrentLocale());
		for (final String reason : declineReasons)
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
	 * get the corresponding decline reason according to the selected item in the combobox if existing
	 *
	 * @param event
	 * 		the select event on the combobox
	 * @return the corresponding decline reason
	 */
	protected Optional<DeclineReason> getSelectedDeclineReason(final Event event)
	{
		Optional<DeclineReason> result = Optional.empty();
		if (!((SelectEvent) event).getSelectedItems().isEmpty())
		{
			final Object selectedValue = ((Comboitem) ((SelectEvent) event).getSelectedItems().iterator().next()).getValue();
			result = matchingComboboxDeclineReason(selectedValue.toString());
		}
		return result;
	}

	/**
	 * Gets the label for the selected decline reason in case the event has a target of Combobox
	 *
	 * @param event
	 * 		the event that was fired
	 * @return the DeclineReason corresponding to the label.
	 */
	protected Optional<DeclineReason> getCustomSelectedDeclineReason(final Event event)
	{
		Optional<DeclineReason> reason = Optional.empty();
		if (event.getTarget() instanceof Combobox)
		{
			final Object selectedValue = event.getData();
			reason = matchingComboboxDeclineReason(selectedValue.toString());
		}
		return reason;
	}

	/**
	 * Get the corresponding warehouse location according to the selected item in the combobox if anything is selected
	 *
	 * @param event
	 * 		the select event on the combobox
	 * @return the corresponding warehouse location
	 */
	protected WarehouseModel getSelectedLocation(final Event event)
	{
		WarehouseModel result = null;
		if (!((SelectEvent) event).getSelectedItems().isEmpty())
		{
			result = ((Comboitem) ((SelectEvent) event).getSelectedItems().iterator().next()).getValue();
		}
		return result;
	}

	/**
	 * Updates all entries with the selected global comment
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleGlobalComment(final Event event)
	{
		applyToGrid(((InputEvent) event).getValue(), COLUMN_INDEX_REALLOCATION_COMMENT);

		consignmentEntries.getRows().getChildren().stream()
				.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked()).forEach(
				entry -> ((ConsignmentEntryToReallocateDto) ((Row) entry).getValue())
						.setDeclineConsignmentEntryComment(((InputEvent) event).getValue()));
	}

	/**
	 * Updates all entries with the selected global location
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleGlobalLocation(final Event event)
	{
		final WarehouseModel selectedLocation = getSelectedLocation(event);
		if (selectedLocation != null)
		{
			applyToGrid(Integer.valueOf(getLocationIndex(selectedLocation)), COLUMN_INDEX_REALLOCATION_LOCATION);

			consignmentEntries.getRows().getChildren().stream()
					.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked()).forEach(
					entry -> ((ConsignmentEntryToReallocateDto) ((Row) entry).getValue()).setSelectedLocation(selectedLocation));
		}
	}

	/**
	 * Updates all entries with the selected global reason
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleGlobalReason(final Event event)
	{
		final Optional<DeclineReason> declineReason = getSelectedDeclineReason(event);
		if (declineReason.isPresent())
		{
			applyToGrid(Integer.valueOf(getReasonIndex(declineReason.get())), COLUMN_INDEX_REALLOCATION_REASON);

			consignmentEntries.getRows().getChildren().stream()
					.filter(entry -> ((Checkbox) (entry.getChildren().iterator().next())).isChecked()).forEach(
					entry -> ((ConsignmentEntryToReallocateDto) ((Row) entry).getValue()).setSelectedReason(declineReason.get()));
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
		final Optional<DeclineReason> declineReason = getCustomSelectedDeclineReason(event);
		if (declineReason.isPresent())
		{
			autoSelect(event);
			((ConsignmentEntryToReallocateDto) ((Row) event.getTarget().getParent()).getValue())
					.setSelectedReason(declineReason.get());
		}
	}

	/**
	 * Updates an individual entry with the selected location
	 *
	 * @param event
	 * 		the event that triggered the change
	 */
	protected void handleIndividualLocation(final Event event)
	{
		if (!((SelectEvent) event).getSelectedItems().isEmpty())
		{
			autoSelect(event);
			final Object selectedValue = ((Comboitem) ((SelectEvent) event).getSelectedItems().iterator().next()).getValue();
			if (selectedValue instanceof WarehouseModel)
			{
				((ConsignmentEntryToReallocateDto) ((Row) event.getTarget().getParent()).getValue())
						.setSelectedLocation((WarehouseModel) selectedValue);
			}
		}
	}

	/**
	 * Set or Reset the given row with the appropriate default informations
	 *
	 * @param row
	 * 		the row to set/reset
	 */
	protected void handleRow(final Row row)
	{
		final ConsignmentEntryToReallocateDto myEntry = row.getValue();

		if (row.getChildren().iterator().next() instanceof Checkbox)
		{
			if (!((Checkbox) row.getChildren().iterator().next()).isChecked())
			{
				applyToRow(Integer.valueOf(0), COLUMN_INDEX_REALLOCATION_QUANTITY, row);
				applyToRow(null, COLUMN_INDEX_REALLOCATION_REASON, row);
				applyToRow(null, COLUMN_INDEX_REALLOCATION_LOCATION, row);
				applyToRow(null, COLUMN_INDEX_REALLOCATION_COMMENT, row);

				myEntry.setQuantityToReallocate(0L);
				myEntry.setSelectedReason(null);
				myEntry.setSelectedLocation(null);
				myEntry.setDeclineConsignmentEntryComment(null);
			}
			else
			{
				applyToRow(Integer.valueOf(globalDeclineReasons.getSelectedIndex()), COLUMN_INDEX_REALLOCATION_REASON, row);
				applyToRow(globalPossibleLocations.getSelectedIndex(), COLUMN_INDEX_REALLOCATION_LOCATION, row);
				applyToRow(globalDeclineComment.getValue(), COLUMN_INDEX_REALLOCATION_COMMENT, row);

				final Optional<DeclineReason> reason = matchingComboboxDeclineReason(
						(globalDeclineReasons.getSelectedItem() != null) ? globalDeclineReasons.getSelectedItem().getLabel() : null);

				myEntry.setSelectedReason(reason.isPresent() ? reason.get() : null);
				myEntry.setSelectedLocation((globalPossibleLocations.getSelectedItem() != null) ?
						globalPossibleLocations.getSelectedItem().getValue() :
						null);
				myEntry.setDeclineConsignmentEntryComment(globalDeclineComment.getValue());
			}
		}
	}

	/**
	 * Check if the decline process is done or not.
	 *
	 * @param latestConsignmentModel
	 * 		the updated consignment model
	 * @param entriesToReallocate
	 * 		the entries to be reallocated
	 * @return true if the process is done. Otherwise false
	 */
	protected boolean isDeclineProcessDone(final ConsignmentModel latestConsignmentModel,
			final Collection<DeclineEntry> entriesToReallocate)
	{
		return entriesToReallocate.stream().allMatch(entry -> isDeclinedQuantityCorrect(latestConsignmentModel, entry));
	}

	/**
	 * Check if the declined quantity of the passed consignment model matches the expected quantity to be declined.
	 *
	 * @param latestConsignmentModel
	 * @param declineEntry
	 * @return true if the declined quantity is matching the expected declined quantity. Otherwise, returns false.
	 */
	protected boolean isDeclinedQuantityCorrect(final ConsignmentModel latestConsignmentModel, final DeclineEntry declineEntry)
	{
		final Long expectedDeclinedQuantity = Long.valueOf(
				declineEntry.getConsignmentEntry().getQuantityDeclined().longValue() + declineEntry.getQuantity().longValue());
		return latestConsignmentModel.getConsignmentEntries().stream().anyMatch(
				entry -> entry.getPk().equals(declineEntry.getConsignmentEntry().getPk()) && expectedDeclinedQuantity
						.equals(entry.getQuantityDeclined()));
	}

	/**
	 * Retrieve the {@link de.hybris.platform.warehousing.enums.DeclineReason} according to the selected label in the
	 * combobox
	 *
	 * @param declineReasonLabel
	 * 		a {@link String}
	 * @return the decline reason corresponding to the label
	 */
	protected Optional<DeclineReason> matchingComboboxDeclineReason(final String declineReasonLabel)
	{
		return getEnumerationService().getEnumerationValues(DeclineReason.class).stream().filter(
				reason -> getEnumerationService().getEnumerationName(reason, getCockpitLocaleService().getCurrentLocale())
						.equals(declineReasonLabel)).findFirst();
	}

	/**
	 * Select or unselect all the rows of the grid and sets the default values for each of them.
	 */
	protected void selectAllEntries()
	{
		applyToGrid(Boolean.TRUE, 0);

		for (final Component row : consignmentEntries.getRows().getChildren())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox)
			{
				((Checkbox) firstComponent).setChecked(globalDeclineEntriesSelection.isChecked());
			}
			handleRow((Row) row);
			if (globalDeclineEntriesSelection.isChecked())
			{
				final int reallocatableQuantity = Integer.parseInt(((Label) row.getChildren().get(3)).getValue());
				applyToRow(Integer.valueOf(reallocatableQuantity), COLUMN_INDEX_REALLOCATION_QUANTITY, row);
			}
		}

		if (globalDeclineEntriesSelection.isChecked())
		{
			consignmentsEntriesToReallocate.stream()
					.forEach(entry -> entry.setQuantityToReallocate(entry.getConsignmentEntry().getQuantityPending()));
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
		for (final Component component : consignmentEntries.getRows().getChildren())
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
	 * Validate each consignment entry and throw a {@link WrongValueException} if it fails any check
	 *
	 * @param entry
	 * 		the individual entry to validate
	 */
	protected void validateConsignmentEntry(ConsignmentEntryToReallocateDto entry)
	{
		if (entry.getQuantityToReallocate() > entry.getConsignmentEntry().getQuantityPending())
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(
					entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_REALLOCATION_QUANTITY);
			throw new WrongValueException(quantity,
					getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.invalid.quantity"));
		}
		else if (entry.getSelectedReason() != null && entry.getQuantityToReallocate() == 0)
		{
			final InputElement quantity = (InputElement) targetFieldToApplyValidation(
					entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_REALLOCATION_QUANTITY);
			throw new WrongValueException(quantity,
					getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.missing.quantity"));
		}
		else if (entry.getSelectedReason() == null && entry.getQuantityToReallocate() > 0)
		{
			final Combobox reason = (Combobox) targetFieldToApplyValidation(
					entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_REALLOCATION_REASON);
			throw new WrongValueException(reason,
					getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.missing.reason"));
		}
		else if (entry.getSelectedLocation() != null && getWarehouseStockService()
				.getStockLevelForProductCodeAndWarehouse(entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(),
						entry.getSelectedLocation()) == 0)
		{
			final Combobox location = (Combobox) targetFieldToApplyValidation(
					entry.getConsignmentEntry().getOrderEntry().getProduct().getCode(), 1, COLUMN_INDEX_REALLOCATION_LOCATION);
			throw new WrongValueException(location,
					getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.invalid.stockLevel"));
		}
	}

	/**
	 * Check if the data provided by the form are compliant with the validation rules
	 */
	protected void validateRequest()
	{
		for (final Component row : getConsignmentEntries().getRows().getChildren())
		{
			final Component firstComponent = row.getChildren().iterator().next();
			if (firstComponent instanceof Checkbox && ((Checkbox) firstComponent).isChecked())
			{
				final InputElement returnQty = (InputElement) row.getChildren().get(COLUMN_INDEX_REALLOCATION_QUANTITY);
				if (returnQty.getRawValue().equals(0))
				{
					throw new WrongValueException(returnQty,
							getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.missing.quantity"));
				}
			}
		}

		final ListModelList<ConsignmentEntryToReallocateDto> modelList = ((ListModelList) getConsignmentEntries().getModel());

		if (modelList.stream().allMatch(entry -> entry.getQuantityToReallocate() == 0))
		{
			throw new WrongValueException(globalDeclineEntriesSelection,
					getLabel("warehousingbackoffice.reallocationconsignment.decline.validation.missing.selectedLine"));
		}
		else
		{
			modelList.forEach(this::validateConsignmentEntry);
		}
	}

	// getters and setters

	protected ConsignmentModel getConsignment()
	{
		return consignment;
	}

	public void setConsignment(final ConsignmentModel consignment)
	{
		this.consignment = consignment;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	protected Grid getConsignmentEntries()
	{
		return consignmentEntries;
	}

	protected WarehousingBusinessProcessService<ConsignmentModel> getConsignmentBusinessProcessService()
	{
		return consignmentBusinessProcessService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	protected BackofficeLocaleService getCockpitLocaleService()
	{
		return cockpitLocaleService;
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	protected DefaultWarehouseStockService getWarehouseStockService()
	{
		return warehouseStockService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}
}
