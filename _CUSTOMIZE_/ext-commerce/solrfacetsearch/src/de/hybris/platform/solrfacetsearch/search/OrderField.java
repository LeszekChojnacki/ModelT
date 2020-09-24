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
package de.hybris.platform.solrfacetsearch.search;

import java.io.Serializable;


public class OrderField implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum SortOrder
	{
		ASCENDING, DESCENDING
	}

	public static final String SCORE = "score";

	private String field;
	private SortOrder sortOrder;

	public OrderField(final String field)
	{
		this(field, SortOrder.ASCENDING);
	}

	public OrderField(final String field, final SortOrder sortOrder)
	{
		this.field = field;
		this.sortOrder = sortOrder;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	/**
	 * @return the sortOrder
	 */
	public SortOrder getSortOrder()
	{
		return sortOrder;
	}

	/**
	 * @param sortOrder
	 *           the sortOrder to set
	 */
	public void setSortOrder(final SortOrder sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public boolean isAscending()
	{
		return sortOrder == SortOrder.ASCENDING;
	}

	public void setAscending(final boolean ascending)
	{
		this.sortOrder = ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING;
	}

	@Override
	public String toString()
	{
		return getClass().getName() + " [" + field + " (ascending = " + sortOrder + ")]";
	}
}
