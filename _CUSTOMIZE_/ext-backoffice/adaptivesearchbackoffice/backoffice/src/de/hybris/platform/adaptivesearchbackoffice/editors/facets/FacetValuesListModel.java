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
package de.hybris.platform.adaptivesearchbackoffice.editors.facets;

import de.hybris.platform.adaptivesearch.data.AsFacetValueData;

import java.util.List;

import org.zkoss.zul.SimpleListModel;


public class FacetValuesListModel extends SimpleListModel<AsFacetValueData>
{
	private final int stickyValuesSize;

	public FacetValuesListModel(final List<? extends AsFacetValueData> values, final int stickyValuesSize)
	{
		super(values);
		this.stickyValuesSize = stickyValuesSize;
	}

	public int getStickyValuesSize()
	{
		return stickyValuesSize;
	}
}
