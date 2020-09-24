/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.refresh;

import java.util.Map;

import org.apache.log4j.Logger;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

public class RefreshAfterCreationController extends DefaultWidgetController
{
	private static final Logger LOG = Logger.getLogger(RefreshAfterCreationController.class);

	private static final String SOCKET_IN_WIZARD_RESULT = "wizardResult";
	private static final String SETTING_REFRESH_RESULTS = "refreshResults";
	private static final String SETTING_PARAM_NAME = "paramName";

	@SocketEvent(socketId = SOCKET_IN_WIZARD_RESULT)
	public void executeAfterWizard(final Object wizardResult)
	{
		if (!(wizardResult instanceof Map))
		{
			LOG.error("Object is not a Map");
			return;
		}

		if (!((Map) wizardResult).containsKey(getWidgetSettings().getString(SETTING_PARAM_NAME)))
		{
			return;
		}

		if (getWidgetSettings().getBoolean(SETTING_REFRESH_RESULTS))
		{
			sendOutput(SETTING_REFRESH_RESULTS, null);
		}
	}
}
