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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSortExpression;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AbstractAsSortConfigurationModel} from {@link AbstractAsSortConfiguration}.
 */
public class AsSortConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsSortConfiguration, AbstractAsSortConfigurationModel, AsItemConfigurationReverseConverterContext>
{
	private I18NService i18NService;

	private ContextAwareConverter<AsSortExpression, AsSortExpressionModel, AsItemConfigurationReverseConverterContext> asSortExpressionReverseConverter;

	@Override
	public void populate(final AbstractAsSortConfiguration source, final AbstractAsSortConfigurationModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		final AsItemConfigurationReverseConverterContext newContext = new AsItemConfigurationReverseConverterContext();
		newContext.setCatalogVersion(context.getCatalogVersion());
		newContext.setParentConfiguration(target);

		target.setProperty(SEARCH_CONFIGURATION_ATTRIBUTE, context.getParentConfiguration());

		target.setCode(source.getCode());

		final Set<Locale> supportedLocales = i18NService.getSupportedLocales();
		for (final Locale locale : supportedLocales)
		{
			target.setName(source.getName().get(locale.toString()), locale);
		}

		target.setPriority(source.getPriority());
		target.setApplyPromotedItems(source.isApplyPromotedItems());
		target.setHighlightPromotedItems(source.isHighlightPromotedItems());
		target.setExpressions(asSortExpressionReverseConverter.convertAll(source.getExpressions(), newContext));
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

	public ContextAwareConverter<AsSortExpression, AsSortExpressionModel, AsItemConfigurationReverseConverterContext> getAsSortExpressionReverseConverter()
	{
		return asSortExpressionReverseConverter;
	}

	@Required
	public void setAsSortExpressionReverseConverter(
			final ContextAwareConverter<AsSortExpression, AsSortExpressionModel, AsItemConfigurationReverseConverterContext> asSortExpressionReverseConverter)
	{
		this.asSortExpressionReverseConverter = asSortExpressionReverseConverter;
	}
}
