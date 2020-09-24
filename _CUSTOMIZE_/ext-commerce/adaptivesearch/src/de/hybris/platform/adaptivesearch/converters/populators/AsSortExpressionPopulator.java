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
import de.hybris.platform.adaptivesearch.data.AsSortExpression;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AsSortExpression} from {@link AsSortExpressionModel}.
 */
public class AsSortExpressionPopulator
		implements ContextAwarePopulator<AsSortExpressionModel, AsSortExpression, AsItemConfigurationConverterContext>
{
	@Override
	public void populate(final AsSortExpressionModel source, final AsSortExpression target,
			final AsItemConfigurationConverterContext context)
	{
		target.setExpression(source.getExpression());
		target.setOrder(source.getOrder());
	}
}