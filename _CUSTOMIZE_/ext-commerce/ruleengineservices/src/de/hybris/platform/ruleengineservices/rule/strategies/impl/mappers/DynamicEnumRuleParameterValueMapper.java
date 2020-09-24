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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Performs mapping between Dynamic Enum(Enumeration Value) and a String representation of its instance.
 */
public class DynamicEnumRuleParameterValueMapper implements RuleParameterValueMapper<EnumerationValueModel>
{
	private TypeService typeService;
	private final String enumerationCode;

	public DynamicEnumRuleParameterValueMapper(final String enumerationCode)
	{
		this.enumerationCode = enumerationCode;
	}

	@Override
	public String toString(final EnumerationValueModel value)
	{
		ServicesUtil.validateParameterNotNull(value, "EnumerationValueModel cannot be null");
		return value.getCode();
	}

	@Override
	public EnumerationValueModel fromString(final String code)
	{
		ServicesUtil.validateParameterNotNull(code, "String value cannot be null");
		try
		{
			return getTypeService().getEnumerationValue(enumerationCode, code);
		}
		catch (final UnknownIdentifierException ex)
		{
			throw new RuleParameterValueMapperException("Could not find Enumeration Value with the code: " + code);
		}
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
