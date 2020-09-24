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
package de.hybris.platform.droolsruleengineservices.agendafilter.impl;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MAXIMUM_RULE_EXECUTIONS;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_CODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_EXCLUSIVE;
import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.DEFAULT_MAX_ALLOWED_RUNS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.rule.evaluation.impl.RuleAndRuleGroupExecutionTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.definition.rule.Rule;


/**
 * Used during rule execution to track and control rule execution
 */
public class DefaultRuleAndRuleGroupExecutionTracker implements RuleAndRuleGroupExecutionTracker
{

	private final Map<String, Integer> executedRules;
	private final Map<String, Map<String, Integer>> executedRuleGroups;
	private final List<String> actionsInvoked;

	public DefaultRuleAndRuleGroupExecutionTracker()
	{
		this.executedRules = new HashMap<>();
		this.actionsInvoked = new ArrayList<>();
		this.executedRuleGroups = new HashMap<>();
	}

	@Override
	public boolean allowedToExecute(final Object theRule)
	{
		final Rule rule = (Rule) theRule;
		final String ruleCode = getRuleCode(rule);
		final int maximumRuleExecutions = getMaximumRuleExecutions(rule);
		final String ruleGroupCode = getRuleGroupCode(rule);
		final boolean ruleGroupIsExclusive = isRuleGroupExclusive(rule);

		// allowed to execute unless:
		// 1) rule has been executed more than maximum rule executions
		final Integer currentRuns = getExecutedRules().get(ruleCode);
		if (currentRuns != null && maximumRuleExecutions <= currentRuns)
		{
			return false;
		}

		// 2) rulegroup exists and is exclusive and has been executed already
		//   (except if the rule that consumed it fires again)
		if (ruleGroupCode != null && ruleGroupIsExclusive)
		{
			final Map<String, Integer> groupEntries = getExecutedRuleGroups().get(ruleGroupCode);
			if (nonNull(groupEntries) && !groupEntries.isEmpty() && !groupEntries.containsKey(ruleCode))
			{
				return false;
			}
		}

		return true;
	}


	@Override
	public void trackActionExecutionStarted(final String ruleCode)
	{
		actionsInvoked.add(ruleCode);
	}

	@Override
	public void trackRuleExecution(final Object kcontext)
	{
		final KnowledgeHelper helper = (KnowledgeHelper) kcontext;

		final Rule rule = helper.getRule();
		final String ruleCode = getRuleCode(rule);

		// track rule execution only if action(s) has been invoked successfully
		if (actionsInvoked.contains(ruleCode))
		{
			getExecutedRules().compute(ruleCode, (k, v) -> v == null ? 1 : Integer.valueOf(v.intValue() + 1));

			// track rule group execution
			final String ruleGroupCode = getRuleGroupCode(rule);
			if (nonNull(ruleGroupCode))
			{
				final Map<String, Integer> rgExecutions = getExecutedRuleGroups().computeIfAbsent(ruleGroupCode,
						k -> new HashMap<>());
				rgExecutions.compute(ruleCode, (k, v) -> v == null ? 1 : Integer.valueOf(v.intValue() + 1));
			}
		}
	}

	protected int getMaximumRuleExecutions(final Rule rule)
	{
		final String maxRuns = getMetaData(rule, RULEMETADATA_MAXIMUM_RULE_EXECUTIONS);
		return (maxRuns == null ? DEFAULT_MAX_ALLOWED_RUNS : Integer.parseInt(maxRuns));
	}

	protected boolean isRuleGroupExclusive(final Rule rule)
	{
		return Boolean.parseBoolean(getMetaData(rule, RULEMETADATA_RULEGROUP_EXCLUSIVE));
	}

	protected String getRuleGroupCode(final Rule rule)
	{
		return getMetaData(rule, RULEMETADATA_RULEGROUP_CODE);
	}

	protected String getRuleCode(final Rule rule)
	{
		return getMetaData(rule, RULEMETADATA_RULECODE);
	}

	protected String getMetaData(final Rule rule, final String key)
	{
		final Object value = rule.getMetaData().get(key);
		return isNull(value) ? null : value.toString();
	}

	protected Map<String, Integer> getExecutedRules()
	{
		return executedRules;
	}

	protected Map<String, Map<String, Integer>> getExecutedRuleGroups()
	{
		return executedRuleGroups;
	}

	protected List<String> getActionsInvoked()
	{
		return actionsInvoked;
	}
}
