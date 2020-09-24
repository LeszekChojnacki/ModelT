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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import static java.util.Collections.emptySet;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * DefaultRuleConfigurationRRDProvider is the default implementation for the rule configuration {@code RRD} provider
 * used by the default drools rule engine.
 */
public class DefaultRuleConfigurationRRDProvider implements RAOProvider
{

	private Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> ruleConfigurationRrdConverter;

	@Override
	public Set expandFactModel(final Object modelFact)
	{
		if (modelFact instanceof AbstractRuleEngineRuleModel)
		{
			return Collections.singleton(createRAO((AbstractRuleEngineRuleModel) modelFact));
		}
		else
		{
			return emptySet();
		}
	}

	protected RuleConfigurationRRD createRAO(final AbstractRuleEngineRuleModel source)
	{
		return getRuleConfigurationRrdConverter().convert(source);
	}

	protected Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> getRuleConfigurationRrdConverter()
	{
		return ruleConfigurationRrdConverter;
	}

	@Required
	public void setRuleConfigurationRrdConverter(
			final Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> ruleConfigurationConverter)
	{
		this.ruleConfigurationRrdConverter = ruleConfigurationConverter;
	}
}
