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

import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.enums.WorkflowActionType;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;

import com.hybris.cockpitng.components.visjs.network.data.Node;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.labels.LabelService;


class WorkflowItemFromWorkflowActionModel extends WorkflowItem
{
	private static final String ASSIGNED_USER_LABEL = "workflow.visualization.details.assigneduser";
	private static final String LAST_UPDATE_LABEL = "workflow.visualization.details.lastupdate";

	private static final String PREFIX_ACTION_STATUS = "action_";
	private static final char UNDERSCORE_CHAR = '_';
	private static final String DETAILS_ENTRY_DELIMITER = "<br>";
	private static final String DETAILS_ENTRY_FORMAT = "<strong>%s</strong>: %s";

	private static final List<WorkflowActionStatus> STATUSES_TO_BE_HIGHLIGHTED = Arrays.asList( //
			WorkflowActionStatus.PENDING, //
			WorkflowActionStatus.IN_PROGRESS, //
			WorkflowActionStatus.COMPLETED, //
			WorkflowActionStatus.TERMINATED //
	);

	private final WorkflowActionModel action;
	private final CockpitLocaleService localeService;
	private final LabelService labelService;

	public WorkflowItemFromWorkflowActionModel(final WorkflowActionModel action, final CockpitLocaleService localeService,
			final LabelService labelService)
	{
		super(String.valueOf(action.getPk()), Type.ACTION, WorkflowActionType.END == action.getActionType());
		this.action = action;
		this.localeService = localeService;
		this.labelService = labelService;
	}

	private static List<String> collectPKs(final Collection<? extends AbstractItemModel> items)
	{
		return items.stream() //
				.map(AbstractItemModel::getPk) //
				.map(String::valueOf) //
				.collect(Collectors.toList());
	}

	private static String convertSnakeCaseToCamelCase(final String value)
	{
		final String camelCaseValue = WordUtils.capitalizeFully(value.toLowerCase(), new char[]
		{ UNDERSCORE_CHAR }).replaceAll(String.valueOf(UNDERSCORE_CHAR), StringUtils.EMPTY);
		return WordUtils.uncapitalize(camelCaseValue);
	}

	@Override
	public Node createNode()
	{
		return new Node.Builder() //
				.withId(getId()) //
				.withLabel(action.getName(localeService.getCurrentLocale())) //
				.withLevel(getLevel()) //
				.withGroup(getGroupName(action.getStatus())) //
				.withTitle(createDetailsMessage(action)).build();
	}

	protected String createDetailsMessage(final WorkflowActionModel action)
	{
		final Map<String, Object> detailsMap = new HashMap<>();
		detailsMap.put(ASSIGNED_USER_LABEL, action.getPrincipalAssigned());
		detailsMap.put(LAST_UPDATE_LABEL, action.getActivated());

		return detailsMap.entrySet().stream() //
				.filter(entry -> Objects.nonNull(entry.getValue())) //
				.map(this::createDetailsEntry) //
				.collect(Collectors.joining(DETAILS_ENTRY_DELIMITER));
	}

	protected String createDetailsEntry(final Map.Entry<String, Object> entry)
	{
		return String.format(DETAILS_ENTRY_FORMAT, Labels.getLabel(entry.getKey()), labelService.getObjectLabel(entry.getValue()));
	}

	@Override
	public Collection<String> getNeighborsIds()
	{
		final List<String> andConnections = collectPKs(action.getIncomingLinks().stream() //
				.filter(WorkflowItemModelFactory::isAndConnection) //
				.collect(Collectors.toList()));
		if (!andConnections.isEmpty())
		{
			return andConnections;
		}
		return collectPKs(action.getIncomingDecisions());
	}

	/**
	 * Gets group name as a String basing on the WorkflowActionStatus
	 *
	 * @param status
	 *           WorkflowActionStatus
	 * @return group name as a WorkflowActionStatus if it should be highlighted or as an Action if not
	 */
	String getGroupName(final WorkflowActionStatus status)
	{
		if (STATUSES_TO_BE_HIGHLIGHTED.contains(status))
		{
			final String groupNameInSnakeCase = PREFIX_ACTION_STATUS.concat(status.toString());
			return convertSnakeCaseToCamelCase(groupNameInSnakeCase);
		}
		return WorkflowItem.Type.ACTION.toString().toLowerCase();
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
