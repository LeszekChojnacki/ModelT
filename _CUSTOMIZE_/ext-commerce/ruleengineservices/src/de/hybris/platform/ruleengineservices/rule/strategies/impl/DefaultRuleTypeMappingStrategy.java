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
package de.hybris.platform.ruleengineservices.rule.strategies.impl;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleTemplateModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleTypeMappingException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleTypeMappingStrategy;
import de.hybris.platform.servicelayer.type.TypeService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleTypeMappingStrategy implements RuleTypeMappingStrategy
{
	private static final String TEMPLATE_SUFFIX = "Template";

	private TypeService typeService;

	@Override
	public Class<? extends AbstractRuleModel> findRuleType(final Class<? extends AbstractRuleTemplateModel> templateType)
	{
		if (templateType == null)
		{
			throw new RuleTypeMappingException("Template type cannot be null");
		}

		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(templateType);
		final String typeCode = composedType.getCode();

		if (!StringUtils.endsWith(typeCode, TEMPLATE_SUFFIX))
		{
			throw new RuleTypeMappingException("Template name does not follow a convention");
		}

		final String potentialRuleTypeCode = StringUtils.removeEnd(typeCode, TEMPLATE_SUFFIX);
		return typeService.getModelClass(potentialRuleTypeCode);
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
