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
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetValueConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetValueConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AbstractAsFacetValueConfiguration} from {@link AbstractAsFacetValueConfigurationModel}.
 */
public class AsFacetValueConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsFacetValueConfigurationModel, AbstractAsFacetValueConfiguration, AsItemConfigurationConverterContext>
{
	@Override
	public void populate(final AbstractAsFacetValueConfigurationModel source, final AbstractAsFacetValueConfiguration target,
			final AsItemConfigurationConverterContext context)
	{
		target.setValue(source.getValue());
	}
}
