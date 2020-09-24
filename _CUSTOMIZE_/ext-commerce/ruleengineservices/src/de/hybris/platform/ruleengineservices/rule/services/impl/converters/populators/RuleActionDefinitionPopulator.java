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

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionCategoryModel;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionModel;
import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionParameterModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionCategoryData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleActionDefinitionPopulator implements Populator<RuleActionDefinitionModel, RuleActionDefinitionData>
{
	private Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> ruleActionDefinitionCategoryConverter; // NOPMD
	private Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> ruleActionDefinitionParameterConverter; // NOPMD

	@Override
	public void populate(final RuleActionDefinitionModel source, final RuleActionDefinitionData target)
	{
		target.setId(source.getId());
		target.setName(source.getName());
		target.setPriority(source.getPriority());
		target.setBreadcrumb(source.getBreadcrumb());
		target.setTranslatorId(source.getTranslatorId());
		target.setTranslatorParameters(new HashMap<>(source.getTranslatorParameters()));
		target.setCategories(populateCategories(source));
		target.setParameters(populateParameters(source));
	}

	protected List<RuleActionDefinitionCategoryData> populateCategories(final RuleActionDefinitionModel source)
	{
		if (CollectionUtils.isNotEmpty(source.getCategories()))
		{
			return Converters.convertAll(source.getCategories(), ruleActionDefinitionCategoryConverter);
		}
		return Collections.<RuleActionDefinitionCategoryData> emptyList();
	}

	protected Map<String, RuleParameterDefinitionData> populateParameters(final RuleActionDefinitionModel source)
	{
		final Map<String, RuleParameterDefinitionData> parameters = new HashMap<>();

		if (CollectionUtils.isNotEmpty(source.getParameters()))
		{
			for (final RuleActionDefinitionParameterModel sourceParameter : source.getParameters())
			{
				final String parameterId = sourceParameter.getId();
				final RuleParameterDefinitionData parameter = ruleActionDefinitionParameterConverter.convert(sourceParameter);
				parameters.put(parameterId, parameter);
			}
		}

		return parameters;
	}

	public Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> getRuleActionDefinitionCategoryConverter()
	{
		return ruleActionDefinitionCategoryConverter;
	}

	@Required
	public void setRuleActionDefinitionCategoryConverter(
			final Converter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> ruleActionDefinitionCategoryConverter) // NOPMD
	{
		this.ruleActionDefinitionCategoryConverter = ruleActionDefinitionCategoryConverter;
	}

	public Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> getRuleActionDefinitionParameterConverter()
	{
		return ruleActionDefinitionParameterConverter;
	}

	@Required
	public void setRuleActionDefinitionParameterConverter(
			final Converter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> ruleActionDefinitionParameterConverter) // NOPMD
	{
		this.ruleActionDefinitionParameterConverter = ruleActionDefinitionParameterConverter;
	}
}
