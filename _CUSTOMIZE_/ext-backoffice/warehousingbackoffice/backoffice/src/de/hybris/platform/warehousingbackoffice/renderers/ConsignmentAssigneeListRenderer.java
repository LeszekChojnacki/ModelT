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

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import javax.annotation.Resource;

import java.util.Optional;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;


/**
 * Displays the employee assigned to a given consignment in the All Consignments list view.
 */
public class ConsignmentAssigneeListRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	protected static final String SCLASS_YW_LISTVIEW_CELL_LABEL = "yw-listview-cell-label";
	@Resource
	protected WorkflowService newestWorkflowService;

	@Override
	public void render(final Listcell listcell, final ListColumn configuration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (object instanceof ConsignmentModel && ((ConsignmentModel) object).getTaskAssignmentWorkflow() != null)
		{
			final WorkflowModel taskAssignmentWorkflow = getNewestWorkflowService()
					.getWorkflowForCode(((ConsignmentModel) object).getTaskAssignmentWorkflow());

			final Optional<WorkflowActionModel> taskInProgress = taskAssignmentWorkflow.getActions().stream()
					.filter(action -> action.getStatus().equals(WorkflowActionStatus.IN_PROGRESS)).findAny();
			if (taskInProgress.isPresent())
			{
				final Label assigneeLabel = new Label(taskInProgress.get().getPrincipalAssigned().getDisplayName());
				assigneeLabel.setSclass(SCLASS_YW_LISTVIEW_CELL_LABEL);
				listcell.appendChild(assigneeLabel);
			}
		}

	}

	protected WorkflowService getNewestWorkflowService()
	{
		return newestWorkflowService;
	}
}
