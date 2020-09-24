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
package com.hybris.backoffice.selenium.remote;

/**
 * Service used by Selenium via HttpInvoker to execute admin operations.
 */
public interface BackofficeAdminTestingService
{

	/**
	 * Resets to defaults.
	 */
	void resetToDefaults();

	/**
	 * Applies widgets configuration.
	 *
	 * @param content
	 *           the configuration.
	 * @param rootWidgetId
	 *           the root widget id.
	 */
	void applyWidgetsConfiguration(String content, String rootWidgetId);

	/**
	 * Applies cockpit configuration.
	 *
	 * @param content
	 *           the configuration.
	 */
	void applyCockpitConfiguration(String content);

	/**
	 * Imports impex.
	 *
	 * @param content
	 *           the impex.
	 */
	void importImpex(String content);

}
