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
package de.hybris.platform.ruleengine.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.api.definition.rule.Rule;


/**
 * DroolsRuleLoopException indicates that a potentially infinite loop has been detected (based on the fact that a rule
 * has been fired more than {@code limit} times.
 */
public class DroolsRuleLoopException extends RuleEngineRuntimeException
{
	private final long limit;
	private final transient Map<Rule, Long> ruleMap;

	public DroolsRuleLoopException(final long limit, final Map<Rule, Long> ruleMap)
	{
		super();
		this.limit = limit;
		this.ruleMap = ruleMap;
	}

	public long getLimit()
	{
		return limit;
	}

	/**
	 * Returns an ordered list of strings for all rules that have fired during the rule evaluation. Each string
	 * represents how often a specific rule has fired. The result is in the format: {@code $firedCount:$Rule.id}, e.g.
	 * {@code 100:Rule Number 14}
	 *
	 * @return the list of all rule firings
	 */
	public List<String> getAllRuleFirings()
	{
		return getRuleFirings(Long.MAX_VALUE);
	}

	/**
	 * Returns the list of strings as described in {@link #getAllRuleFirings()} but limited by {@code size}.
	 *
	 * @param size
	 *           limit the list of strings returned
	 * @return the list of the {@code size} most often fired rules
	 */
	public List<String> getRuleFirings(final long size)
	{
		if (ruleMap == null)
		{
			return Collections.emptyList();
		}
		return ruleMap.entrySet().stream().sorted(Map.Entry.<Rule, Long> comparingByValue().reversed()).limit(size)
				.map(e -> e.getValue().toString() + ":" + e.getKey().getId()).collect(Collectors.toList());
	}

	/**
	 * Returns the 10 most fired rules and its firing count.
	 */
	@Override
	public String getMessage()
	{
		final StringBuilder sb = new StringBuilder();
		// top ten rules
		sb.append("Possible rule-loop detected. Maximum allowed rule matches has been exceeded.").append(System.lineSeparator());
		sb.append("Current Limit:").append(limit).append(System.lineSeparator());
		for (final String ruleFiring : getRuleFirings(10))
		{
			sb.append(ruleFiring).append(System.lineSeparator());
		}
		sb.append("You can adjust or disable the limit for rule matches by changing the ruleFiringLimit field in the 'Drools Engine Context' object (see the 'Rule Firing Limit' attribute).\n");
		return sb.toString();
	}

}
