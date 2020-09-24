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
package com.hybris.backoffice.widgets.workflowsearch;

import static com.hybris.cockpitng.components.Editor.ON_VALUE_CHANGED;

import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.backoffice.workflow.WorkflowSearchData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;
import com.hybris.cockpitng.testing.annotation.InextensibleMethod;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.Range;


/**
 * Widget controller class for Workflow Search Widget
 */
public class WorkflowSearchWidgetController extends DefaultWidgetController
{

	protected static final String SOCKET_INPUT_STATUSES = "statuses";
	protected static final String SOCKET_INPUT_REFRESH = "refresh";
	protected static final String SOCKET_OUTPUT_RESULTS = "result";

	protected static final String COMPONENT_SEARCH_BUTTON = "searchButton";
	protected static final String COMPONENT_DATE_RANGE_EDITOR = "rangeEditor";

	protected static final String MODEL_STATUSES = "statuses";
	protected static final String MODEL_DATE_RANGE = "dateRange";

	protected static final String SETTING_PAGE_SIZE = "pageSize";

	@Wire
	private Editor rangeEditor;

	@WireVariable
	private transient WorkflowFacade workflowFacade;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

		if (getValue(MODEL_STATUSES, Collection.class) == null)
		{
			setValue(MODEL_STATUSES, Arrays.asList(WorkflowStatus.RUNNING.name(), WorkflowStatus.PLANNED.name()));
		}
		getRangeEditor().setValue(getValue(MODEL_DATE_RANGE, Range.class));
	}

	@SocketEvent(socketId = SOCKET_INPUT_STATUSES)
	public void onStatusesChanged(final Collection<String> statuses)
	{
		setValue(MODEL_STATUSES, statuses != null ? new HashSet<>(statuses) : null);

		doSearch();
	}

	@ViewEvent(componentID = COMPONENT_SEARCH_BUTTON, eventName = Events.ON_CLICK)
	public void onSearchButtonClick(final Event event)
	{
		doSearch();
	}

	@ViewEvent(componentID = COMPONENT_DATE_RANGE_EDITOR, eventName = ON_VALUE_CHANGED)
	public void onDateRangeValueChanged(final Event event)
	{
		final Object possibleNewRange = event.getData();
		if (possibleNewRange instanceof Range)
		{
			setValue(MODEL_DATE_RANGE, possibleNewRange);
		}
	}

	@SocketEvent(socketId = SOCKET_INPUT_REFRESH)
	public void doSearch()
	{
		final int pageSize = getWidgetSettings().getInt(SETTING_PAGE_SIZE);
		final Collection<String> statuses = getValue(MODEL_STATUSES, Collection.class);

		if (CollectionUtils.isNotEmpty(statuses))
		{
			final Set<WorkflowStatus> workflowStatuses = statuses.stream().map(String::toUpperCase).map(WorkflowStatus::valueOf)
					.collect(Collectors.toCollection(() -> EnumSet.noneOf(WorkflowStatus.class)));
			final WorkflowSearchData searchData = new WorkflowSearchData(pageSize, workflowStatuses);
			includeDateFilters(searchData);
			sendSearchResults(workflowFacade.getWorkflows(searchData));
		}
		else
		{
			sendSearchResults(new PageableList<>(Collections.emptyList(), pageSize, WorkflowModel._TYPECODE));
		}
	}

	@InextensibleMethod
	private void includeDateFilters(final WorkflowSearchData searchData)
	{
		final Range<Date> dates = (Range<Date>) getRangeEditor().getValue();
		if (dates != null)
		{
			if (dates.getStart() != null)
			{
				searchData.setDateFrom(floor(dates.getStart()));
			}
			if (dates.getEnd() != null)
			{
				searchData.setDateTo(ceil(dates.getEnd()));
			}
		}
	}

	@InextensibleMethod
	private static Date floor(final Date date)
	{
		final Calendar calendar = Calendar.getInstance(Locales.getCurrent());
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	@InextensibleMethod
	private static Date ceil(final Date date)
	{
		final Calendar calendar = Calendar.getInstance(Locales.getCurrent());
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	protected void sendSearchResults(final Pageable<WorkflowModel> results)
	{
		sendOutput(SOCKET_OUTPUT_RESULTS, results);
	}

	protected Editor getRangeEditor()
	{
		return rangeEditor;
	}

	protected WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}
}
