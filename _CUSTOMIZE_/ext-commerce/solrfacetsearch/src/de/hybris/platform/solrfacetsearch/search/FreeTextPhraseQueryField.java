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


public class FreeTextPhraseQueryField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private Float slop;
	private Float boost;

	public FreeTextPhraseQueryField(final String field)
	{
		this.field = field;
	}

	public FreeTextPhraseQueryField(final String field, final Float slop)
	{
		this.field = field;
		this.slop = slop;
	}

	public FreeTextPhraseQueryField(final String field, final Float slop, final Float boost)
	{
		this.field = field;
		this.slop = slop;
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

	public Float getSlop()
	{
		return slop;
	}

	public void setSlop(final Float slop)
	{
		this.slop = slop;
	}

	public Float getBoost()
	{
		return boost;
	}

	public void setBoost(final Float boost)
	{
		this.boost = boost;
	}
}
