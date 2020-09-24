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
 * Populates {@link AbstractAsFacetConfiguration} from {@link AbstractAsFacetConfigurationModel}.
 */
public class AsFacetConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsFacetConfigurationModel, AbstractAsFacetConfiguration, AsItemConfigurationConverterContext>
{
	private ContextAwareConverter<AsPromotedFacetValueModel, AsPromotedFacetValue, AsItemConfigurationConverterContext> asPromotedFacetValueConverter;
	private ContextAwareConverter<AsExcludedFacetValueModel, AsExcludedFacetValue, AsItemConfigurationConverterContext> asExcludedFacetValueConverter;

	@Override
	public void populate(final AbstractAsFacetConfigurationModel source, final AbstractAsFacetConfiguration target,
			final AsItemConfigurationConverterContext context)
	{
		target.setIndexProperty(source.getIndexProperty());
		target.setFacetType(source.getFacetType());
		target.setPriority(source.getPriority());
		target.setValuesSortProvider(source.getValuesSortProvider());
		target.setValuesDisplayNameProvider(source.getValuesDisplayNameProvider());
		target.setTopValuesProvider(source.getTopValuesProvider());
		target.setPromotedValues(asPromotedFacetValueConverter.convertAll(source.getPromotedValues(), context));
		target.setExcludedValues(asExcludedFacetValueConverter.convertAll(source.getExcludedValues(), context));
	}

	public ContextAwareConverter<AsPromotedFacetValueModel, AsPromotedFacetValue, AsItemConfigurationConverterContext> getAsPromotedFacetValueConverter()
	{
		return asPromotedFacetValueConverter;
	}

	@Required
	public void setAsPromotedFacetValueConverter(
			final ContextAwareConverter<AsPromotedFacetValueModel, AsPromotedFacetValue, AsItemConfigurationConverterContext> asPromotedFacetValueConverter)
	{
		this.asPromotedFacetValueConverter = asPromotedFacetValueConverter;
	}

	public ContextAwareConverter<AsExcludedFacetValueModel, AsExcludedFacetValue, AsItemConfigurationConverterContext> getAsExcludedFacetValueConverter()
	{
		return asExcludedFacetValueConverter;
	}

	@Required
	public void setAsExcludedFacetValueConverter(
			final ContextAwareConverter<AsExcludedFacetValueModel, AsExcludedFacetValue, AsItemConfigurationConverterContext> asExcludedFacetValueConverter)
	{
		this.asExcludedFacetValueConverter = asExcludedFacetValueConverter;
	}
}
