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
package com.hybris.backoffice.workflow;


import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.ListModelList;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.actions.AbstractStatefulActionRenderer;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionListener;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.components.Action;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.WidgetUtils;


public class WorkflowActionDecisionActionRenderer
		extends AbstractStatefulActionRenderer<WorkflowActionModel, WorkflowDecisionModel>
{

	private static final String CONFIRM_LABEL = "workflowactiondecisionaction.confirm";
	private static final String CHOOSE_ACTION_LABEL = "workflowactiondecisionaction.chooseaction";
	private static final String SCLASS_WORKFLOW_ACTION_SELECTED_DECISION = "yw-workflowaction-selected-decision";
	private static final String SCLASS_WORKFLOW_ACTION_DECISION_CONTAINER = "yw-workflowaction-decision-container";

	private LabelService labelService;
	private ObjectFacade objectFacade;
	private WidgetUtils widgetUtils;
	private PermissionFacade permissionFacade;


	@Override
	public void render(final Component parent, final CockpitAction<WorkflowActionModel, WorkflowDecisionModel> action,
			final ActionContext<WorkflowActionModel> context, final boolean updateMode,
			final ActionListener<WorkflowDecisionModel> listener)
	{
		if (!shouldRenderAction(context))
		{
			return;
		}

		final WidgetInstanceManager wim = ((Action) parent).getWidgetInstanceManager();
		context.setParameter("wim", wim);

		final WorkflowActionModel workflowAction = context.getData();

		final Button confirmDecisionButton = createConfirmDecisionButton(context, action, listener);
		final Combobox decisionCombobox = createDecisionCombobox(context, confirmDecisionButton);

		final Hlayout container = new Hlayout();
		container.appendChild(decisionCombobox);
		container.appendChild(confirmDecisionButton);
		container.setSclass(SCLASS_WORKFLOW_ACTION_DECISION_CONTAINER);
		parent.appendChild(container);
		registerWorkflowActionUpdateListener(workflowAction, container, parent);
	}

	protected boolean shouldRenderAction(final ActionContext<WorkflowActionModel> context)
	{
		if (context.getData() == null)
		{
			return false;
		}

		final WorkflowActionModel workflowAction = context.getData();

		return workflowAction.getStatus() != null && workflowAction.getStatus() == WorkflowActionStatus.IN_PROGRESS
				&& hasPermissionToMakeDecision(workflowAction);
	}

	protected boolean hasPermissionToMakeDecision(final WorkflowActionModel action)
	{
		return hasPermissions(action) && !getPermittedDecisions(action).isEmpty();
	}

	protected boolean hasPermissions(final WorkflowActionModel action)
	{
		return getPermissionFacade().canReadType(WorkflowActionModel._TYPECODE) && getPermissionFacade().canReadInstance(action)
				&& getPermissionFacade().canChangeType(WorkflowActionModel._TYPECODE)
				&& getPermissionFacade().canChangeInstance(action);
	}

	protected boolean hasPermissions(final WorkflowDecisionModel decision)
	{
		return getPermissionFacade().canReadType(WorkflowDecisionModel._TYPECODE)
				&& getPermissionFacade().canReadInstance(decision);
	}

	protected List<WorkflowDecisionModel> getPermittedDecisions(final WorkflowActionModel action)
	{
		return action.getDecisions().stream().filter(this::hasPermissions).collect(Collectors.toList());
	}

	protected Button createConfirmDecisionButton(final ActionContext<WorkflowActionModel> context,
			final CockpitAction<WorkflowActionModel, WorkflowDecisionModel> action,
			final ActionListener<WorkflowDecisionModel> listener)
	{
		final Button confirmDecisionButton = new Button(context.getLabel(CONFIRM_LABEL));

		confirmDecisionButton.addEventListener(Events.ON_CLICK, event -> {
			confirmDecisionButton.setDisabled(true);
			perform(action, context, wrapActionListener(listener, confirmDecisionButton));
		});

		return confirmDecisionButton;
	}

	protected ActionListener<WorkflowDecisionModel> wrapActionListener(final ActionListener<WorkflowDecisionModel> listener,
			final Button confirmButton)
	{
		return result -> {
			if (ActionResult.ERROR.equals(result.getResultCode()))
			{
				confirmButton.setDisabled(false);
			}
			listener.actionPerformed(result);
		};
	}

	protected Combobox createDecisionCombobox(final ActionContext<WorkflowActionModel> context, final Button confirmButton)
	{
		final Combobox combobox = new Combobox();
		combobox.setItemRenderer((comboitem, decision, index) -> renderSingleComboitem(comboitem, decision));
		combobox.setSclass(SCLASS_WORKFLOW_ACTION_SELECTED_DECISION);
		combobox.setReadonly(true);
		combobox.setPlaceholder(context.getLabel(CHOOSE_ACTION_LABEL));

		final WorkflowActionModel workflowAction = context.getData();
		final ListModelList<WorkflowDecisionModel> model = new ListModelList<>(
				Lists.newArrayList(getPermittedDecisions(workflowAction)));
		combobox.setModel(model);

		setInitialComboboxValue(combobox, context);

		combobox.addEventListener(Events.ON_CHANGE, event -> {
			final Object selectedValue = combobox.getSelectedItem().getValue();
			setValue(context, WorkflowActionDecisionAction.PARAMETER_SELECTED_DECISION, selectedValue);

			confirmButton.setDisabled(shouldDisableConfirmDecisionButton(combobox, workflowAction.getStatus()));
		});

		confirmButton.setDisabled(shouldDisableConfirmDecisionButton(combobox, workflowAction.getStatus()));

		return combobox;
	}

	protected boolean shouldDisableConfirmDecisionButton(final Combobox combobox, final WorkflowActionStatus workflowActionStatus)
	{
		return combobox.getSelectedItem() == null || workflowActionStatus != WorkflowActionStatus.IN_PROGRESS;
	}

	protected void renderSingleComboitem(final Comboitem comboitem, final Object decision)
	{
		comboitem.setLabel(getLabelService().getObjectLabel(decision));
		comboitem.setValue(decision);
	}

	protected void setInitialComboboxValue(final Combobox combobox, final ActionContext<WorkflowActionModel> context)
	{
		final Optional<Comboitem> comboitemToSelect = combobox.getItems().stream()
				.filter(p -> p.getValue().equals(getValue(context, WorkflowActionDecisionAction.PARAMETER_SELECTED_DECISION)))
				.findFirst();

		if (comboitemToSelect.isPresent())
		{
			combobox.setSelectedItem(comboitemToSelect.get());
		}
		else
		{
			combobox.setSelectedItem(null);
		}
	}

	protected void registerWorkflowActionUpdateListener(final WorkflowActionModel workflowAction, final Hlayout container,
			final Component parent)
	{
		final WidgetInstanceManager widgetInstanceManager = ((Action) parent).getWidgetInstanceManager();
		getWidgetUtils().addGlobalEventListener(ObjectFacade.OBJECTS_UPDATED_EVENT, widgetInstanceManager.getWidgetslot(),
				event -> {
					final CockpitEvent data = (CockpitEvent) event.getData();
					final boolean actionMatches = data.getDataAsCollection().stream()
							.anyMatch(action -> action.equals(workflowAction));
					if (actionMatches)
					{
						final WorkflowActionModel updatedWorkflowAction = getObjectFacade().reload(workflowAction);
						widgetInstanceManager.getModel().setValue("$_selectedAction", updatedWorkflowAction);
						if (updatedWorkflowAction.getStatus() != WorkflowActionStatus.IN_PROGRESS)
						{
							parent.removeChild(container);
						}
					}
				}, CockpitEvent.SESSION);
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	@Autowired
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	protected ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Autowired
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	protected WidgetUtils getWidgetUtils()
	{
		return widgetUtils;
	}

	@Autowired
	public void setWidgetUtils(final WidgetUtils widgetUtils)
	{
		this.widgetUtils = widgetUtils;
	}

	protected PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Autowired
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

}
