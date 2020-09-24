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

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationReverseConverterContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsItemConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsItemConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AbstractAsItemConfigurationModel} from {@link AbstractAsItemConfiguration}.
 */
public class AsItemConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsItemConfiguration, AbstractAsItemConfigurationModel, AsItemConfigurationReverseConverterContext>
{
	@Override
	public void populate(final AbstractAsItemConfiguration source, final AbstractAsItemConfigurationModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		// empty
	}
}
