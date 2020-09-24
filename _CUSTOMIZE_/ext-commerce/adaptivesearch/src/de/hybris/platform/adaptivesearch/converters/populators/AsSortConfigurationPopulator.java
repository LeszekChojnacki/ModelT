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
package de.hybris.platform.adaptivesearch.converters.populators;

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSortExpression;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AbstractAsSortConfiguration} from {@link AbstractAsSortConfigurationModel}.
 */
public class AsSortConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsSortConfigurationModel, AbstractAsSortConfiguration, AsItemConfigurationConverterContext>
{
	private I18NService i18NService;

	private ContextAwareConverter<AsSortExpressionModel, AsSortExpression, AsItemConfigurationConverterContext> asSortExpressionConverter;

	@Override
	public void populate(final AbstractAsSortConfigurationModel source, final AbstractAsSortConfiguration target,
			final AsItemConfigurationConverterContext context)
	{
		target.setCode(source.getCode());
		target.setName(buildNameLocalizationMap(source));
		target.setPriority(source.getPriority());
		target.setApplyPromotedItems(source.isApplyPromotedItems());
		target.setHighlightPromotedItems(source.isHighlightPromotedItems());
		target.setExpressions(asSortExpressionConverter.convertAll(source.getExpressions(), context));
	}

	protected Map<String, String> buildNameLocalizationMap(final AbstractAsSortConfigurationModel source)
	{
		final Set<Locale> supportedLocales = i18NService.getSupportedLocales();

		return supportedLocales.stream().filter(locale -> StringUtils.isNotBlank(source.getName(locale)))
				.collect(Collectors.toMap(Locale::toString, locale -> source.getName(locale)));
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}

	public ContextAwareConverter<AsSortExpressionModel, AsSortExpression, AsItemConfigurationConverterContext> getAsSortExpressionConverter()
	{
		return asSortExpressionConverter;
	}

	@Required
	public void setAsSortExpressionConverter(
			final ContextAwareConverter<AsSortExpressionModel, AsSortExpression, AsItemConfigurationConverterContext> asSortExpressionConverter)
	{
		this.asSortExpressionConverter = asSortExpressionConverter;
	}
}
