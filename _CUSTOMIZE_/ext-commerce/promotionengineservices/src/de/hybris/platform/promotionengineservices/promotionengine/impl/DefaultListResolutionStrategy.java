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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.joining;


import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.servicelayer.config.ConfigurationService;


/**
 * DefaultListResolutionStrategy resolves the given {@link RuleParameterData#getValue()} into a list of values that are resolved
 * by dedicated configured {@link PromotionMessageParameterResolutionStrategy}
 */
public class DefaultListResolutionStrategy implements PromotionMessageParameterResolutionStrategy
{
	protected static final String LIST_ITEMS_SEPARATOR_KEY = "promotionengineservices.listresolutionstrategy.separator";
	private PromotionMessageParameterResolutionStrategy resolutionStrategy;
	private ConfigurationService configurationService;

	@Override
	public String getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale)
	{
		validateParameterNotNull(data, "parameter data must not be null");
		validateParameterNotNull(promotionResult, "parameter promotionResult must not be null");
		validateParameterNotNull(locale, "parameter locale must not be null");

		final List<Object> items = data.getValue();
		return items.stream()
				.map(item -> createRuleParameterData(data, item))
				.map(newData -> itemValue(promotionResult, locale, newData))
				.collect(joining(joiningSeparator()));
	}

	protected String itemValue(final PromotionResultModel promotionResult, final Locale locale, final RuleParameterData newData)
	{
		return valueOf(getResolutionStrategy().getValue(newData, promotionResult, locale));
	}

	protected RuleParameterData createRuleParameterData(final RuleParameterData source, Object value)
	{
		final RuleParameterData data = new RuleParameterData();
		data.setType(source.getType());
		data.setValue(value);
		return data;
	}

	protected PromotionMessageParameterResolutionStrategy getResolutionStrategy()
	{
		return resolutionStrategy;
	}

	@Required
	public void setResolutionStrategy(
			final PromotionMessageParameterResolutionStrategy resolutionStrategy)
	{
		this.resolutionStrategy = resolutionStrategy;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected String joiningSeparator(){
		return getConfigurationService().getConfiguration().getString(LIST_ITEMS_SEPARATOR_KEY,",")+ " ";
	}
}
