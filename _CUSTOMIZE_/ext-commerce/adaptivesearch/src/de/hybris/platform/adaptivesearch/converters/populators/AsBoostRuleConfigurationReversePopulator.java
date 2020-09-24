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
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.model.AbstractAsBoostRuleConfigurationModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AbstractAsBoostRuleConfigurationModel} from {@link AbstractAsBoostRuleConfiguration}.
 */
public class AsBoostRuleConfigurationReversePopulator implements
		ContextAwarePopulator<AbstractAsBoostRuleConfiguration, AbstractAsBoostRuleConfigurationModel, AsItemConfigurationReverseConverterContext>
{
	@Override
	public void populate(final AbstractAsBoostRuleConfiguration source, final AbstractAsBoostRuleConfigurationModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		target.setProperty(SEARCH_CONFIGURATION_ATTRIBUTE, context.getParentConfiguration());
	}
}
