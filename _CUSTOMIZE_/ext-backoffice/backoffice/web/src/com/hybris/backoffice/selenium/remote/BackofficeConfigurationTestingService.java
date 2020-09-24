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

import java.io.IOException;

/**
 * Interface used for remote method invocation (via Spring's HTTP Invoker)
 */
public interface BackofficeConfigurationTestingService
{

	void importImpex(String content);

	void reloadValidationEngine();

	void resetCockpitConfig();

	void resetConfigurationCache();

	void applyTestConfigurationToConfigurationCache(String fileContent, String moduleName) throws IOException;

	void resetWidgetConfig();

	void applyTestWidgetConfig(String configFileName) throws IOException;

}
