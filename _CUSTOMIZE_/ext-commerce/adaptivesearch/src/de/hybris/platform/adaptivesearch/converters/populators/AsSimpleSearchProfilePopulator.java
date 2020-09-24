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

import de.hybris.platform.adaptivesearch.converters.AsSearchConfigurationConverterContext;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSimpleSearchProfile;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchProfileModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.converters.Populator;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AsSimpleSearchProfile} from {@link AsSimpleSearchProfileModel}.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericSimpleSearchProfilePopulator}
 */
@Deprecated
public class AsSimpleSearchProfilePopulator implements Populator<AsSimpleSearchProfileModel, AsSimpleSearchProfile>
{
	private ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter;

	@Override
	public void populate(final AsSimpleSearchProfileModel source, final AsSimpleSearchProfile target)
	{
		if (CollectionUtils.isNotEmpty(source.getSearchConfigurations()))
		{
			final AsSearchConfigurationConverterContext context = new AsSearchConfigurationConverterContext();
			context.setSearchProfileCode(source.getCode());

			final AsSimpleSearchConfigurationModel sourceSearchConfiguration = source.getSearchConfigurations().get(0);

			if (!sourceSearchConfiguration.isCorrupted())
			{
				final AsConfigurableSearchConfiguration searchConfiguration = asConfigurableSearchConfigurationConverter
						.convert(sourceSearchConfiguration, context);

				target.setSearchConfiguration(searchConfiguration);
			}
		}
	}

	public ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> getAsConfigurableSearchConfigurationConverter()
	{
		return asConfigurableSearchConfigurationConverter;
	}

	@Required
	public void setAsConfigurableSearchConfigurationConverter(
			final ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter)
	{
		this.asConfigurableSearchConfigurationConverter = asConfigurableSearchConfigurationConverter;
	}
}
