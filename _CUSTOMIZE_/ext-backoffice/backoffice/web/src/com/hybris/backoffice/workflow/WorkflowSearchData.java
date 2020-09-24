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

import de.hybris.platform.workflow.WorkflowStatus;

import java.util.Collection;
import java.util.Date;


public class WorkflowSearchData
{
	private int pageSize;
	private Collection<WorkflowStatus> statuses;
	private Date dateFrom;
	private Date dateTo;

	public WorkflowSearchData(final int pageSize, final Collection<WorkflowStatus> statuses)
	{
		this(pageSize, statuses, null, null);
	}

	public WorkflowSearchData(final int pageSize, final Collection<WorkflowStatus> statuses, final Date dateFrom,
			final Date dateTo)
	{
		this.pageSize = pageSize;
		this.statuses = statuses;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(final int pageSize)
	{
		this.pageSize = pageSize;
	}

	public Collection<WorkflowStatus> getStatuses()
	{
		return statuses;
	}

	public void setStatuses(final Collection<WorkflowStatus> statuses)
	{
		this.statuses = statuses;
	}

	public Date getDateFrom()
	{
		return dateFrom;
	}

	public void setDateFrom(final Date dateFrom)
	{
		this.dateFrom = dateFrom;
	}

	public Date getDateTo()
	{
		return dateTo;
	}

	public void setDateTo(final Date dateTo)
	{
		this.dateTo = dateTo;
	}

}
