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
package de.hybris.platform.warehousingbackoffice.actions.taskassignment;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;

import javax.annotation.Resource;

import java.util.List;
import java.util.Optional;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Filters the current {@link de.hybris.platform.workflow.model.WorkflowActionModel} for the Task Assignment and assigns it to the user.
 */
public class WarehousingTaskAssignmentWorkflowActionController extends AbstractInitAdvancedSearchAdapter
{
	private static final String BACKOFFICE_WFL_INBOX_ID = "warehousing.treenode.taskassignment.inbox";
	private static final String WORKFLOW_TYPE_CODE = "WorkflowAction";
	private static final String SOCKET_CONTEXT = "context";
	private static final String WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR = "principalAssigned";
	private static final String WORKFLOW_ACTION_STATUS_ATTR = "status";

	@Resource
	private transient CockpitUserService cockpitUserService;
	@Resource
	private transient UserService userService;

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		final FieldType fieldType = new FieldType();
		fieldType.setDisabled(true);
		fieldType.setSelected(true);
		fieldType.setName(WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR);
		final UserModel currentUser = getCurrentUser();

		List<SearchConditionData> conditions = searchData.getConditions(WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR);
		if (CollectionUtils.isNotEmpty(conditions))
		{
			conditions.clear();
		}
		searchData.addCondition(fieldType, ValueComparisonOperator.EQUALS, currentUser);

		conditions = searchData.getConditions(WORKFLOW_ACTION_STATUS_ATTR);
		if (CollectionUtils.isNotEmpty(conditions))
		{
			conditions.clear();
		}
		final FieldType statusType = new FieldType();
		statusType.setDisabled(true);
		statusType.setSelected(true);
		statusType.setName(WORKFLOW_ACTION_STATUS_ATTR);

		searchData.addCondition(statusType, ValueComparisonOperator.EQUALS, WorkflowActionStatus.IN_PROGRESS);
	}

	/**
	 * Gets the current {@link UserModel} that is logged in.
	 *
	 * @return the {@link UserModel}
	 */
	private UserModel getCurrentUser()
	{
		UserModel userModel = null;
		final String userId = getCockpitUserService().getCurrentUser();
		if (StringUtils.isNotBlank(userId))
		{
			userModel = getUserService().getUserForUID(userId);
		}
		return userModel;
	}

	@Override
	public String getTypeCode()
	{
		return WORKFLOW_TYPE_CODE;
	}

	@Override
	public String getNavigationNodeId()
	{
		return BACKOFFICE_WFL_INBOX_ID;
	}

	@Override
	protected String getOutputSocketName()
	{
		return SOCKET_CONTEXT;
	}

	protected CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	protected UserService getUserService()
	{
		return userService;
	}
}
