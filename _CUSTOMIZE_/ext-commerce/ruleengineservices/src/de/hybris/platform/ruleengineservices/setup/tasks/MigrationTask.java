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
package de.hybris.platform.ruleengineservices.setup.tasks;

import de.hybris.platform.core.initialization.SystemSetupContext;


/**
 * Provides interface for task that need to be exectud during migration
 */
public interface MigrationTask
{
	/**
	 * Executes migration task
	 * @param systemSetupContext - setup context that gives a control over task execution
	 */
	void execute(final SystemSetupContext systemSetupContext);
}
