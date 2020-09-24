/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.backoffice.workflow.renderer;

import de.hybris.platform.workflow.model.WorkflowModel;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class WorkflowShowFlowActionRenderer implements WidgetComponentRenderer<Menupopup, ListColumn, WorkflowModel>
{
	protected static final String NO_ICON = " ";
	private static final String LABEL_WORKFLOWS_ACTION_SHOW_FLOW = "workflow.action.showflow";
	private static final String SOCKET_OUTPUT_SHOW_FLOW = "showFlow";

	@Override
	public void render(final Menupopup menupopup, final ListColumn listColumn, final WorkflowModel workflowModel,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final Menuitem menuitem = new Menuitem();
		menuitem.setIconSclass(NO_ICON);
		menuitem.setLabel((Labels.getLabel(LABEL_WORKFLOWS_ACTION_SHOW_FLOW)));
		menuitem.addEventListener(Events.ON_CLICK, e -> widgetInstanceManager.sendOutput(SOCKET_OUTPUT_SHOW_FLOW, workflowModel));
		menupopup.appendChild(menuitem);
	}
}
