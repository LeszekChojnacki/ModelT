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

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class WorkflowActionDecisionMenuitemRenderer
		extends AbstractWorkflowActionDecisionRenderer<Menupopup, Void, Pair<WorkflowActionModel, WorkflowDecisionModel>>
{

	protected static final String SCLASS_DECISION_MENUPOPUP = "yw-workflows-menu-popup yw-pointer-menupopup yw-pointer-menupopup-secondary";
	protected static final String NO_ICON = " ";

	private PermissionFacade permissionFacade;

	@Override
	public void render(final Menupopup parent, final Void configuration,
			final Pair<WorkflowActionModel, WorkflowDecisionModel> data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final WorkflowDecisionModel workflowDecision = data.getValue();

		if (hasPermissions(data))
		{
			final Menuitem decisionMenuitem = createDecisionMenuitem(workflowDecision);
			decisionMenuitem.setIconSclass(NO_ICON);
			parent.appendChild(decisionMenuitem);

			final WorkflowActionModel workflowAction = data.getKey();
			decisionMenuitem.addEventListener(Events.ON_CLICK,
					createDecisionOnClickEventListener(workflowAction, workflowDecision, widgetInstanceManager));
			parent.setSclass(SCLASS_DECISION_MENUPOPUP);
			fireComponentRendered(decisionMenuitem, parent, configuration, data);
		}
		fireComponentRendered(parent, configuration, data);
	}

	protected boolean hasPermissions(final Pair<WorkflowActionModel, WorkflowDecisionModel> data)
	{
		return hasPermissions(data.getLeft()) && hasPermissions(data.getRight());
	}

	protected boolean hasPermissions(final WorkflowActionModel action)
	{
		return getPermissionFacade().canReadType(WorkflowActionModel._TYPECODE) && getPermissionFacade().canReadInstance(action)
				&& getPermissionFacade().canChangeInstance(action);
	}

	protected boolean hasPermissions(final WorkflowDecisionModel decision)
	{
		return getPermissionFacade().canReadType(WorkflowDecisionModel._TYPECODE)
				&& getPermissionFacade().canReadInstance(decision);
	}

	protected Menuitem createDecisionMenuitem(final WorkflowDecisionModel workflowDecision)
	{
		final Menuitem decisionMenuitem = new Menuitem(getDecisionLabel(workflowDecision));
		return decisionMenuitem;
	}

	protected EventListener<Event> createDecisionOnClickEventListener(final WorkflowActionModel workflowAction,
			final WorkflowDecisionModel workflowDecision, final WidgetInstanceManager widgetInstanceManager)
	{
		return event -> makeDecision(workflowAction, workflowDecision, widgetInstanceManager);
	}

	protected PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}
}
