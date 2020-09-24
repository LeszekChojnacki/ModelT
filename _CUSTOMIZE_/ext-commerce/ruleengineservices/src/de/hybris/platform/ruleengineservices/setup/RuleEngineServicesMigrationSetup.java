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
package de.hybris.platform.ruleengineservices.setup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;


/**
 * Hook to initialization/update process
 */
@SystemSetup(extension = RuleEngineConstants.EXTENSIONNAME)
public class RuleEngineServicesMigrationSetup
{
	private static final Logger LOG = LoggerFactory.getLogger(RuleEngineServicesMigrationSetup.class);
	private List<MigrationTask> migrationTasks;

	@SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.UPDATE)
	public void execute(final SystemSetupContext context)
	{
		LOG.info("Rules migration started");
		getMigrationTasks().forEach(task -> task.execute(context));
		LOG.info("Rules migration finished");
	}

	protected List<MigrationTask> getMigrationTasks()
	{
		return migrationTasks;
	}

	@Required
	public void setMigrationTasks(final List<MigrationTask> migrationTasks)
	{
		this.migrationTasks = migrationTasks;
	}
}
