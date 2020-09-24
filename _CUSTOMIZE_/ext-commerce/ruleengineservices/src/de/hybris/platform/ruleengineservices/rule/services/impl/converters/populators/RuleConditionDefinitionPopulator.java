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
import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionCategoryModel;
import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;
import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionParameterModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionCategoryData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleConditionDefinitionPopulator implements Populator<RuleConditionDefinitionModel, RuleConditionDefinitionData>
{
	private Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> ruleConditionDefinitionCategoryConverter; // NOPMD
	private Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleConditionDefinitionParameterConverter; // NOPMD

	@Override
	public void populate(final RuleConditionDefinitionModel source, final RuleConditionDefinitionData target)
	{
		target.setId(source.getId());
		target.setName(source.getName());
		target.setPriority(source.getPriority());
		target.setBreadcrumb(source.getBreadcrumb());
		target.setAllowsChildren(source.getAllowsChildren());
		target.setTranslatorId(source.getTranslatorId());
		target.setTranslatorParameters(MapUtils.isEmpty(source.getTranslatorParameters()) ? Collections.emptyMap()
				: new HashMap<>(source.getTranslatorParameters()));
		target.setCategories(populateCategories(source));
		target.setParameters(populateParameters(source));
	}

	protected List<RuleConditionDefinitionCategoryData> populateCategories(final RuleConditionDefinitionModel source)
	{
		if (CollectionUtils.isNotEmpty(source.getCategories()))
		{
			return Converters.convertAll(source.getCategories(), ruleConditionDefinitionCategoryConverter);
		}
		return Collections.<RuleConditionDefinitionCategoryData> emptyList();
	}

	protected Map<String, RuleParameterDefinitionData> populateParameters(final RuleConditionDefinitionModel source)
	{
		final Map<String, RuleParameterDefinitionData> parameters = new HashMap<>();

		if (CollectionUtils.isNotEmpty(source.getParameters()))
		{
			for (final RuleConditionDefinitionParameterModel sourceParameter : source.getParameters())
			{
				final String parameterId = sourceParameter.getId();
				final RuleParameterDefinitionData parameter = ruleConditionDefinitionParameterConverter.convert(sourceParameter);
				parameters.put(parameterId, parameter);
			}
		}

		return parameters;
	}

	public Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> getRuleConditionDefinitionCategoryConverter()
	{
		return ruleConditionDefinitionCategoryConverter;
	}

	@Required
	public void setRuleConditionDefinitionCategoryConverter(
			final Converter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> ruleConditionDefinitionCategoryConverter) // NOPMD
	{
		this.ruleConditionDefinitionCategoryConverter = ruleConditionDefinitionCategoryConverter;
	}

	public Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> getRuleConditionDefinitionParameterConverter()
	{
		return ruleConditionDefinitionParameterConverter;
	}

	@Required
	public void setRuleConditionDefinitionParameterConverter(
			final Converter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleConditionDefinitionParameterConverter) // NOPMD
	{
		this.ruleConditionDefinitionParameterConverter = ruleConditionDefinitionParameterConverter;
	}
}
