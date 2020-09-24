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
package de.hybris.platform.ruleengine.strategies.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.strategies.RulesModuleResolver;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengine.util.RuleMappings;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RulesModuleResolver}
 */
public class DefaultRulesModuleResolver implements RulesModuleResolver
{

	private RulesModuleDao rulesModuleDao;
	private EngineRulesRepository engineRulesRepository;

	@Override
	public String lookupForModuleName(final RuleType ruleType)
	{
		return lookupForRulesModule(ruleType).getName();
	}

	@Override
	public <T extends AbstractRulesModuleModel> T lookupForRulesModule(final RuleType ruleType)
	{
		checkArgument(Objects.nonNull(ruleType), "RuleType should be provided");

		final List<AbstractRulesModuleModel> rulesModulesByRuleType = getRulesModuleDao()
				.findActiveRulesModulesByRuleType(ruleType);

		if (isEmpty(rulesModulesByRuleType))
		{
			throw new IllegalStateException("No module found for the rule type [" + ruleType + "]");
		}

		if (rulesModulesByRuleType.size() == 1)
		{
			return (T) rulesModulesByRuleType.get(0);
		}
		else if (rulesModulesByRuleType.size() > 1)
		{
			throw new IllegalStateException("More than one module found for the rule type [" + ruleType + "]");
		}

		return null;
	}

	@Override
	public <T extends AbstractRulesModuleModel> List<T> lookupForRulesModules(final AbstractRuleModel rule)
	{
		checkArgument(Objects.nonNull(rule), "Rule should be provided");

		return rule.getEngineRules().stream()
				.filter(DroolsRuleModel.class::isInstance).map(DroolsRuleModel.class::cast)
				.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r, RuleMappings.moduleName(r))).
						map(r -> (T) RuleMappings.module(r))
				.distinct().collect(toList());
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected EngineRulesRepository getEngineRulesRepository()
	{
		return engineRulesRepository;
	}

	@Required
	public void setEngineRulesRepository(final EngineRulesRepository engineRulesRepository)
	{
		this.engineRulesRepository = engineRulesRepository;
	}
}
