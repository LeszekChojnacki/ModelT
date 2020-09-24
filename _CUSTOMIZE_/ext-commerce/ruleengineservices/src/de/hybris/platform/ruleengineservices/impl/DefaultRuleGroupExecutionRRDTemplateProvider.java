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
package de.hybris.platform.ruleengineservices.impl;

import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * provides a RuleGroupExecutionRRD copied from the given RuleGroupExecutionRRD template
 */
public class DefaultRuleGroupExecutionRRDTemplateProvider implements RAOProvider
{

	private Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> ruleGroupExecutionRrdTemplateConverter;

	@Override
	public Set expandFactModel(final Object factTemplate)
	{
		if (factTemplate instanceof RuleGroupExecutionRRD)
		{
			return Collections.singleton(createRAO((RuleGroupExecutionRRD) factTemplate));
		}
		else
		{
			return Collections.emptySet();
		}
	}

	protected RuleGroupExecutionRRD createRAO(final RuleGroupExecutionRRD factTemplate)
	{
		return getRuleGroupExecutionRrdTemplateConverter().convert(factTemplate);
	}

	protected Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> getRuleGroupExecutionRrdTemplateConverter()
	{
		return ruleGroupExecutionRrdTemplateConverter;
	}

	@Required
	public void setRuleGroupExecutionRrdTemplateConverter(
			final Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> ruleGroupExecutionRrdTemplateConverter)
	{
		this.ruleGroupExecutionRrdTemplateConverter = ruleGroupExecutionRrdTemplateConverter;
	}
}
