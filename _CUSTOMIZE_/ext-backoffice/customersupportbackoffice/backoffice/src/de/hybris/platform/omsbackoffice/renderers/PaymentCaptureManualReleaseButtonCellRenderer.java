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
package de.hybris.platform.omsbackoffice.renderers;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.util.localization.Localization;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;


/**
 * This cell renderer displays a button to release a {@link ConsignmentModel} from a waiting step following a failed partial capture.
 */
public class PaymentCaptureManualReleaseButtonCellRenderer extends ReleaseButtonCellRenderer
		implements WidgetComponentRenderer<Listcell, ListColumn, ConsignmentModel>
{
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String HANDLE_MANUAL_PAYMENT_CAPTURE_CHOICE = "handleManualCapture";
	protected static final String RELEASE_BUTTON = "releasebutton";
	protected static final String DISABLED = "disabled";

	private WidgetInstanceManager widgetInstanceManager;

	@Override
	public void render(final Listcell parent, final ListColumn listColumn, final ConsignmentModel consignmentModel,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		setWidgetInstanceManager(widgetInstanceManager);

		final Button releaseButton = new Button();
		String releaseButtonClass = RELEASE_BUTTON;

		releaseButton.setParent(parent);
		releaseButton.addEventListener(Events.ON_CLICK, event -> executeManualRelease(consignmentModel));
		releaseButton.setTooltiptext(
				Localization.getLocalizedString("customersupportbackoffice.tooltip.consignment.paymentcapturemanualrelease"));

		if (!canPerformOperation(consignmentModel, ConsignmentStatus.PAYMENT_NOT_CAPTURED))
		{
			releaseButton.setDisabled(true);
			releaseButtonClass = releaseButtonClass + " " + DISABLED;
		}

		releaseButton.setSclass(releaseButtonClass);
	}

	/**
	 * Performs the manual payment capture release if the {@link ConsignmentModel} can be released
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} to check and release
	 */
	protected void executeManualRelease(final ConsignmentModel consignmentModel)
	{
		if (canPerformOperation(consignmentModel, ConsignmentStatus.PAYMENT_NOT_CAPTURED))
		{
			triggerBusinessProcessEvent(consignmentModel, CONSIGNMENT_ACTION_EVENT_NAME, HANDLE_MANUAL_PAYMENT_CAPTURE_CHOICE);
		}
	}

	protected WidgetInstanceManager getWidgetInstanceManager()
	{
		return widgetInstanceManager;
	}

	public void setWidgetInstanceManager(final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;
	}
}
