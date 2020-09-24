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

import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menupopup;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;

public class WorkflowActionDecisionListSubMenuRenderer
		extends AbstractWidgetComponentRenderer<Menupopup, ListColumn, WorkflowActionModel>
{

	private static final String LABEL_SET_DECISION = "workflow.set.decision";
	protected static final String NO_ICON = " ";

	private WorkflowActionDecisionMenuitemRenderer workflowActionDecisionMenuitemRenderer;
	private Predicate<WorkflowActionModel> predicate;

	@Override
	public void render(final Menupopup menupopup, final ListColumn configuration, final WorkflowActionModel workflowAction,
			final DataType type, final WidgetInstanceManager wim)
	{
		if (predicate.negate().test(workflowAction))
		{
			return;
		}

		final Menu menu = createMenu();
		final Menupopup subMenuPopup = new Menupopup();
		final ProxyRenderer<Menupopup, ListColumn, WorkflowActionModel> proxyRenderer = new ProxyRenderer<>(this, menupopup,
				configuration, workflowAction);

		workflowAction.getDecisions().forEach(decision -> {
			final Pair<WorkflowActionModel, WorkflowDecisionModel> data = ImmutablePair.of(workflowAction, decision);
			proxyRenderer.render(getWorkflowActionDecisionMenuitemRenderer(), subMenuPopup, null, data, type, wim);
		});

		if (CollectionUtils.isNotEmpty(subMenuPopup.getChildren()))
		{
			menu.appendChild(subMenuPopup);
			menupopup.appendChild(menu);

			fireComponentRendered(menu, menupopup, configuration, workflowAction);
		}
		fireComponentRendered(menupopup, configuration, workflowAction);
	}

	private static Menu createMenu() {
		final Menu menu = new Menu(Labels.getLabel(LABEL_SET_DECISION));
		menu.setIconSclass(NO_ICON);
		return menu;
	}

	protected WorkflowActionDecisionMenuitemRenderer getWorkflowActionDecisionMenuitemRenderer()
	{
		return workflowActionDecisionMenuitemRenderer;
	}

	@Required
	public void setWorkflowActionDecisionMenuitemRenderer(
			final WorkflowActionDecisionMenuitemRenderer workflowDecisionMenuitemRenderer)
	{
		this.workflowActionDecisionMenuitemRenderer = workflowDecisionMenuitemRenderer;
	}

	public Predicate<WorkflowActionModel> getPredicate()
	{
		return predicate;
	}

	@Required
	public void setPredicate(final Predicate<WorkflowActionModel> predicate)
	{
		this.predicate = predicate;
	}
}
