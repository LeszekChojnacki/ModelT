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

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


public class CurrencyRuleParameterValueMapper implements RuleParameterValueMapper<CurrencyModel>
{
	private CommonI18NService commonI18NService;

	@Override
	public String toString(final CurrencyModel currency)
	{
		ServicesUtil.validateParameterNotNull(currency, "Object cannot be null");
		return currency.getIsocode();
	}

	@Override
	public CurrencyModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		final CurrencyModel currency = commonI18NService.getCurrency(value);
		if (currency == null)
		{
			throw new RuleParameterValueMapperException("Cannot find currency with the code: " + value);
		}

		return currency;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
