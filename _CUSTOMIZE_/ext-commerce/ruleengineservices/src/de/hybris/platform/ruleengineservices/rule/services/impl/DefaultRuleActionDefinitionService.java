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

import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleActionDefinitionDao;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionDefinitionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link RuleActionDefinitionService}
 */
public class DefaultRuleActionDefinitionService implements RuleActionDefinitionService
{
	private RuleActionDefinitionDao ruleActionDefinitionDao;

	@Override
	public List<RuleActionDefinitionModel> getAllRuleActionDefinitions()
	{
		return ruleActionDefinitionDao.findAllRuleActionDefinitions();
	}

	@Override
	public List<RuleActionDefinitionModel> getRuleActionDefinitionsForRuleType(final Class<?> ruleType)
	{
		return ruleActionDefinitionDao.findRuleActionDefinitionsByRuleType(ruleType);
	}

	public RuleActionDefinitionDao getRuleActionDefinitionDao()
	{
		return ruleActionDefinitionDao;
	}

	@Required
	public void setRuleActionDefinitionDao(final RuleActionDefinitionDao ruleActionDefinitionDao)
	{
		this.ruleActionDefinitionDao = ruleActionDefinitionDao;
	}
}
