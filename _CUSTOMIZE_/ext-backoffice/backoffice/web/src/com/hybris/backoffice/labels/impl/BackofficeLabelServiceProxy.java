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
package com.hybris.backoffice.labels.impl;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.Locales;

import com.hybris.backoffice.proxy.LabelServiceProxy;
import com.hybris.cockpitng.labels.LabelService;


public class BackofficeLabelServiceProxy implements LabelServiceProxy
{
	private LabelService labelService;

	@Override
	public String getObjectLabel(final Object object, final Locale locale)
	{
		final Locale currentLocal = Locales.getCurrent();

		Locales.setThreadLocal(locale);
		final String objectLabel = getObjectLabel(object);
		Locales.setThreadLocal(currentLocal);

		return objectLabel;
	}

	public String getObjectLabel(final Object object)
	{
		return labelService.getObjectLabel(object);
	}

	@Override
	public String getObjectDescription(final Object object)
	{
		return labelService.getObjectDescription(object);
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
