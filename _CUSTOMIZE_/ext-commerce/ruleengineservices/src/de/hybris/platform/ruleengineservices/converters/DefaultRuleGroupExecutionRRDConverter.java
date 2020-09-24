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
package de.hybris.platform.ruleengineservices.converters;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Required;


/**
 * DefaultRuleGroupExecutionRRDConverter is a converter for converting an {@code AbstractRuleEngineRuleModel} to a set
 * of {@link RuleGroupExecutionRRD}s.
 */
public class DefaultRuleGroupExecutionRRDConverter implements Converter<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD>
{
	private Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> ruleGroupExecutionRrdPopulator;

	@Override
	public RuleGroupExecutionRRD convert(final AbstractRuleEngineRuleModel source) 
	{
		final RuleGroupExecutionRRD result = new RuleGroupExecutionRRD();
		result.setExecutedRules(new LinkedHashMap<>());
		return convert(source, result);
	}

	@Override
	public RuleGroupExecutionRRD convert(final AbstractRuleEngineRuleModel source, final RuleGroupExecutionRRD prototype)
			
	{
		getRuleGroupExecutionRrdPopulator().populate(source, prototype);
		return prototype;
	}

	protected Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> getRuleGroupExecutionRrdPopulator()
	{
		return ruleGroupExecutionRrdPopulator;
	}

	@Required
	public void setRuleGroupExecutionRrdPopulator(
			final Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> ruleGroupExecutionRrdPopulator)
	{
		this.ruleGroupExecutionRrdPopulator = ruleGroupExecutionRrdPopulator;
	}

}
