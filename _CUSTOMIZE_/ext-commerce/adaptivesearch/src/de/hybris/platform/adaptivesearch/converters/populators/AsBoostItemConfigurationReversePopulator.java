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
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsBoostItemConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * Populates {@link AbstractAsBoostItemConfigurationModel} from {@link AbstractAsBoostItemConfiguration}.
 */
public class AsBoostItemConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsBoostItemConfiguration, AbstractAsBoostItemConfigurationModel, AsItemConfigurationReverseConverterContext>
{
	private ModelService modelService;

	@Override
	public void populate(final AbstractAsBoostItemConfiguration source, final AbstractAsBoostItemConfigurationModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		target.setProperty(SEARCH_CONFIGURATION_ATTRIBUTE, context.getParentConfiguration());

		final ItemModel item = modelService.get(source.getItemPk());
		target.setItem(item);
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
