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


public class FreeTextQueryField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private Integer minTermLength;
	private Float boost;

	public FreeTextQueryField(final String field)
	{
		this.field = field;
	}

	public FreeTextQueryField(final String field, final Integer minTermLength, final Float boost)
	{
		this.field = field;
		this.minTermLength = minTermLength;
		this.boost = boost;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	public Float getBoost()
	{
		return boost;
	}

	public void setBoost(final Float boost)
	{
		this.boost = boost;
	}

	public Integer getMinTermLength()
	{
		return minTermLength;
	}

	public void setMinTermLength(final Integer minTermLength)
	{
		this.minTermLength = minTermLength;
	}
}
