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
import de.hybris.platform.adaptivesearch.data.AsSortExpression;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortExpressionModel;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;


/**
 * Populates {@link AsSortExpressionModel} from {@link AsSortExpression}.
 */
public class AsSortExpressionReversePopulator
		implements ContextAwarePopulator<AsSortExpression, AsSortExpressionModel, AsItemConfigurationReverseConverterContext>
{
	@Override
	public void populate(final AsSortExpression source, final AsSortExpressionModel target,
			final AsItemConfigurationReverseConverterContext context)
	{
		target.setSortConfiguration((AbstractAsSortConfigurationModel) context.getParentConfiguration());

		target.setExpression(source.getExpression());
		target.setOrder(source.getOrder());
	}
}