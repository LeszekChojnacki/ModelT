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


public class GroupCommandField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private Integer groupLimit;

	public GroupCommandField(final String field)
	{
		this.field = field;
	}

	public GroupCommandField(final String field, final Integer groupLimit)
	{
		this.field = field;
		this.groupLimit = groupLimit;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	public Integer getGroupLimit()
	{
		return groupLimit;
	}

	public void setGroupLimit(final Integer groupLimit)
	{
		this.groupLimit = groupLimit;
	}
}
