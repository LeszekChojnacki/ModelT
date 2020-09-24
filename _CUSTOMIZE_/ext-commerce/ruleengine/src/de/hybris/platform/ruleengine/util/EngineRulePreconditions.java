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
package de.hybris.platform.ruleengine.util;

import com.google.common.base.Preconditions;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import java.util.Collection;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * Preconditions check methods for {@link de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel} subclasses
 */
public class EngineRulePreconditions
{

	private EngineRulePreconditions()
	{
	}

	/**
	 * Verify if all the rules in the collection are of the same type. Throw {@link IllegalStateException} otherwise
	 *
	 * @param engineRules
	 * 		collection of engine rules
	 * @param <T>
	 * 		type of the engine rule
	 * @throws IllegalStateException
	 * 		if rule type is not the same
	 */
	public static <T extends AbstractRuleEngineRuleModel> void checkRulesHaveSameType(final Collection<T> engineRules)
	{
		if (isNotEmpty(engineRules))
		{
			final T sampleEngineRule = engineRules.iterator().next();
			final RuleType ruleType = sampleEngineRule.getRuleType();
			if (isNull(ruleType))
			{
				throw new IllegalStateException("RuleType of engine rule [" + sampleEngineRule.getCode() + "] is NULL");
			}
			if (!engineRules.stream().allMatch(r -> ruleType.equals(r.getRuleType())))
			{
				throw new IllegalStateException("One or more rules in the collection are having different rule types");
			}
		}
	}

	/**
	 * Verify if all the rules in the collection can be undeployed. Throw {@link IllegalStateException} otherwise
	 *
	 * @param engineRules
	 * 		collection of source rules {@link SourceRuleModel}
	 * @param <T>
	 * 		type of the engine rule
	 * @throws IllegalStateException
	 * 		if rule status is {@link RuleStatus#ARCHIVED}
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	public static <T extends SourceRuleModel> void checkRulesCanBeUndeployed(final Collection<T> engineRules)
	{
		if (engineRules.stream().anyMatch(r -> RuleStatus.ARCHIVED.equals(r.getStatus())))
		{
			throw new IllegalStateException("Archived rules are not eligible to be undeployed.");
		}
	}

	/**
	 * Verify if all the rules in the collection can be published. Throw {@link IllegalStateException} otherwise
	 *
	 * @param engineRules
	 * 		collection of source rules {@link SourceRuleModel}
	 * @param <T>
	 * 		type of the engine rule
	 * @throws IllegalStateException
	 * 		if rule status is {@link RuleStatus#ARCHIVED}
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	public static <T extends SourceRuleModel> void checkRulesCanBePublished(final Collection<T> engineRules)
	{
		if (engineRules.stream().anyMatch(r -> RuleStatus.ARCHIVED.equals(r.getStatus())))
		{
			throw new IllegalStateException("Archived rules are not eligible to be published.");
		}
	}

	/**
	 * Verify if the engine rule is associated with any KIE module, throw {@link IllegalStateException} otherwise
	 *
	 * @param rule
	 * 		engine rule to check
	 * @param <T>
	 * 		type of the engine rule
	 * @throws IllegalStateException
	 * 		if any given rule has no KIE module
	 */
	public static <T extends AbstractRuleEngineRuleModel> void checkRuleHasKieModule(final T rule)
	{
		Preconditions.checkArgument(nonNull(rule), "Rule should not be null");
		Preconditions.checkArgument(rule instanceof DroolsRuleModel, "Rule must be instance of DroolsRuleModel");
		final DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
		if (isNull(droolsRule.getKieBase()))
		{
			throw new IllegalStateException("Rule [" + droolsRule.getCode() + "] has no KieBase assigned to it");
		}
		if (isNull(droolsRule.getKieBase().getKieModule()))
		{
			throw new IllegalStateException("Rule [" + droolsRule.getCode() + "] has no KieModule assigned to it");
		}
	}

}
