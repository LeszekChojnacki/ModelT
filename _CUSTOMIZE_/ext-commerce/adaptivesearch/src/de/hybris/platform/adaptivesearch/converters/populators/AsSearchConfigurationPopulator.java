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
import de.hybris.platform.adaptivesearch.data.AbstractAsSearchConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AbstractAsSearchConfiguration} from {@link AbstractAsSearchConfigurationModel}.
 */
public class AsSearchConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsSearchConfigurationModel, AbstractAsSearchConfiguration, AsSearchConfigurationConverterContext>
{
	@Override
	public void populate(final AbstractAsSearchConfigurationModel source, final AbstractAsSearchConfiguration target,
			final AsSearchConfigurationConverterContext context)
	{
		// empty
	}
}
