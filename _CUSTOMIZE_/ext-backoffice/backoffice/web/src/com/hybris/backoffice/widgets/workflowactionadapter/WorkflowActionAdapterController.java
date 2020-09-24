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
package com.hybris.backoffice.widgets.workflowactionadapter;

import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.workflow.model.AbstractWorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class WorkflowActionAdapterController extends AbstractInitAdvancedSearchAdapter
{

	protected static final String BACKOFFICE_WFL_INBOX_ID = "hmc_backoffice-workflow-inbox";
	/**
	 * @deprecated since 6.5.0.0, use {@link WorkflowActionModel#_TYPECODE} instead
	 */
	@Deprecated
	protected static final String WORKFLOW_ACTION_TYPE_CODE = WorkflowActionModel._TYPECODE;
	protected static final String WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR = AbstractWorkflowActionModel.PRINCIPALASSIGNED;
	protected static final String SOCKET_INPUT_STATUSES = "statuses";
	protected static final String SOCKET_OUTPUT_CONTEXT = "context";
	protected static final String MODEL_SELECTED_STATUSES = "selectedStatuses";
	protected static final String MODEL_NAVIGATION_NODE = "navigationNode";

	protected transient CockpitUserService cockpitUserService;
	protected transient UserService userService;


	@Override
	@SocketEvent(socketId = SOCKET_IN_NODE_SELECTED)
	public void createAdvancedSearchInitContext(final NavigationNode navigationNode)
	{
		setValue(MODEL_NAVIGATION_NODE, navigationNode);
		super.createAdvancedSearchInitContext(navigationNode);
	}

	@SocketEvent(socketId = SOCKET_INPUT_STATUSES)
	public void setSelectedStatuses(final Set<Object> statuses)
	{
		setValue(MODEL_SELECTED_STATUSES, statuses);
		final NavigationNode navigationNode = getValue(MODEL_NAVIGATION_NODE, NavigationNode.class);
		if (navigationNode != null)
		{
			super.createAdvancedSearchInitContext(navigationNode);
		}
	}

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> optional)
	{
		addUserCondition(searchData);
		addStatusCondition(searchData);
	}

	protected void addUserCondition(final AdvancedSearchData searchData)
	{
		clearConditionsForAttribute(searchData, WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR);
		final UserModel currentUser = getCurrentUser();
		final FieldType fieldType = createFieldType(WORKFLOW_ACTION_PRINCIPAL_ASSIGNED_ATTR);

		final List<SearchConditionData> conditions = new ArrayList<>();
		final SearchConditionData currentUserCondition = new SearchConditionData(fieldType, currentUser,
				ValueComparisonOperator.EQUALS);
		conditions.add(currentUserCondition);

		final Set<UserGroupModel> groups = getUserService().getAllUserGroupsForUser(currentUser);
		for (final UserGroupModel currentGroup : groups)
		{
			final SearchConditionData userGroupCondition = new SearchConditionData(fieldType, currentGroup,
					ValueComparisonOperator.EQUALS);
			conditions.add(userGroupCondition);
		}
		searchData.addFilterQueryRawConditionsList(ValueComparisonOperator.OR, conditions);
	}

	protected void clearConditionsForAttribute(final AdvancedSearchData searchData, final String attribute)
	{
		final List<SearchConditionData> conditions = searchData.getConditions(attribute);
		if (CollectionUtils.isNotEmpty(conditions))
		{
			conditions.clear();
		}
	}

	protected FieldType createFieldType(final String fieldName)
	{
		final FieldType fieldType = new FieldType();
		fieldType.setDisabled(Boolean.TRUE);
		fieldType.setSelected(Boolean.TRUE);
		fieldType.setName(fieldName);
		return fieldType;
	}

	protected void addStatusCondition(final AdvancedSearchData searchData)
	{
		clearConditionsForAttribute(searchData, WorkflowActionModel.STATUS);

		final Set<?> statuses = getValue(MODEL_SELECTED_STATUSES, Set.class);
		if (CollectionUtils.isNotEmpty(statuses))
		{
			final FieldType fieldType = createFieldType(WorkflowActionModel.STATUS);
			final List<SearchConditionData> conditions = new ArrayList<>();
			for (final Object status : statuses)
			{
				conditions.add(new SearchConditionData(fieldType, status, ValueComparisonOperator.EQUALS));
			}
			searchData.addFilterQueryRawConditionsList(ValueComparisonOperator.OR, conditions);
		}
	}

	protected UserModel getCurrentUser()
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
		return WorkflowActionModel._TYPECODE;
	}

	@Override
	public String getNavigationNodeId()
	{
		return BACKOFFICE_WFL_INBOX_ID;
	}

	@Override
	protected String getOutputSocketName()
	{
		return SOCKET_OUTPUT_CONTEXT;
	}

	protected CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	@WireVariable
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@WireVariable
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

}
