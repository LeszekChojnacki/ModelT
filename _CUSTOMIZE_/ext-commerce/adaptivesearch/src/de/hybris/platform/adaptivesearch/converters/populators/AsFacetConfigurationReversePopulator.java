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
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacetValue;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacetValue;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetValueModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetValueModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AbstractAsFacetConfigurationModel} from {@link AbstractAsFacetConfiguration}.
 */
public class AsFacetConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsFacetConfiguration, AbstractAsFacetConfigurationModel, AsItemConfigurationReverseConverterContext>
{
	private ContextAwareConverter<AsPromotedFacetValue, AsPromotedFacetValueModel, AsItemConfigurationReverseConverterContext> asPromotedFacetValueReverseConverter;
	private ContextAwareConverter<AsExcludedFacetValue, AsExcludedFacetValueModel, AsItemConfigurationReverseConverterContext> asExcludedFacetValueReverseConverter;

	@Override
	public void populate(final AbstractAsFacetConfiguration source, final AbstractAsFacetConfigurationModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		final AsItemConfigurationReverseConverterContext newContext = new AsItemConfigurationReverseConverterContext();
		newContext.setCatalogVersion(context.getCatalogVersion());
		newContext.setParentConfiguration(target);

		target.setProperty(SEARCH_CONFIGURATION_ATTRIBUTE, context.getParentConfiguration());

		target.setIndexProperty(source.getIndexProperty());
		target.setFacetType(source.getFacetType());
		target.setPriority(source.getPriority());
		target.setValuesSortProvider(source.getValuesSortProvider());
		target.setValuesDisplayNameProvider(source.getValuesDisplayNameProvider());
		target.setTopValuesProvider(source.getTopValuesProvider());
		target.setPromotedValues(asPromotedFacetValueReverseConverter.convertAll(source.getPromotedValues(), newContext));
		target.setExcludedValues(asExcludedFacetValueReverseConverter.convertAll(source.getExcludedValues(), newContext));
	}

	public ContextAwareConverter<AsPromotedFacetValue, AsPromotedFacetValueModel, AsItemConfigurationReverseConverterContext> getAsPromotedFacetValueReverseConverter()
	{
		return asPromotedFacetValueReverseConverter;
	}

	@Required
	public void setAsPromotedFacetValueReverseConverter(
			final ContextAwareConverter<AsPromotedFacetValue, AsPromotedFacetValueModel, AsItemConfigurationReverseConverterContext> asPromotedFacetValueReverseConverter)
	{
		this.asPromotedFacetValueReverseConverter = asPromotedFacetValueReverseConverter;
	}

	public ContextAwareConverter<AsExcludedFacetValue, AsExcludedFacetValueModel, AsItemConfigurationReverseConverterContext> getAsExcludedFacetValueReverseConverter()
	{
		return asExcludedFacetValueReverseConverter;
	}

	@Required
	public void setAsExcludedFacetValueReverseConverter(
			final ContextAwareConverter<AsExcludedFacetValue, AsExcludedFacetValueModel, AsItemConfigurationReverseConverterContext> asExcludedFacetValueReverseConverter)
	{
		this.asExcludedFacetValueReverseConverter = asExcludedFacetValueReverseConverter;
	}
}
