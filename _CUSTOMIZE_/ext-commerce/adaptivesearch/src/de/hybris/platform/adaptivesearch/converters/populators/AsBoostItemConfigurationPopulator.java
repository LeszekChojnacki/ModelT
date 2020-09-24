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
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsBoostItemConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AbstractAsBoostItemConfiguration} from {@link AbstractAsBoostItemConfigurationModel}.
 */
public class AsBoostItemConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsBoostItemConfigurationModel, AbstractAsBoostItemConfiguration, AsItemConfigurationConverterContext>
{
	@Override
	public void populate(final AbstractAsBoostItemConfigurationModel source, final AbstractAsBoostItemConfiguration target,
			final AsItemConfigurationConverterContext context)
	{
		target.setItemPk(source.getItem().getPk());
	}
}
