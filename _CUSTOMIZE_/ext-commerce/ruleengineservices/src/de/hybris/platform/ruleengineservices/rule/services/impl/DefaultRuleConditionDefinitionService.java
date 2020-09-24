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
package de.hybris.platform.ruleengineservices.rule.services.impl;

import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleConditionDefinitionDao;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionDefinitionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link RuleConditionDefinitionService}
 */
public class DefaultRuleConditionDefinitionService implements RuleConditionDefinitionService
{
	private RuleConditionDefinitionDao ruleConditionDefinitionDao;

	@Override
	public List<RuleConditionDefinitionModel> getAllRuleConditionDefinitions()
	{
		return ruleConditionDefinitionDao.findAllRuleConditionDefinitions();
	}

	@Override
	public List<RuleConditionDefinitionModel> getRuleConditionDefinitionsForRuleType(final Class<?> ruleType)
	{
		return ruleConditionDefinitionDao.findRuleConditionDefinitionsByRuleType(ruleType);
	}

	public RuleConditionDefinitionDao getRuleConditionDefinitionDao()
	{
		return ruleConditionDefinitionDao;
	}

	@Required
	public void setRuleConditionDefinitionDao(final RuleConditionDefinitionDao ruleConditionDefinitionDao)
	{
		this.ruleConditionDefinitionDao = ruleConditionDefinitionDao;
	}
}
