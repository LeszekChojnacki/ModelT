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
package de.hybris.platform.droolsruleengineservices.compiler.impl;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.nonNull;

import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleMetadataGenerator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;


/**
 * Default implementation of {@link DroolsRuleMetadataGenerator}.
 *
 */
public class DefaultDroolsRuleMetadataGenerator implements DroolsRuleMetadataGenerator
{
	@Override
	public String generateMetadata(final DroolsRuleGeneratorContext context, final String indentation)
	{
		final List<RuleIrCondition> conditions = context.getRuleIr().getConditions();
		final Map<String, List<Object>> conditionMetadata = newHashMap();

		final List<Map<String, Object>> metadataList = conditions.stream().filter(c -> nonNull(c.getMetadata()))
				.map(RuleIrCondition::getMetadata).collect(Collectors.toList());
		for (final Map<String, Object> metadata : metadataList)
		{
			for (final Map.Entry<String, Object> entry : metadata.entrySet())
			{
				if (!conditionMetadata.containsKey(entry.getKey()))
				{
					conditionMetadata.put(entry.getKey(), newArrayList());
				}
				if (entry.getValue() instanceof Collection<?>)
				{
					conditionMetadata.get(entry.getKey()).addAll((Collection) entry.getValue());
				}
				else
				{
					conditionMetadata.get(entry.getKey()).add(entry.getValue());
				}
			}
		}

		final StringJoiner conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
		for (final Map.Entry<String, List<Object>> entry : conditionMetadata.entrySet())
		{
			final String metadataValue = entry.getValue().stream().map(o -> String.format("\"%s\"", o.toString()))
					.collect(Collectors.joining(","));
			conditionsJoiner.add("@").add(entry.getKey()).add(" ( ").add(metadataValue).add(" )\n");
		}
		return conditionsJoiner.toString();
	}
}
