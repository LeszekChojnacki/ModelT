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

import static java.util.Collections.emptySet;

import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * provides a RuleConfigurationRRD copied from the given RuleConfigurationRRD template
 */
public class DefaultRuleConfigurationRRDTemplateProvider implements RAOProvider
{

	private Converter<RuleConfigurationRRD, RuleConfigurationRRD> ruleConfigurationRrdTemplateConverter;

	@Override
	public Set expandFactModel(final Object factTemplate)
	{
		if (factTemplate instanceof RuleConfigurationRRD)
		{
			return Collections.singleton(createRAO((RuleConfigurationRRD) factTemplate));
		}
		else
		{
			return emptySet();
		}
	}

	protected RuleConfigurationRRD createRAO(final RuleConfigurationRRD template)
	{
		return getRuleConfigurationRrdTemplateConverter().convert(template);
	}

	protected Converter<RuleConfigurationRRD, RuleConfigurationRRD> getRuleConfigurationRrdTemplateConverter()
	{
		return ruleConfigurationRrdTemplateConverter;
	}

	@Required
	public void setRuleConfigurationRrdTemplateConverter(
			final Converter<RuleConfigurationRRD, RuleConfigurationRRD> ruleConfigurationConverter)
	{
		this.ruleConfigurationRrdTemplateConverter = ruleConfigurationConverter;
	}

}
