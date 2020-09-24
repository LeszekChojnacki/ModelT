/*
 * [y] hybris Platform
 *
 * Copyright (c) 2019 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.droolsruleengineservices.compiler.impl;


import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;


/**
 * Drools rule value formatter used as part of rule's conditions generation process for translation of
 * {@link de.hybris.platform.ruleengineservices.compiler.RuleIrAction}
 */
public class ConditionsDroolsRuleValueFormatter extends DefaultDroolsRuleValueFormatter
{
	private Map<RuleIrAttributeOperator,
			Function<RuleIrAttributeCondition, Supplier<Map<String, DroolsRuleValueFormatterHelper>>>> formatterSuppliers;

	@Override
	public String formatValue(final DroolsRuleGeneratorContext context, final Object object)
	{
		if (object instanceof RuleIrAttributeCondition)
		{
			final RuleIrAttributeCondition attribute = (RuleIrAttributeCondition) object;
			return getFormatterSuppliers().containsKey(attribute.getOperator()) ?
					formatValue(context, attribute.getValue(), getFormatterSuppliers().get(attribute.getOperator()).apply(attribute)) :
					formatValue(context, attribute.getValue());
		}

		return super.formatValue(context, object);
	}

	@PostConstruct
	public void initializeFormatterSupplierstest()
	{
		formatterSuppliers = ImmutableMap.of( //
				RuleIrAttributeOperator.IN,
				(condition) -> () -> ImmutableMap.of(AbstractList.class.getName(), (context, value) -> {
					final StringJoiner joiner = new StringJoiner(" || " + condition.getAttribute() + " == ",
							condition.getAttribute() + " == ", StringUtils.EMPTY);

					((List<Object>) value).stream().forEach(v -> joiner.add(formatValue(context, v)));

					return joiner.toString();
				}), //
				RuleIrAttributeOperator.NOT_IN, //
				(condition) -> () -> ImmutableMap.of(AbstractList.class.getName(), (context, value) -> {
					final StringJoiner joiner = new StringJoiner(", " + condition.getAttribute() + " != ",
							condition.getAttribute() + " != ", StringUtils.EMPTY);

					((List<Object>) value).stream().forEach(v -> joiner.add(formatValue(context, v)));

					return joiner.toString();
				}));
	}

	protected Map<RuleIrAttributeOperator,
			Function<RuleIrAttributeCondition, Supplier<Map<String, DroolsRuleValueFormatterHelper>>>> getFormatterSuppliers()
	{
		return formatterSuppliers;
	}
}