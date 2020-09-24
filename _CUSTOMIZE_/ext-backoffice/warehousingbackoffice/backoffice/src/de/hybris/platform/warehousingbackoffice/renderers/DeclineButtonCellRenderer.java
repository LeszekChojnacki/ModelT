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

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;

import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * This cell renderer displays a decline entry button
 */
public class DeclineButtonCellRenderer implements WidgetComponentRenderer<Listcell, ListColumn, ConsignmentEntryModel>
{
	protected static final String SOCKET_OUT_CONTEXT = "consignmentEntryContext";


	@Override
	public void render(final Listcell parent, final ListColumn columnConfiguration, final ConsignmentEntryModel object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManger)
	{
		final Button button = new Button();
		button.setSclass("declineitem");
		button.setParent(parent);

		if (object.getConsignment().getDeliveryMode() instanceof PickUpDeliveryModeModel || object.getQuantityPending() == 0)
		{
			button.setDisabled(true);
		}

		button.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>()
		{
			@Override
			public void onEvent(final MouseEvent event) throws Exception
			{
				widgetInstanceManger.sendOutput(SOCKET_OUT_CONTEXT, object);
			}
		});
	}

}
