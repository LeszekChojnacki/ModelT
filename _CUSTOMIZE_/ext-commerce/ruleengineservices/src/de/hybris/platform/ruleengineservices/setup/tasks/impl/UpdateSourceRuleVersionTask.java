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
import de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Update {@link de.hybris.platform.ruleengineservices.jalo.SourceRule}s version:
 */
public class UpdateSourceRuleVersionTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(UpdateSourceRuleVersionTask.class);
	private ModelService modelService;
	private RuleDao ruleDao;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Updating Source Rules version");
		final List<AbstractRuleModel> allRules = getRuleDao().findAllRules();
		allRules.stream().forEach(this::changeVersion);
		getModelService().saveAll(allRules);
	}

	protected void changeVersion(final AbstractRuleModel rule)
	{
		if (rule.getVersion() == null)
		{
			rule.setVersion(RuleEngineServicesConstants.DEFAULT_RULE_VERSION);
		}
	}

	public RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
