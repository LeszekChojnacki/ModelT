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
import de.hybris.platform.adaptivesearch.data.AsCategoryAwareSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchProfileModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;


/**
 * Populates {@link AsCategoryAwareSearchProfile} from {@link AsCategoryAwareSearchProfileModel}.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericCategoryAwareSearchProfilePopulator}
 */
@Deprecated
public class AsCategoryAwareSearchProfilePopulator
		implements Populator<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchProfile>
{
	private ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter;

	@Override
	public void populate(final AsCategoryAwareSearchProfileModel source, final AsCategoryAwareSearchProfile target)
	{
		final Map<PK, AsConfigurableSearchConfiguration> searchConfigurations = new HashMap<>();

		if (CollectionUtils.isNotEmpty(source.getSearchConfigurations()))
		{
			final AsSearchConfigurationConverterContext context = new AsSearchConfigurationConverterContext();
			context.setSearchProfileCode(source.getCode());

			for (final AsCategoryAwareSearchConfigurationModel sourceSearchConfiguration : source.getSearchConfigurations())
			{
				if (!sourceSearchConfiguration.isCorrupted())
				{
					final CategoryModel category = sourceSearchConfiguration.getCategory();
					final PK key = category == null ? null : category.getPk();
					final AsConfigurableSearchConfiguration searchConfiguration = asConfigurableSearchConfigurationConverter
							.convert(sourceSearchConfiguration, context);

					searchConfigurations.put(key, searchConfiguration);
				}
			}
		}

		target.setSearchConfigurations(searchConfigurations);
	}

	public ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> getAsConfigurableSearchConfigurationConverter()
	{
		return asConfigurableSearchConfigurationConverter;
	}

	public void setAsConfigurableSearchConfigurationConverter(
			final ContextAwareConverter<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext> asConfigurableSearchConfigurationConverter)
	{
		this.asConfigurableSearchConfigurationConverter = asConfigurableSearchConfigurationConverter;
	}
}
