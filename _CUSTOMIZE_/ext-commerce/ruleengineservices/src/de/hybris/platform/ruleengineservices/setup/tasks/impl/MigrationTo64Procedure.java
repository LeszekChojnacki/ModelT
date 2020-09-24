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
package de.hybris.platform.ruleengineservices.setup.tasks.impl;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Procedure that composes all necessary tasks that do the rule engine migration to the 6.4 version
 */
public class MigrationTo64Procedure implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(MigrationTo64Procedure.class);
	protected static final String SELECT_RULES_WITHOUT_VERSION = "SELECT {Pk} FROM {" + SourceRuleModel._TYPECODE + "} WHERE {"
			+ SourceRuleModel.VERSION + "} IS NULL";

	private FlexibleSearchService flexibleSearchService;
	private List<MigrationTask> migrationTasks;

	@Override
	public void execute(final SystemSetupContext context)
	{
		if (isRequired())
		{
			LOG.info("Rules migration to 6.4 release started");
			getMigrationTasks().forEach(task -> task.execute(context));
			LOG.info("Rules migration to 6.4 release finished");
		}
	}

	protected boolean isRequired()
	{
		return getFlexibleSearchService().search(SELECT_RULES_WITHOUT_VERSION).getTotalCount() > 0;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
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
