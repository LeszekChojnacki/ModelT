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
package de.hybris.platform.ruleengineservices.rule.services.impl.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionParameterModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueConverter;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Required;


public class RuleActionDefinitionParameterPopulator implements
		Populator<RuleActionDefinitionParameterModel, RuleParameterDefinitionData>
{
	private RuleParameterValueConverter ruleParameterValueConverter;

	public RuleParameterValueConverter getRuleParameterValueConverter()
	{
		return ruleParameterValueConverter;
	}

	@Required
	public void setRuleParameterValueConverter(final RuleParameterValueConverter ruleParameterValueConverter)
	{
		this.ruleParameterValueConverter = ruleParameterValueConverter;
	}

	@Override
	public void populate(final RuleActionDefinitionParameterModel source, final RuleParameterDefinitionData target)
	{
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setPriority(source.getPriority());
		target.setType(source.getType());
		target.setRequired(source.getRequired());
		target.setValidators(source.getValidators());
		target.setDefaultEditor(source.getDefaultEditor());
		target.setFilters(source.getFilters() != null ? source.getFilters() : Collections.emptyMap());

		try
		{
			final Object defaultValue = ruleParameterValueConverter.fromString(source.getValue(), source.getType());
			target.setDefaultValue(defaultValue);
		}
		catch (final RuleConverterException e)
		{
			throw new ConversionException(e.getMessage(), e);
		}
	}
}
