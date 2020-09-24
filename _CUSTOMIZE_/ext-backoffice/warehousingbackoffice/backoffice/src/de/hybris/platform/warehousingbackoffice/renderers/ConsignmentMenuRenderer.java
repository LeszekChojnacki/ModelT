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
package de.hybris.platform.warehousingbackoffice.renderers;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.warehousing.labels.strategy.PrintExportFormStrategy;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import javax.annotation.Resource;

import java.util.List;
import java.util.NoSuchElementException;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelUtils;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Button;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;


/**
 * Renders the three-dots menu in order to display some options while viewing tasks in the Inbox section.
 */
public class ConsignmentMenuRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsignmentMenuRenderer.class);

	protected static final String ROW_POPUP_STYLE = "ye-inline-editor-row-popup";
	protected static final String SOCKET_OUT_CONTEXT = "reallocateContext";
	protected static final String ACTION_BUTTON = "ye-actiondots--btn ye-actiondots--consignment--btn";
	protected static final String ACTION_CELL = "ye-actiondots ye-actiondots--consignment";
	protected static final String CAN_PRINT_RETURNFORM_KEY = "warehousing.printreturnform.active";
	protected static final String CAN_PRINT_RETURNSHIPPINGLABEL_KEY = "warehousing.printreturnshippinglabel.active";
	protected static final String PICK_ACTION = "Picking";
	protected static final String PACK_ACTION = "Packing";
	protected static final String NON_SELECTABLE_TAGS = "button";
	protected static final String MENU_POPUP_POSITION = "after_end";
	protected static final String CAPTURE_PAYMENT_ON_CONSIGNMENT = "warehousing.capturepaymentonconsignment";
	private static final String EVENT_LOOPBACK = "onLoopbackRequest";

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintPickSlipStrategy;
	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintPackSlipStrategy;
	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintExportFormStrategy;
	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintShippingLabelStrategy;
	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintReturnFormStrategy;
	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintReturnShippingLabelStrategy;
	@Resource
	private PrintExportFormStrategy printExportFormStrategy;
	@Resource
	private ConfigurationService configurationService;
	@Override
	public void render(final Listcell listCell, final ListColumn columnConfiguration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Button btnPopup = new Button();
		btnPopup.setSclass(ACTION_BUTTON);

		final Menupopup menupopup = createMenuPopup((WorkflowActionModel) object, widgetInstanceManager);
		btnPopup.appendChild(menupopup);
		btnPopup.addEventListener(Events.ON_CLICK, event -> onActionClick(listCell, menupopup));

		listCell.appendChild(btnPopup);
		listCell.setSclass(ACTION_CELL);
		listCell.addEventListener(EVENT_LOOPBACK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event e) throws Exception
			{
				if (e.getData() instanceof Listcell)
				{
					final Listcell cell = (Listcell) e.getData();
					if (cell.getListbox() != null)
					{
						cell.getListbox().setNonselectableTags(NON_SELECTABLE_TAGS);
					}
				}
				listCell.removeEventListener(EVENT_LOOPBACK, this);
			}
		});
		Events.echoEvent(EVENT_LOOPBACK, listCell, listCell);
	}

	@Resource
	private List<ConsignmentStatus> reallocableConsignmentStatuses;

	/**
	 * Creates the popup menu associated to the three-dots menu. All options associated to a {@link ConsignmentModel} must be placed here.
	 *
	 * @param workflowActionModel
	 * 		the {@link WorkflowActionModel} with the attached {@link ConsignmentModel} which will be used to populate the various actions
	 * @param widgetInstanceManager
	 * 		the {@link WidgetInstanceManager}
	 * @return the newly populated {@link Menupopup}
	 */
	protected Menupopup createMenuPopup(final WorkflowActionModel workflowActionModel,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final ConsignmentModel consignmentModel = (ConsignmentModel) workflowActionModel.getAttachmentItems().get(0);
		final Menupopup menuPopup = new Menupopup();
		menuPopup.setSclass(ROW_POPUP_STYLE);

		// Create menu options only if the consignment is delegated to an internal system
		if (consignmentModel.getFulfillmentSystemConfig() == null)
		{
			boolean captureOnConsignmentReallocationAllowed = true;
			if (getConfigurationService().getConfiguration().getBoolean(CAPTURE_PAYMENT_ON_CONSIGNMENT, Boolean.FALSE))
			{
				captureOnConsignmentReallocationAllowed = getReallocableConsignmentStatuses().contains(consignmentModel.getStatus());
			}

			// create the reallocate option only if consignment is a shipping consignment and reallocation is allowed
			if (consignmentModel.getDeliveryPointOfService() == null && captureOnConsignmentReallocationAllowed)
			{
				final Menuitem reallocateConsignment = new Menuitem();
				reallocateConsignment.setLabel(resolveLabel("warehousingbackoffice.taskassignment.consignment.reallocate"));
				reallocateConsignment.addEventListener(Events.ON_CLICK,
						event -> widgetInstanceManager.sendOutput(SOCKET_OUT_CONTEXT, consignmentModel));
				menuPopup.appendChild(reallocateConsignment);
			}

			final Menuitem printDocument = new Menuitem();
			printDocument.setLabel(resolveLabel("warehousingbackoffice.taskassignment.consignment.print"));
			printDocument.addEventListener(Events.ON_CLICK, event -> printDocument(workflowActionModel, consignmentModel));
			menuPopup.appendChild(printDocument);
		}
		else
		{
			final Menuitem emptyMenuItem = new Menuitem();
			emptyMenuItem.setLabel(resolveLabel("warehousingbackoffice.taskassignment.consignment.no.action"));
			menuPopup.appendChild(emptyMenuItem);
		}

		return menuPopup;
	}

	/**
	 * Brings up the popup menu.
	 *
	 * @param actionColumn
	 * 		the {@link Listcell} which was selected
	 * @param menuPopup
	 * 		the {@link Menupopup} that will be opened
	 */
	protected void onActionClick(final Listcell actionColumn, final Menupopup menuPopup)
	{
		menuPopup.open(actionColumn, MENU_POPUP_POSITION);
	}

	/**
	 * Gets the localized label for the given key.
	 *
	 * @param labelKey
	 * 		the key for which the label is required
	 * @return the localized label
	 */
	protected String resolveLabel(final String labelKey)
	{
		final String defaultValue = LabelUtils.getFallbackLabel(labelKey);
		return Labels.getLabel(labelKey, defaultValue);
	}

	/**
	 * Prints a document depending on the {@link WorkflowActionModel} that is to be performed
	 *
	 * @param workflowActionModel
	 * 		the {@link WorkflowActionModel} which will be used to know which document to print based on the task that is to be performed
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} whose status will be verified
	 */
	protected void printDocument(final WorkflowActionModel workflowActionModel, final ConsignmentModel consignmentModel)
	{
		if (workflowActionModel.getName().equals(PICK_ACTION))
		{
			getConsignmentPrintPickSlipStrategy().printDocument(consignmentModel);
		}
		else if (workflowActionModel.getName().equals(PACK_ACTION))
		{
			getConsignmentPrintPackSlipStrategy().printDocument(consignmentModel);
			printExportSlip(consignmentModel);

			printReturnFormAndLabel(consignmentModel);
		}
		else
		{
			getConsignmentPrintShippingLabelStrategy().printDocument(consignmentModel);
		}
	}

	/**
	 * Evaluates if {@link ConsignmentModel#RETURNFORM} and {@link ConsignmentModel#RETURNLABEL} needs to be printed.<br>
	 * And prints the document, if required
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} for which return form and return label needs to be printed
	 */
	protected void printReturnFormAndLabel(final ConsignmentModel consignmentModel)
	{
		final Configuration configuration = getConfigurationService().getConfiguration();
		boolean canPrintReturnForm = false;
		boolean canPrintReturnLabel = false;
		try
		{
			if (configuration != null)
			{
				canPrintReturnForm = configuration.getBoolean(CAN_PRINT_RETURNFORM_KEY);
				canPrintReturnLabel = configuration.getBoolean(CAN_PRINT_RETURNSHIPPINGLABEL_KEY);
			}
		}
		catch (final ConversionException | NoSuchElementException e)//NOSONAR
		{
			LOGGER.error(String.format(
					"No or incorrect property defined for [%s] or [%s]. Value has to be 'true' or 'false' - any other value will be treated as a false",
					CAN_PRINT_RETURNFORM_KEY, CAN_PRINT_RETURNSHIPPINGLABEL_KEY));//NOSONAR
		}

		if (canPrintReturnForm)
		{
			getConsignmentPrintReturnFormStrategy().printDocument(consignmentModel);
		}
		if (canPrintReturnLabel)
		{
			getConsignmentPrintReturnShippingLabelStrategy().printDocument(consignmentModel);
		}
	}

	/**
	 * Prints the export slip for a {@link ConsignmentModel} if possible.
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} for which the export slip will be printed
	 */
	protected void printExportSlip(final ConsignmentModel consignmentModel)
	{
		if (getPrintExportFormStrategy().canPrintExportForm(consignmentModel))
		{
			getConsignmentPrintExportFormStrategy().printDocument(consignmentModel);
		}
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintPickSlipStrategy()
	{
		return consignmentPrintPickSlipStrategy;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintPackSlipStrategy()
	{
		return consignmentPrintPackSlipStrategy;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintExportFormStrategy()
	{
		return consignmentPrintExportFormStrategy;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintShippingLabelStrategy()
	{
		return consignmentPrintShippingLabelStrategy;
	}

	protected PrintExportFormStrategy getPrintExportFormStrategy()
	{
		return printExportFormStrategy;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintReturnFormStrategy()
	{
		return consignmentPrintReturnFormStrategy;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintReturnShippingLabelStrategy()
	{
		return consignmentPrintReturnShippingLabelStrategy;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected List<ConsignmentStatus> getReallocableConsignmentStatuses()
	{
		return reallocableConsignmentStatuses;
	}
}

