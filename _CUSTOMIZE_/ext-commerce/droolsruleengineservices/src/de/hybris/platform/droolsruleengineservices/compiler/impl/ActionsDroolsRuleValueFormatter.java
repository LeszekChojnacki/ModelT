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


import java.util.AbstractList;
import java.util.List;
import java.util.StringJoiner;


/**
 * Drools rule value formatter used as part rule actions generation process for translation of
 * {@link de.hybris.platform.ruleengineservices.compiler.RuleIrAction}
 */
public class ActionsDroolsRuleValueFormatter extends DefaultDroolsRuleValueFormatter
{
	/**
	 * This method initializes formatters via its parent and overrides formatter to be used for {@link AbstractList}
	 */
	@Override
	public void initFormatters()
	{
		super.initFormatters();

		getFormatters().put(AbstractList.class.getName(), (context, value) -> {
			final StringJoiner joiner = new StringJoiner(", ", "[", "]");

			((List<Object>) value).stream().forEach(v -> joiner.add(formatValue(context, v)));

			return joiner.toString();
		});
	}
}
