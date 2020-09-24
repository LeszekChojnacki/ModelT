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
package com.hybris.backoffice.cockpitng.classification.labels.impl;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;

import com.hybris.cockpitng.labels.LabelProvider;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.Range;


/**
 * Label Provider for Ranges
 */
public class RangeLabelProvider implements LabelProvider<Range>
{
	private LabelService labelService;

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}


	@Override
	public String getLabel(final Range range)
	{
		if (range == null)
		{
			return null;
		}
		final String labelFrom = Labels.getLabel("range.from", "from");
		final String labelTo = Labels.getLabel("range.to", "to");
		if (range.getStart() != null && range.getEnd() != null)
		{
			final String startValue = getStringValue(range.getStart());
			final String endValue = getStringValue(range.getEnd());
			return labelFrom + " " + startValue + " " + labelTo + " " + endValue;
		}
		else if (range.getStart() != null)
		{
			final String startValue = getStringValue(range.getStart());
			return labelFrom + " " + startValue;
		}
		else if (range.getEnd() != null)
		{
			final String endValue = getStringValue(range.getEnd());
			return labelTo + " " + endValue;
		}
		return null;
	}

	private String getStringValue(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		return (object instanceof String) ? object.toString() : labelService.getObjectLabel(object);
	}


	@Override
	public String getDescription(final Range range)
	{
		return null;
	}

	@Override
	public String getIconPath(final Range range)
	{
		return null;
	}
}
