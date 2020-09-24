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
package com.hybris.backoffice.workflow.impl;

import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowSearchData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.pageable.Pageable;


/**
 * Pageable object which allows to search workflows using {@link WorkflowSearchData}
 */
public class WorkflowSearchPageable implements Pageable<WorkflowModel>
{
	protected static final int TOTAL_COUNT_PAGE_SIZE = 1;
	protected static final int NOT_CALCULATED = -1;
	private int regularWorkflowsCount = NOT_CALCULATED;
	private int adHocWorkflowsCount = NOT_CALCULATED;
	private final Date dateFrom;
	private final Date dateTo;
	private final EnumSet<WorkflowStatus> statuses;
	private int currentPage = 0;
	private int pageSize;
	private List<WorkflowModel> currentPageResult;
	private WorkflowService workflowService;

	protected WorkflowSearchPageable(final WorkflowSearchData searchData)
	{
		this.pageSize = searchData.getPageSize();
		this.dateFrom = searchData.getDateFrom() != null ? new Date(searchData.getDateFrom().getTime()) : null;
		this.dateTo = searchData.getDateTo() != null ? new Date(searchData.getDateTo().getTime()) : null;
		this.statuses = EnumSet.copyOf((searchData.getStatuses()));
	}

	@Override
	public List<WorkflowModel> getCurrentPage()
	{
		if (currentPageResult == null)
		{
			currentPageResult = new ArrayList<>();
			currentPageResult.addAll(fetchRegularWorkflows());

			final int adHocWorkflowsToFetch = pageSize - currentPageResult.size();

			currentPageResult.addAll(fetchAdHocWorkflows(adHocWorkflowsToFetch));
		}
		return currentPageResult;
	}

	protected List<WorkflowModel> fetchRegularWorkflows()
	{
		final int currentStartIndex = getCurrentStartIndex();
		final int regularWorkflowsStartIndex = getRegularWorkflowsStartIndex(currentStartIndex);

		if (regularWorkflowsStartIndex != NOT_CALCULATED)
		{
			final SearchResult<WorkflowModel> allWorkflows = workflowService.getAllWorkflows(statuses, dateFrom, dateTo,
					regularWorkflowsStartIndex, pageSize);
			this.regularWorkflowsCount = allWorkflows.getTotalCount();
			if (currentStartIndex < regularWorkflowsCount)
			{
				return allWorkflows.getResult();
			}
		}
		return Collections.emptyList();
	}

	protected List<WorkflowModel> fetchAdHocWorkflows(final int adHocWorkflowsToFetch)
	{
		final int currentStartIndex = getCurrentStartIndex();
		if (adHocWorkflowsToFetch > 0)
		{
			final int adHocWorkflowsStartIndex = currentStartIndex > regularWorkflowsCount
					? (currentStartIndex - regularWorkflowsCount) : 0;

			final SearchResult<WorkflowModel> allWorkflows = workflowService.getAllAdhocWorkflows(statuses, dateFrom, dateTo,
					adHocWorkflowsStartIndex, adHocWorkflowsToFetch);
			this.adHocWorkflowsCount = allWorkflows.getTotalCount();
			return allWorkflows.getResult();
		}
		else if (adHocWorkflowsCount == NOT_CALCULATED)
		{
			final SearchResult<WorkflowModel> allWorkflows = workflowService.getAllAdhocWorkflows(statuses, dateFrom, dateTo, 0,
					TOTAL_COUNT_PAGE_SIZE);
			this.adHocWorkflowsCount = allWorkflows.getTotalCount();
		}
		return Collections.emptyList();
	}

	private int getRegularWorkflowsStartIndex(final int currentStartIndex)
	{
		if (regularWorkflowsCount == NOT_CALCULATED)
		{
			return currentStartIndex;
		}
		else
		{
			if (currentStartIndex < regularWorkflowsCount)
			{
				return currentStartIndex;
			}
			else
			{
				return NOT_CALCULATED;
			}
		}
	}


	private int getCurrentStartIndex()
	{
		return currentPage * pageSize;
	}

	@Override
	public void refresh()
	{
		currentPageResult = null;
		regularWorkflowsCount = NOT_CALCULATED;
		adHocWorkflowsCount = NOT_CALCULATED;
	}

	@Override
	public List<WorkflowModel> nextPage()
	{
		if (hasNextPage())
		{
			currentPage++;
			currentPageResult = null;
			return getCurrentPage();
		}
		return Collections.emptyList();
	}

	@Override
	public List<WorkflowModel> previousPage()
	{
		if (hasPreviousPage())
		{
			currentPage--;
			currentPageResult = null;
			return getCurrentPage();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean hasNextPage()
	{
		return currentPage < getNumberOfPages()-1;
	}

	@Override
	public boolean hasPreviousPage()
	{
		return currentPage > 0;
	}

	@Override
	public void setPageNumber(final int i)
	{
		if (i != currentPage)
		{
			currentPage = i;
			currentPageResult = null;
		}
	}

	private int getNumberOfPages()
	{
		final int totalCount = getTotalCount();
		int numberOfPages = totalCount / pageSize;
		if (totalCount - (pageSize * numberOfPages) > 0)
		{
			numberOfPages++;
		}
		return numberOfPages;
	}

	@Override
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public String getTypeCode()
	{
		return WorkflowModel._TYPECODE;
	}

	@Override
	public List<WorkflowModel> setPageSize(final int i)
	{
		this.pageSize = i;
		return getCurrentPage();
	}

	@Override
	public int getTotalCount()
	{
		if (regularWorkflowsCount == NOT_CALCULATED || adHocWorkflowsCount == NOT_CALCULATED)
		{
			getCurrentPage();
		}
		return regularWorkflowsCount + adHocWorkflowsCount;
	}

	@Override
	public int getPageNumber()
	{
		return currentPage;
	}

	@Override
	public SortData getSortData()
	{
		return new SortData();
	}

	@Override
	public void setSortData(final SortData sortData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<WorkflowModel> getAllResults()
	{
		final List<WorkflowModel> workflows = new ArrayList<>();
		workflows.addAll(workflowService.getAllWorkflows(statuses, dateFrom, dateTo));
		workflows.addAll(workflowService.getAllAdhocWorkflows(statuses, dateFrom, dateTo));
		return workflows;
	}

	@Required
	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}

	public WorkflowService getWorkflowService()
	{
		return workflowService;
	}
}
