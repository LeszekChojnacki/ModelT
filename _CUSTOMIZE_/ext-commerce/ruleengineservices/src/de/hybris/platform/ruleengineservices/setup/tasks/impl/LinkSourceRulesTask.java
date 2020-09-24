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

import static java.util.stream.Collectors.toList;


import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.jalo.SourceRule;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * Link the {@link de.hybris.platform.ruleengineservices.jalo.SourceRule}s (version = 0, status=
 * {@link RuleStatus#INACTIVE} or status={@link RuleStatus#ARCHIVED}) and
 * {@link de.hybris.platform.ruleengine.jalo.DroolsRule} together, using the "uuid" as reference. The
 * {@link RuleStatus#INACTIVE} or status={@link RuleStatus#ARCHIVED})
 * {@link de.hybris.platform.ruleengineservices.jalo.SourceRule} having {@link SourceRule#getVersion()} = 0) is linked
 * to the latest/highest version of its corresponding {@link de.hybris.platform.ruleengine.jalo.DroolsRule}
 */
public class LinkSourceRulesTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(LinkSourceRulesTask.class);
	private EngineRuleDao engineRuleDao;
	private RuleDao ruleDao;
	private ModelService modelService;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Linking source rules with engine rules");
		final List<AbstractRuleEngineRuleModel> linkedEngineRules = selectRules(RuleStatus.INACTIVE, RuleStatus.ARCHIVED).map(this::updateMapping)
				.filter(Objects::nonNull).collect(toList());
		getModelService().saveAll(linkedEngineRules);
	}

	protected AbstractRuleEngineRuleModel updateMapping(final SourceRuleModel rule)
	{
		final AbstractRuleEngineRuleModel ruleEngineRule = getEngineRuleDao().getRuleByUuid(rule.getUuid());
		if (null != ruleEngineRule)
		{
			ruleEngineRule.setSourceRule(rule);
		}
		return ruleEngineRule;
	}

	@SuppressWarnings({"deprecation","findByVersionAndStatuses"})
	protected Stream<SourceRuleModel> selectRules(final RuleStatus... statuses)
	{
		return getRuleDao().<SourceRuleModel>findByVersionAndStatuses(RuleEngineServicesConstants.DEFAULT_RULE_VERSION, statuses).stream();
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

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
	}
}
