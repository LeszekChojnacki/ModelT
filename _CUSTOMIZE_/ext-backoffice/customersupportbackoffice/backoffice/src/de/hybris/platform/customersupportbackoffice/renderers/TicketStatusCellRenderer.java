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
package de.hybris.platform.customersupportbackoffice.renderers;

import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.model.CsTicketModel;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class TicketStatusCellRenderer implements WidgetComponentRenderer<Listcell, ListColumn, CsTicketModel>
{
	private static final String TICKET_STATE_OPEN = "customersupport_backoffice_tickets_state_open";
	private static final String TICKET_STATE_PREFIX = "customersupport_backoffice_tickets_state_";


	@Override
	public void render(final Listcell parent, final ListColumn configuration, final CsTicketModel ticket, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		Label stateLabel = null;

		if (ticket.getState().equals(CsTicketState.OPEN))
		{
			stateLabel = new Label(Labels.getLabel(TICKET_STATE_OPEN));
		}
		else
		{
			String state = Labels.getLabel(TICKET_STATE_PREFIX + ticket.getState().getCode().toLowerCase());
			if (StringUtils.isEmpty(state))
			{
				state = ticket.getState().getCode();
			}
			stateLabel = new Label(state);
		}

		stateLabel.setVisible(true);
		stateLabel.setParent(parent);
	}

}
