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
package com.hybris.backoffice.config.impl;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.core.persistence.impl.jaxb.Widgets;


/**
 * Configuration service used for testing purposes. Allows injection of custom configuration snippets.
 */
public class TestingBackofficeWidgetPersistenceService extends BackofficeWidgetPersistenceService
{

	private String additionalWidgetConfig;


	@Override
	public void resetToDefaults()
	{
		clearAdditionalWidgetConfig();
		super.resetToDefaults();
	}

	@Override
	protected Widgets loadWidgets(final InputStream inputStream)
	{
		final Widgets widgets = super.loadWidgets(inputStream);
		if (StringUtils.isNotEmpty(additionalWidgetConfig))
		{
			final Widgets additionalWidgets = super.loadWidgets(new ByteArrayInputStream(getAdditionalWidgetConfig().getBytes()));
			addAdditionalWidgets(widgets, additionalWidgets);
		}
		return widgets;
	}

	/**
	 * Clears additional test configuration of the widgets.
	 */
	public void clearAdditionalWidgetConfig()
	{
		additionalWidgetConfig = StringUtils.EMPTY;
	}

	protected String getAdditionalWidgetConfig()
	{
		return additionalWidgetConfig;
	}

	public void setAdditionalWidgetConfig(final String additionalWidgetConfig)
	{
		this.additionalWidgetConfig = additionalWidgetConfig;
	}

}
