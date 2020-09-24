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
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AsBoostRuleModel} from {@link AsBoostRule}.
 */
public class AsBoostRuleReversePopulator
		implements ContextAwarePopulator<AsBoostRule, AsBoostRuleModel, AsItemConfigurationReverseConverterContext>
{
	@Override
	public void populate(final AsBoostRule source, final AsBoostRuleModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		target.setIndexProperty(source.getIndexProperty());
		target.setOperator(source.getOperator());
		target.setValue(source.getValue());
		target.setBoostType(source.getBoostType());
		target.setBoost(source.getBoost());
	}
}
